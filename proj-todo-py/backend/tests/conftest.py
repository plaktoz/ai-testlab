"""
Pytest configuration and shared fixtures for the Kanban Task Board API test suite.

Strategy
--------
* An in-memory SQLite database is created once per test session using StaticPool
  so that every SQLAlchemy session/connection sees the same database state.
* Tables are dropped and recreated before each individual test, giving full
  isolation without the overhead of a new engine per test.
* TestClient is created WITHOUT the context-manager protocol, which prevents the
  FastAPI lifespan from running (no production-DB access, no seeding).
* The get_db dependency is overridden on the application to yield sessions from
  the test engine.
"""
import pytest
from sqlalchemy import create_engine
from sqlalchemy.pool import StaticPool
from sqlalchemy.orm import sessionmaker
from starlette.testclient import TestClient

# ---------------------------------------------------------------------------
# Module-level imports that register ORM models with Base.metadata
# ---------------------------------------------------------------------------
import models  # noqa: F401 — side-effect: populates Base.metadata

from database import Base, get_db

# ---------------------------------------------------------------------------
# Single shared in-memory engine (StaticPool ensures one connection = one DB)
# ---------------------------------------------------------------------------

TEST_ENGINE = create_engine(
    "sqlite:///:memory:",
    connect_args={"check_same_thread": False},
    poolclass=StaticPool,
)

TestSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=TEST_ENGINE)


# ---------------------------------------------------------------------------
# Session-level setup (create schema once)
# ---------------------------------------------------------------------------

@pytest.fixture(scope="session", autouse=True)
def _create_schema():
    """Create all tables once for the entire test session."""
    Base.metadata.create_all(bind=TEST_ENGINE)
    yield
    Base.metadata.drop_all(bind=TEST_ENGINE)


# ---------------------------------------------------------------------------
# Per-test isolation: wipe all rows before each test
# ---------------------------------------------------------------------------

@pytest.fixture(autouse=True)
def _clean_tables():
    """
    Truncate all tables before every test so each test starts with an empty DB.
    We delete in reverse dependency order (cards before columns) to avoid FK
    violations.
    """
    session = TestSessionLocal()
    try:
        session.query(models.Card).delete()
        session.query(models.Column).delete()
        session.commit()
    finally:
        session.close()


# ---------------------------------------------------------------------------
# FastAPI dependency override + TestClient
# ---------------------------------------------------------------------------

@pytest.fixture()
def db_session():
    """Yield a database session backed by the in-memory test engine."""
    session = TestSessionLocal()
    try:
        yield session
    finally:
        session.close()


@pytest.fixture()
def client(db_session):
    """
    Return a synchronous Starlette TestClient.

    * get_db is overridden to use the test session.
    * TestClient is used WITHOUT the 'with' context manager so the FastAPI
      lifespan (which would touch the production SQLite file) does not run.
    """
    from main import app

    def override_get_db():
        try:
            yield db_session
        finally:
            pass

    app.dependency_overrides[get_db] = override_get_db
    test_client = TestClient(app, raise_server_exceptions=True)
    yield test_client
    app.dependency_overrides.clear()


# ---------------------------------------------------------------------------
# Pre-seeded data fixtures
# ---------------------------------------------------------------------------

@pytest.fixture()
def test_column(db_session):
    """Insert one Column and return it."""
    col = models.Column(name="Test Column", color="#FF0000", position=0)
    db_session.add(col)
    db_session.commit()
    db_session.refresh(col)
    return col


@pytest.fixture()
def second_column(db_session, test_column):
    """Insert a second Column at position 1."""
    col = models.Column(name="Second Column", color="#00FF00", position=1)
    db_session.add(col)
    db_session.commit()
    db_session.refresh(col)
    return col


@pytest.fixture()
def third_column(db_session, second_column):
    """Insert a third Column at position 2."""
    col = models.Column(name="Third Column", color="#0000FF", position=2)
    db_session.add(col)
    db_session.commit()
    db_session.refresh(col)
    return col


@pytest.fixture()
def test_task(db_session, test_column):
    """Insert one Card in test_column at position 0."""
    from datetime import datetime, timezone
    now = datetime.now(timezone.utc)
    card = models.Card(
        title="Test Task",
        description="A test card",
        column_id=test_column.id,
        priority="low",
        position=0,
        created_at=now,
        updated_at=now,
    )
    db_session.add(card)
    db_session.commit()
    db_session.refresh(card)
    return card


@pytest.fixture()
def second_task(db_session, test_column, test_task):
    """Insert a second Card in test_column at position 1."""
    from datetime import datetime, timezone
    now = datetime.now(timezone.utc)
    card = models.Card(
        title="Second Task",
        description="Another test card",
        column_id=test_column.id,
        priority="medium",
        position=1,
        created_at=now,
        updated_at=now,
    )
    db_session.add(card)
    db_session.commit()
    db_session.refresh(card)
    return card


@pytest.fixture()
def third_task(db_session, test_column, second_task):
    """Insert a third Card in test_column at position 2."""
    from datetime import datetime, timezone
    now = datetime.now(timezone.utc)
    card = models.Card(
        title="Third Task",
        description="Third test card",
        column_id=test_column.id,
        priority="high",
        position=2,
        created_at=now,
        updated_at=now,
    )
    db_session.add(card)
    db_session.commit()
    db_session.refresh(card)
    return card
