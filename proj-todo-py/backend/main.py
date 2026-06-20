import logging
from contextlib import asynccontextmanager
from typing import List, Optional

from fastapi import Depends, FastAPI, HTTPException, Query, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session

from database import create_tables, get_db
from models import Card, Column
from schemas import (
    CardCreate,
    CardMoveRequest,
    CardReorderRequest,
    CardResponse,
    CardUpdate,
    ColumnCreate,
    ColumnReorderRequest,
    ColumnResponse,
    ColumnUpdate,
    ErrorResponse,
)

logger = logging.getLogger(__name__)


def seed_columns(db: Session) -> None:
    count = db.query(Column).count()
    if count == 0:
        defaults = [
            Column(name="To Do", color="#6B7280", position=0),
            Column(name="In Progress", color="#3B82F6", position=1),
            Column(name="Done", color="#10B981", position=2),
        ]
        db.add_all(defaults)
        db.commit()
        logger.info("Seeded 3 default columns.")


@asynccontextmanager
async def lifespan(app: FastAPI):
    create_tables()
    from database import SessionLocal
    db = SessionLocal()
    try:
        seed_columns(db)
    finally:
        db.close()
    yield


app = FastAPI(
    title="Kanban Task Board API",
    version="1.0.0",
    description="A production-ready Kanban board REST API built with FastAPI and SQLAlchemy.",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",
        "http://127.0.0.1:3000",
        "null",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from fastapi import Request
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError


@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    return JSONResponse(
        status_code=exc.status_code,
        content={"error": _status_to_code(exc.status_code), "message": exc.detail},
    )


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    return JSONResponse(
        status_code=422,
        content={"error": "VALIDATION_ERROR", "message": str(exc.errors())},
    )


def _status_to_code(status_code: int) -> str:
    mapping = {
        400: "BAD_REQUEST",
        404: "NOT_FOUND",
        422: "VALIDATION_ERROR",
        500: "INTERNAL_SERVER_ERROR",
    }
    return mapping.get(status_code, "ERROR")


# ---------------------------------------------------------------------------
# Column endpoints
# ---------------------------------------------------------------------------

@app.get("/columns", response_model=List[ColumnResponse], status_code=200)
def get_columns(db: Session = Depends(get_db)):
    return db.query(Column).order_by(Column.position.asc()).all()


@app.post("/columns", response_model=ColumnResponse, status_code=201)
def create_column(payload: ColumnCreate, db: Session = Depends(get_db)):
    max_row = db.query(Column).order_by(Column.position.desc()).first()
    next_position = (max_row.position + 1) if max_row else 0
    col = Column(name=payload.name, color=payload.color, position=next_position)
    db.add(col)
    db.commit()
    db.refresh(col)
    return col


@app.put("/columns/{column_id}", response_model=ColumnResponse, status_code=200)
def replace_column(column_id: int, payload: ColumnUpdate, db: Session = Depends(get_db)):
    col = db.query(Column).filter(Column.id == column_id).first()
    if not col:
        raise HTTPException(status_code=404, detail=f"Column {column_id} not found.")
    col.name = payload.name
    if payload.color is not None:
        col.color = payload.color
    db.commit()
    db.refresh(col)
    return col


@app.patch("/columns/{column_id}", response_model=ColumnResponse, status_code=200)
def partial_update_column(column_id: int, payload: ColumnUpdate, db: Session = Depends(get_db)):
    col = db.query(Column).filter(Column.id == column_id).first()
    if not col:
        raise HTTPException(status_code=404, detail=f"Column {column_id} not found.")
    if payload.name is not None:
        col.name = payload.name
    if payload.color is not None:
        col.color = payload.color
    db.commit()
    db.refresh(col)
    return col


@app.delete("/columns/{column_id}", status_code=204)
def delete_column(column_id: int, db: Session = Depends(get_db)):
    col = db.query(Column).filter(Column.id == column_id).first()
    if not col:
        raise HTTPException(status_code=404, detail=f"Column {column_id} not found.")
    deleted_position = col.position
    db.delete(col)
    db.commit()
    # Compact remaining positions
    remaining = (
        db.query(Column)
        .filter(Column.position > deleted_position)
        .order_by(Column.position.asc())
        .all()
    )
    for i, c in enumerate(remaining):
        c.position = deleted_position + i
    db.commit()
    return None


@app.patch("/columns/reorder", response_model=List[ColumnResponse], status_code=200)
def reorder_columns(payload: ColumnReorderRequest, db: Session = Depends(get_db)):
    all_columns = db.query(Column).all()
    all_ids = {c.id for c in all_columns}
    requested_ids = payload.column_ids

    if set(requested_ids) != all_ids or len(requested_ids) != len(all_ids):
        raise HTTPException(
            status_code=400,
            detail="column_ids must include every column exactly once.",
        )

    id_to_col = {c.id: c for c in all_columns}
    for new_pos, col_id in enumerate(requested_ids):
        id_to_col[col_id].position = new_pos
    db.commit()

    return db.query(Column).order_by(Column.position.asc()).all()


# ---------------------------------------------------------------------------
# Column-level card reorder
# ---------------------------------------------------------------------------

@app.patch("/columns/{column_id}/cards/reorder", response_model=List[CardResponse], status_code=200)
def reorder_cards_in_column(
    column_id: int,
    payload: CardReorderRequest,
    db: Session = Depends(get_db),
):
    col = db.query(Column).filter(Column.id == column_id).first()
    if not col:
        raise HTTPException(status_code=404, detail=f"Column {column_id} not found.")

    cards_in_col = db.query(Card).filter(Card.column_id == column_id).all()
    col_card_ids = {c.id for c in cards_in_col}
    requested_ids = payload.card_ids

    if set(requested_ids) != col_card_ids or len(requested_ids) != len(col_card_ids):
        raise HTTPException(
            status_code=400,
            detail="card_ids must include every card in the column exactly once.",
        )

    id_to_card = {c.id: c for c in cards_in_col}
    for new_pos, card_id in enumerate(requested_ids):
        id_to_card[card_id].position = new_pos
    db.commit()

    return (
        db.query(Card)
        .filter(Card.column_id == column_id)
        .order_by(Card.position.asc())
        .all()
    )


# ---------------------------------------------------------------------------
# Card endpoints
# ---------------------------------------------------------------------------

@app.get("/cards", response_model=List[CardResponse], status_code=200)
def get_cards(column_id: Optional[int] = Query(None), db: Session = Depends(get_db)):
    query = db.query(Card).join(Column, Card.column_id == Column.id)
    if column_id is not None:
        query = query.filter(Card.column_id == column_id)
    return query.order_by(Column.position.asc(), Card.position.asc()).all()


@app.get("/cards/{card_id}", response_model=CardResponse, status_code=200)
def get_card(card_id: int, db: Session = Depends(get_db)):
    card = db.query(Card).filter(Card.id == card_id).first()
    if not card:
        raise HTTPException(status_code=404, detail=f"Card {card_id} not found.")
    return card


@app.post("/cards", response_model=CardResponse, status_code=201)
def create_card(payload: CardCreate, db: Session = Depends(get_db)):
    col = db.query(Column).filter(Column.id == payload.column_id).first()
    if not col:
        raise HTTPException(status_code=404, detail=f"Column {payload.column_id} not found.")

    max_card = (
        db.query(Card)
        .filter(Card.column_id == payload.column_id)
        .order_by(Card.position.desc())
        .first()
    )
    next_position = (max_card.position + 1) if max_card else 0

    card = Card(
        title=payload.title,
        description=payload.description or "",
        column_id=payload.column_id,
        priority=payload.priority,
        position=next_position,
    )
    db.add(card)
    db.commit()
    db.refresh(card)
    return card


@app.put("/cards/{card_id}", response_model=CardResponse, status_code=200)
def replace_card(card_id: int, payload: CardUpdate, db: Session = Depends(get_db)):
    card = db.query(Card).filter(Card.id == card_id).first()
    if not card:
        raise HTTPException(status_code=404, detail=f"Card {card_id} not found.")

    if payload.column_id is not None:
        col = db.query(Column).filter(Column.id == payload.column_id).first()
        if not col:
            raise HTTPException(status_code=404, detail=f"Column {payload.column_id} not found.")
        card.column_id = payload.column_id

    card.title = payload.title
    card.description = payload.description if payload.description is not None else ""
    if payload.priority is not None:
        card.priority = payload.priority
    if payload.position is not None:
        card.position = payload.position

    from datetime import datetime, timezone
    card.updated_at = datetime.now(timezone.utc)
    db.commit()
    db.refresh(card)
    return card


@app.patch("/cards/{card_id}", response_model=CardResponse, status_code=200)
def partial_update_card(card_id: int, payload: CardUpdate, db: Session = Depends(get_db)):
    card = db.query(Card).filter(Card.id == card_id).first()
    if not card:
        raise HTTPException(status_code=404, detail=f"Card {card_id} not found.")

    if payload.column_id is not None:
        col = db.query(Column).filter(Column.id == payload.column_id).first()
        if not col:
            raise HTTPException(status_code=404, detail=f"Column {payload.column_id} not found.")
        card.column_id = payload.column_id

    if payload.title is not None:
        card.title = payload.title
    if payload.description is not None:
        card.description = payload.description
    if payload.priority is not None:
        card.priority = payload.priority
    if payload.position is not None:
        card.position = payload.position

    from datetime import datetime, timezone
    card.updated_at = datetime.now(timezone.utc)
    db.commit()
    db.refresh(card)
    return card


@app.delete("/cards/{card_id}", status_code=204)
def delete_card(card_id: int, db: Session = Depends(get_db)):
    card = db.query(Card).filter(Card.id == card_id).first()
    if not card:
        raise HTTPException(status_code=404, detail=f"Card {card_id} not found.")

    col_id = card.column_id
    deleted_pos = card.position
    db.delete(card)
    db.commit()

    # Compact positions in the same column
    remaining = (
        db.query(Card)
        .filter(Card.column_id == col_id, Card.position > deleted_pos)
        .order_by(Card.position.asc())
        .all()
    )
    for i, c in enumerate(remaining):
        c.position = deleted_pos + i
    db.commit()
    return None


@app.patch("/cards/{card_id}/move", response_model=CardResponse, status_code=200)
def move_card(card_id: int, payload: CardMoveRequest, db: Session = Depends(get_db)):
    card = db.query(Card).filter(Card.id == card_id).first()
    if not card:
        raise HTTPException(status_code=404, detail=f"Card {card_id} not found.")

    target_col = db.query(Column).filter(Column.id == payload.column_id).first()
    if not target_col:
        raise HTTPException(status_code=404, detail=f"Column {payload.column_id} not found.")

    src_col_id = card.column_id
    src_pos = card.position
    dst_col_id = payload.column_id
    dst_pos = payload.position

    if src_col_id == dst_col_id:
        # Within same column reorder
        if src_pos < dst_pos:
            # Moving down: shift cards between src_pos+1..dst_pos up by -1
            db.query(Card).filter(
                Card.column_id == src_col_id,
                Card.position > src_pos,
                Card.position <= dst_pos,
                Card.id != card_id,
            ).update({"position": Card.position - 1}, synchronize_session="fetch")
        elif src_pos > dst_pos:
            # Moving up: shift cards between dst_pos..src_pos-1 down by +1
            db.query(Card).filter(
                Card.column_id == src_col_id,
                Card.position >= dst_pos,
                Card.position < src_pos,
                Card.id != card_id,
            ).update({"position": Card.position + 1}, synchronize_session="fetch")
        card.position = dst_pos
    else:
        # Cross-column move
        # 1. Compact source column
        db.query(Card).filter(
            Card.column_id == src_col_id,
            Card.position > src_pos,
        ).update({"position": Card.position - 1}, synchronize_session="fetch")

        # 2. Make room in destination column
        db.query(Card).filter(
            Card.column_id == dst_col_id,
            Card.position >= dst_pos,
        ).update({"position": Card.position + 1}, synchronize_session="fetch")

        card.column_id = dst_col_id
        card.position = dst_pos

    from datetime import datetime, timezone
    card.updated_at = datetime.now(timezone.utc)
    db.commit()
    db.refresh(card)
    return card