from datetime import datetime, timezone

from sqlalchemy import (
    CheckConstraint,
    Column as SAColumn,
    DateTime,
    ForeignKey,
    Integer,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.orm import relationship

from database import Base


class Column(Base):
    __tablename__ = "columns"

    id = SAColumn(Integer, primary_key=True, autoincrement=True)
    name = SAColumn(String(255), nullable=False)
    color = SAColumn(String(7), nullable=False, default="#6B7280")
    position = SAColumn(Integer, nullable=False)

    cards = relationship(
        "Card",
        back_populates="column",
        cascade="all, delete-orphan",
        passive_deletes=True,
    )

    __table_args__ = (UniqueConstraint("position", name="uq_column_position"),)


class Card(Base):
    __tablename__ = "cards"

    id = SAColumn(Integer, primary_key=True, autoincrement=True)
    title = SAColumn(String(255), nullable=False)
    description = SAColumn(Text, default="", nullable=True)
    column_id = SAColumn(
        Integer,
        ForeignKey("columns.id", ondelete="CASCADE"),
        nullable=False,
    )
    priority = SAColumn(
        String(8),
        nullable=False,
        default="low",
    )
    position = SAColumn(Integer, nullable=False)
    created_at = SAColumn(
        DateTime(timezone=True),
        nullable=False,
        default=lambda: datetime.now(timezone.utc),
    )
    updated_at = SAColumn(
        DateTime(timezone=True),
        nullable=False,
        default=lambda: datetime.now(timezone.utc),
        onupdate=lambda: datetime.now(timezone.utc),
    )

    column = relationship("Column", back_populates="cards")

    __table_args__ = (
        CheckConstraint(
            "priority IN ('low','medium','high','critical')",
            name="ck_card_priority",
        ),
    )
