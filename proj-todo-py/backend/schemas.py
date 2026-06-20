from datetime import datetime
from typing import List, Literal, Optional

from pydantic import BaseModel, ConfigDict, Field


# ---------------------------------------------------------------------------
# Column schemas
# ---------------------------------------------------------------------------

class ColumnCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=255)
    color: str = Field(default="#6B7280", max_length=7)


class ColumnUpdate(BaseModel):
    """Used for both PUT (all fields technically optional at schema level,
    PUT handler enforces name required) and PATCH (truly all optional)."""

    name: Optional[str] = Field(default=None, min_length=1, max_length=255)
    color: Optional[str] = Field(default=None, max_length=7)


class ColumnResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str
    color: str
    position: int


class ColumnReorderRequest(BaseModel):
    column_ids: List[int]


# ---------------------------------------------------------------------------
# Card schemas
# ---------------------------------------------------------------------------

PriorityLiteral = Literal["low", "medium", "high", "critical"]


class CardCreate(BaseModel):
    column_id: int
    title: str = Field(..., min_length=1, max_length=255)
    description: Optional[str] = Field(default="")
    priority: PriorityLiteral = "low"


class CardUpdate(BaseModel):
    """Used for both PUT and PATCH card endpoints."""

    title: Optional[str] = Field(default=None, min_length=1, max_length=255)
    description: Optional[str] = None
    priority: Optional[PriorityLiteral] = None
    column_id: Optional[int] = None
    position: Optional[int] = None


class CardMoveRequest(BaseModel):
    column_id: int
    position: int = Field(..., ge=0)


class CardReorderRequest(BaseModel):
    card_ids: List[int]


class CardResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    column_id: int
    title: str
    description: Optional[str]
    priority: str
    position: int
    created_at: datetime
    updated_at: datetime


# ---------------------------------------------------------------------------
# Error schema
# ---------------------------------------------------------------------------

class ErrorResponse(BaseModel):
    error: str
    message: str
