# Lesson Plan: Loop Engineering

*Source: `research/loop-engineering/report.md`*
*Each lesson → one HTML file in `lessons/`, one reference doc in `reference/`*

---

## Module 1 — Foundations: The Loop as a Universal Pattern
*Establish the core mental model — observe→decide→act — and the empirical case for why iterative loops outperform single-pass systems before introducing any LLM-specific vocabulary.*

### Lesson 1: The OODA Loop — Origins and Universal Structure
**File:** `lessons/0001-ooda-loop.html`
**Key concepts:** observe · orient · decide · act · tempo · cycling speed as competitive advantage
**Source paper:** TechTarget — OODA Loop definition (Boyd's aerial combat research); same source reused in Section 4
**Skill:** Draw the four-stage OODA cycle from memory, label each stage, and write one concrete example for each stage from a domain other than military (e.g. product iteration, debugging a bug)
**Reference doc:** `reference/ooda-loop.html`

**Implementation:** Wire the four OODA stages as named nodes in a LangGraph `StateGraph`; the conditional edge on `act` implements the "cycle back" behaviour that makes OODA iterative rather than a one-shot pipeline. Swap the stub functions for real LLM calls or environment reads — the graph structure stays the same.

```python
from langgraph.graph import StateGraph, END
from typing import TypedDict, Literal

class OODAState(TypedDict):
    environment_data: str
    mental_model: str
    decision: str
    action_result: str
    cycles: int
    goal_achieved: bool

def observe(state: OODAState) -> dict:
    # Gather raw data from the environment (sensor, API, user input, etc.)
    return {"environment_data": f"signal_{state['cycles']}", "cycles": state["cycles"] + 1}

def orient(state: OODAState) -> dict:
    # Interpret new data against the current mental model
    return {"mental_model": f"model(prev={state['mental_model']!r}, new={state['environment_data']!r})"}

def decide(state: OODAState) -> dict:
    # Select the best action given the updated orientation
    return {"decision": f"act_on({state['mental_model']})"}

def act(state: OODAState) -> dict:
    # Execute; evaluate whether the goal is now met
    goal_achieved = state["cycles"] >= 3      # replace with real termination condition
    return {"action_result": f"executed({state['decision']})", "goal_achieved": goal_achieved}

def loop_or_stop(state: OODAState) -> Literal["observe", "__end__"]:
    return END if state["goal_achieved"] else "observe"

builder = StateGraph(OODAState)
for fn in [observe, orient, decide, act]:
    builder.add_node(fn.__name__, fn)
builder.set_entry_point("observe")
builder.add_edge("observe", "orient")
builder.add_edge("orient", "decide")
builder.add_edge("decide", "act")
builder.add_conditional_edges("act", loop_or_stop)

app = builder.compile()
final = app.invoke({"mental_model": "", "cycles": 0, "goal_achieved": False,
                    "environment_data": "", "decision": "", "action_result": ""})
print(f"Completed in {final['cycles']} cycles: {final['action_result']}")
```

---

### Lesson 2: Why Loops Beat Single-Pass — The Empirical Case
**File:** `lessons/0002-loops-vs-single-pass.html`
**Key concepts:** zero-shot vs. agentic performance · HumanEval benchmark · iterative refinement · the "no backspace" analogy
**Source paper:** DeepLearning.AI / Andrew Ng — "How Agents Can Improve LLM Performance"; GPT-3.5 zero-shot 48.1% → agent loop 95.1% vs. GPT-4 zero-shot 67.0%
**Skill:** Given a task description (e.g. "write a function that parses JSON"), rewrite it as an explicit loop: what does each iteration observe, evaluate, and revise? Write out 2–3 loop iterations as pseudocode
**Reference doc:** `reference/agentic-performance-data.html`

**Implementation:** Build a `generate → test` loop where each failed `exec()` feeds back as the revision prompt for the next iteration; the conditional edge on `test_code` terminates when the code passes or the iteration budget is exhausted. To reproduce the benchmark comparison, call `app.invoke` twice — once with `max_iterations=1` (zero-shot) and once with `max_iterations=5` (agentic).

```python
from langgraph.graph import StateGraph, END
from langchain_anthropic import ChatAnthropic
from typing import TypedDict, Literal

llm = ChatAnthropic(model="claude-sonnet-5")

class RefinementState(TypedDict):
    task: str
    code: str
    error: str
    iterations: int
    max_iterations: int
    passed: bool

def generate(state: RefinementState) -> dict:
    if state["error"]:
        prompt = (
            f"Fix this code.\nTask: {state['task']}\n"
            f"Code:\n{state['code']}\nError: {state['error']}"
        )
    else:
        prompt = f"Write Python code for: {state['task']}"
    return {"code": llm.invoke(prompt).content, "iterations": state["iterations"] + 1}

def test_code(state: RefinementState) -> dict:
    try:
        exec(state["code"], {})
        return {"error": "", "passed": True}
    except Exception as exc:
        return {"error": str(exc), "passed": False}

def continue_or_stop(state: RefinementState) -> Literal["generate", "__end__"]:
    if state["passed"] or state["iterations"] >= state["max_iterations"]:
        return END
    return "generate"

builder = StateGraph(RefinementState)
builder.add_node("generate", generate)
builder.add_node("test_code", test_code)
builder.set_entry_point("generate")
builder.add_edge("generate", "test_code")
builder.add_conditional_edges("test_code", continue_or_stop)
app = builder.compile()

# Zero-shot (single pass)
r1 = app.invoke({"task": "write a function sum_list(lst) that returns the sum", "error": "", "iterations": 0, "max_iterations": 1, "passed": False, "code": ""})
# Agentic loop (up to 5 attempts)
r2 = app.invoke({"task": "write a function sum_list(lst) that returns the sum", "error": "", "iterations": 0, "max_iterations": 5, "passed": False, "code": ""})
print(f"Zero-shot passed: {r1['passed']}  |  Loop passed: {r2['passed']} in {r2['iterations']} iteration(s)")
```

---

### Lesson 3: Anatomy of an Agentic Loop — Planning, Memory, Tool Use
**File:** `lessons/0003-anatomy-agentic-loop.html`
**Key concepts:** planning (CoT, ToT) · memory (sensory/short-term/long-term) · tool use (MRKL, Toolformer) · ReAct cycle (Thought → Action → Observation)
**Source paper:** Lilian Weng — "LLM Powered Autonomous Agents" (2023); foundational taxonomy of all three loop components
**Skill:** For a given agent task (e.g. "research a company and write a summary"), identify which memory tier each piece of information belongs to, which tools are needed, and sketch the ReAct loop as a sequence of Thought/Action/Observation steps
**Reference doc:** `reference/agent-components.html`

**Implementation:** The `agent` node handles **Thought + Action** (the LLM picks a tool call); `ToolNode` handles **Observation** (executing the call and returning the result); the conditional edge on `agent` routes to `tools` when there are pending tool calls, and to `END` when the model produces a final answer. This is the canonical LangGraph ReAct pattern.

```python
from langgraph.graph import StateGraph, END
from langgraph.prebuilt import ToolNode
from langchain_anthropic import ChatAnthropic
from langchain_core.messages import HumanMessage, BaseMessage
from langchain_core.tools import tool
from typing import TypedDict, Annotated, Literal
import operator

@tool
def search_web(query: str) -> str:
    """Search the web for up-to-date information."""
    return f"[search results for '{query}']"   # replace with real search call

@tool
def read_file(path: str) -> str:
    """Read a local file and return its contents."""
    try:
        return open(path).read()
    except FileNotFoundError:
        return f"File not found: {path}"

tools = [search_web, read_file]
llm = ChatAnthropic(model="claude-sonnet-5").bind_tools(tools)

class AgentState(TypedDict):
    messages: Annotated[list[BaseMessage], operator.add]

def agent(state: AgentState) -> dict:
    # Thought + Action: model reasons and emits a tool_call or a final answer
    return {"messages": [llm.invoke(state["messages"])]}

def route(state: AgentState) -> Literal["tools", "__end__"]:
    last = state["messages"][-1]
    return "tools" if getattr(last, "tool_calls", None) else END

tool_node = ToolNode(tools)   # Observation: execute the tool call, return result

builder = StateGraph(AgentState)
builder.add_node("agent", agent)
builder.add_node("tools", tool_node)
builder.set_entry_point("agent")
builder.add_conditional_edges("agent", route)
builder.add_edge("tools", "agent")        # Observation feeds back into the next Thought

app = builder.compile()
result = app.invoke({"messages": [HumanMessage("Search for recent LLM benchmark results.")]})
print(result["messages"][-1].content)
```

---

## Module 2 — Human Oversight: The Spectrum
*Shift from "what is a loop" to "who controls it" — introducing the HITL spectrum and the principle that oversight is set per action type, not per system.*

### Lesson 4: The Human-in-the-Loop Spectrum
**File:** `lessons/0004-hitl-spectrum.html`
**Key concepts:** human-in-the-loop · human-on-the-loop · human-out-of-the-loop · autonomy criteria · oversight vs. automation trade-offs
**Source paper:** Google Cloud — "Human-in-the-Loop: Full Spectrum"; three oversight models with concrete placement criteria
**Skill:** Take three real AI system scenarios (e.g. spam filter, medical diagnosis assistant, automated PR reviewer) and classify each on the spectrum — justify with the criteria from the source (confidence, reversibility, regulatory, volume)
**Reference doc:** `reference/hitl-spectrum.html`

**Implementation:** LangGraph's `interrupt()` suspends graph execution and hands control back to the caller; a `MemorySaver` checkpointer persists state across the suspension so the graph can resume from the exact same node after the human responds via `Command(resume=...)`. Gate selection is driven by the `oversight` field in state, so you can switch modes per request without touching the graph structure.

```python
from langgraph.graph import StateGraph, END
from langgraph.checkpoint.memory import MemorySaver
from langgraph.types import interrupt, Command
from typing import TypedDict, Literal

class ReviewState(TypedDict):
    action: str
    confidence: float   # 0.0 – 1.0 model confidence
    oversight: str      # "full" | "supervised" | "autonomous"
    result: str

def route_oversight(state: ReviewState) -> Literal["full_gate", "supervised_gate", "execute"]:
    return {"full": "full_gate", "supervised": "supervised_gate", "autonomous": "execute"}[state["oversight"]]

def full_gate(state: ReviewState) -> dict:
    # Human-in-the-loop: always pause, regardless of confidence
    answer = interrupt({"message": f"Approve '{state['action']}'?"})
    return {"result": "rejected"} if answer != "yes" else {}

def supervised_gate(state: ReviewState) -> dict:
    # Human-on-the-loop: pause only when model is uncertain
    if state["confidence"] < 0.75:
        answer = interrupt({"message": f"Confidence {state['confidence']:.0%} — approve '{state['action']}'?"})
        return {"result": "rejected"} if answer != "yes" else {}
    return {}

def execute(state: ReviewState) -> dict:
    if state.get("result") == "rejected":
        return {}
    return {"result": f"executed: {state['action']}"}

checkpointer = MemorySaver()
builder = StateGraph(ReviewState)
builder.add_node("router", lambda s: s)
builder.add_node("full_gate", full_gate)
builder.add_node("supervised_gate", supervised_gate)
builder.add_node("execute", execute)
builder.set_entry_point("router")
builder.add_conditional_edges("router", route_oversight)
builder.add_edge("full_gate", "execute")
builder.add_edge("supervised_gate", "execute")
builder.add_edge("execute", END)

app = builder.compile(checkpointer=checkpointer)
config = {"configurable": {"thread_id": "review-1"}}

# First invoke pauses at interrupt; resume with:
# app.invoke(Command(resume="yes"), config=config)
app.invoke({"action": "delete_user_data", "confidence": 0.95, "oversight": "full", "result": ""}, config=config)
```

---

### Lesson 5: Designing Oversight at the Action Level
**File:** `lessons/0005-action-level-oversight.html`
**Key concepts:** per-action autonomy · reversibility · consequence severity · intern analogy · checkpoint placement
**Source paper:** Chip Huyen — "Agents" (2025-01) on oversight as a continuum; Anthropic — human checkpoints before irreversible actions
**Skill:** Given a list of five agent actions (read file, write file, send email, delete record, query API), rate each on a reversibility/consequence matrix and assign an oversight level; then identify where in the loop you would insert a human gate
**Reference doc:** *(extend existing hitl-spectrum.html)*

**Implementation:** The `gate` node checks each incoming action against a policy table keyed on reversibility and severity; only actions that fail the policy call `interrupt()`, so cheap read-only actions like `read_file` flow straight to `execute` with no human pause. Add new action types to `ACTION_POLICY` to extend coverage without changing the graph.

```python
from langgraph.graph import StateGraph, END
from langgraph.checkpoint.memory import MemorySaver
from langgraph.types import interrupt, Command
from typing import TypedDict

ACTION_POLICY = {
    "read_file":     {"reversible": True,  "severity": "low"},
    "write_file":    {"reversible": True,  "severity": "medium"},
    "query_api":     {"reversible": True,  "severity": "medium"},
    "send_email":    {"reversible": False, "severity": "high"},
    "delete_record": {"reversible": False, "severity": "critical"},
}

def needs_gate(action: str) -> bool:
    policy = ACTION_POLICY.get(action, {"reversible": False, "severity": "critical"})
    return not policy["reversible"] or policy["severity"] in ("high", "critical")

class ActionState(TypedDict):
    action: str
    payload: dict
    result: str

def gate(state: ActionState) -> dict:
    if needs_gate(state["action"]):
        policy = ACTION_POLICY.get(state["action"], {})
        answer = interrupt({
            "action": state["action"],
            "severity": policy.get("severity"),
            "reversible": policy.get("reversible"),
            "message": "Irreversible or high-severity — approve?",
        })
        if answer != "yes":
            return {"result": "blocked by oversight gate"}
    return {}

def execute(state: ActionState) -> dict:
    if state.get("result"):
        return {}   # already blocked upstream
    return {"result": f"executed {state['action']}({state['payload']})"}

checkpointer = MemorySaver()
builder = StateGraph(ActionState)
builder.add_node("gate", gate)
builder.add_node("execute", execute)
builder.set_entry_point("gate")
builder.add_edge("gate", "execute")
builder.add_edge("execute", END)

app = builder.compile(checkpointer=checkpointer)
config = {"configurable": {"thread_id": "action-1"}}

# send_email triggers interrupt; read_file flows straight through
app.invoke({"action": "send_email", "payload": {"to": "team@co.com"}, "result": ""}, config=config)
# app.invoke(Command(resume="yes"), config=config)   # to approve
```

---

## Module 3 — Agentic Loop Patterns
*Survey the canonical loop architectures: reflection, planning, and orchestration — and the state management machinery that makes them work.*

### Lesson 6: Reflection Loops — Generate, Critique, Revise
**File:** `lessons/0006-reflection-loops.html`
**Key concepts:** generate → critique → revise cycle · basic reflection · Reflexion · grounded critique (tool outputs) · separate generator/critic agents
**Source paper:** Andrew Ng — "Agentic Design Patterns Part 2: Reflection"; LangChain — "Reflection Agents" (basic reflection and Reflexion sections)
**Skill:** Design a reflection loop for a code-writing task: write out what the generator produces, what the critic checks (style, tests, correctness), what the revision prompt looks like, and define the stopping condition
**Reference doc:** `reference/reflection-patterns.html`

**Implementation:** The `generate → critique` edge is always traversed; the conditional edge on `critique` loops back to `generate` (passing the critique as the revision prompt) or exits when the critique begins with `APPROVED:` or the revision budget is exhausted. To use a separate critic model, swap `llm` in the `critique` node for a different `ChatAnthropic` instance.

```python
from langgraph.graph import StateGraph, END
from langchain_anthropic import ChatAnthropic
from typing import TypedDict, Literal

llm = ChatAnthropic(model="claude-sonnet-5")

class ReflectionState(TypedDict):
    task: str
    draft: str
    critique: str
    revisions: int
    approved: bool

def generate(state: ReflectionState) -> dict:
    if state["critique"]:
        prompt = (
            f"Revise based on this critique.\n\n"
            f"Task: {state['task']}\nDraft: {state['draft']}\nCritique: {state['critique']}"
        )
    else:
        prompt = f"Write a first draft for: {state['task']}"
    return {"draft": llm.invoke(prompt).content, "revisions": state["revisions"] + 1}

def critique(state: ReflectionState) -> dict:
    prompt = (
        f"Critique this draft. If it fully satisfies the task, start with 'APPROVED:'.\n\n"
        f"Task: {state['task']}\nDraft: {state['draft']}"
    )
    feedback = llm.invoke(prompt).content
    return {"critique": feedback, "approved": feedback.startswith("APPROVED:")}

def route(state: ReflectionState) -> Literal["generate", "__end__"]:
    return END if state["approved"] or state["revisions"] >= 3 else "generate"

builder = StateGraph(ReflectionState)
builder.add_node("generate", generate)
builder.add_node("critique", critique)
builder.set_entry_point("generate")
builder.add_edge("generate", "critique")
builder.add_conditional_edges("critique", route)

app = builder.compile()
result = app.invoke({"task": "Explain recursion in plain English.", "critique": "", "revisions": 0, "approved": False, "draft": ""})
print(f"Approved after {result['revisions']} revision(s):\n{result['draft']}")
```

---

### Lesson 7: Advanced Reflection — LATS and Tree Search
**File:** `lessons/0007-lats-tree-search.html`
**Key concepts:** LATS (Language Agent Tree Search) · Monte Carlo Tree Search · select → expand → reflect → backpropagate · trajectory scoring · latency vs. quality trade-off
**Source paper:** LangChain — "Reflection Agents" (LATS section); unification of Reflexion + Tree of Thoughts + plan-and-execute
**Skill:** Trace through a two-level LATS expansion by hand: given a root node (initial response), sketch two candidate children, score each, and write what "backpropagating" the scores would tell the system about which branch to continue
**Reference doc:** *(extend existing reflection-patterns.html)*

**Implementation:** Each call to `expand` generates N candidate responses in parallel (tree expansion) and immediately scores each one (reflection); the conditional edge selects the highest-scoring branch to continue searching (backpropagation) and exits once the score threshold or depth limit is reached. In production, replace the scoring `llm.invoke` with a reward model or unit-test harness for faster, cheaper evaluation.

```python
from langgraph.graph import StateGraph, END
from langchain_anthropic import ChatAnthropic
from typing import TypedDict, Literal

llm = ChatAnthropic(model="claude-sonnet-5")
N_CANDIDATES = 3   # branching factor

class LATSState(TypedDict):
    task: str
    depth: int
    max_depth: int
    best_response: str
    best_score: float

def expand(state: LATSState) -> dict:
    # Tree expansion: generate N distinct candidates
    candidates = [
        llm.invoke(f"Task: {state['task']}\nAttempt {i + 1} — be thorough and distinct:").content
        for i in range(N_CANDIDATES)
    ]
    # Reflection: score each candidate
    scored: list[tuple[float, str]] = []
    for candidate in candidates:
        raw = llm.invoke(
            f"Rate this response quality 0.0–1.0 (reply with a number only).\n"
            f"Task: {state['task']}\nResponse: {candidate}"
        ).content.strip()
        try:
            scored.append((float(raw), candidate))
        except ValueError:
            scored.append((0.5, candidate))
    # Backpropagation: select the best-scoring branch
    best_score, best_response = max(scored)
    return {"best_response": best_response, "best_score": best_score, "depth": state["depth"] + 1}

def route(state: LATSState) -> Literal["expand", "__end__"]:
    # Stop when quality is sufficient or search budget is exhausted
    return END if state["best_score"] >= 0.9 or state["depth"] >= state["max_depth"] else "expand"

builder = StateGraph(LATSState)
builder.add_node("expand", expand)
builder.set_entry_point("expand")
builder.add_conditional_edges("expand", route)

app = builder.compile()
result = app.invoke({
    "task": "Explain what a transformer is to a backend software engineer.",
    "depth": 0, "max_depth": 2, "best_score": 0.0, "best_response": "",
})
print(f"Best (score={result['best_score']:.2f}, depth={result['depth']}):\n{result['best_response'][:300]}")
```

---

### Lesson 8: Planning Loops — When Structure Beats Improvisation
**File:** `lessons/0008-planning-loops.html`
**Key concepts:** planning vs. chaining · dynamic task decomposition · plan → validate → execute · rate-limit adaptation example · when planning adds value
**Source paper:** Andrew Ng — "Agentic Design Patterns Part 4: Planning"; Chip Huyen — planning as search with heuristics/AI judges
**Skill:** Given two task descriptions — one with predictable steps (scrape a list of URLs) and one with unpredictable steps (research a company and write a competitive analysis) — classify each as "workflow" or "planning loop" and explain why; then sketch the plan validation step for the second task
**Reference doc:** `reference/planning-patterns.html`

**Implementation:** The `plan → validate` edge adds a gate before any execution starts; if validation fails, the graph re-enters `plan` with the failure reason as context (replanning). Once valid, `execute_step` runs one step at a time and loops back to itself until the plan is exhausted — this inner for-loop is what separates a planning agent from a single `ReAct` call.

```python
from langgraph.graph import StateGraph, END
from langchain_anthropic import ChatAnthropic
from typing import TypedDict, Literal
import json

llm = ChatAnthropic(model="claude-sonnet-5")

class PlanState(TypedDict):
    task: str
    plan: list[str]
    step_index: int
    results: list[str]
    valid: bool
    done: bool

def plan(state: PlanState) -> dict:
    context = f"\nPrevious plan was rejected. Revise it." if state["plan"] else ""
    response = llm.invoke(
        f"Decompose this task into ordered steps. Return a JSON array of strings.{context}\nTask: {state['task']}"
    )
    try:
        steps = json.loads(response.content)
    except Exception:
        steps = [response.content]
    return {"plan": steps, "step_index": 0, "results": []}

def validate(state: PlanState) -> dict:
    response = llm.invoke(
        f"Does this plan fully cover the task without missing steps? Reply 'VALID' or 'INVALID: <reason>'.\n"
        f"Task: {state['task']}\nPlan: {state['plan']}"
    )
    return {"valid": response.content.strip().startswith("VALID")}

def execute_step(state: PlanState) -> dict:
    step = state["plan"][state["step_index"]]
    result = llm.invoke(
        f"Execute this step: {step}\nContext from prior steps: {state['results']}"
    ).content
    new_index = state["step_index"] + 1
    return {
        "results": state["results"] + [result],
        "step_index": new_index,
        "done": new_index >= len(state["plan"]),
    }

def route_after_validate(state: PlanState) -> Literal["plan", "execute_step"]:
    return "plan" if not state["valid"] else "execute_step"

def route_after_execute(state: PlanState) -> Literal["execute_step", "__end__"]:
    return END if state["done"] else "execute_step"

builder = StateGraph(PlanState)
builder.add_node("plan", plan)
builder.add_node("validate", validate)
builder.add_node("execute_step", execute_step)
builder.set_entry_point("plan")
builder.add_edge("plan", "validate")
builder.add_conditional_edges("validate", route_after_validate)
builder.add_conditional_edges("execute_step", route_after_execute)

app = builder.compile()
result = app.invoke({"task": "Research and summarize recent advances in LLM efficiency.", "plan": [], "step_index": 0, "results": [], "valid": False, "done": False})
print(f"Completed {len(result['results'])} steps.")
```

---

### Lesson 9: State Management and Control Flow
**File:** `lessons/0009-state-management.html`
**Key concepts:** sequential · parallel · routing (if-statement) · for-loop control flows · plan metrics (valid plan ratio, plans-per-valid-plan, invalid tool call frequency) · decoupling generate from execute
**Source paper:** Chip Huyen — "Agents" (2025-01), control flow section; evaluation with (task, tool inventory) datasets
**Skill:** Map the four control flow types (sequential, parallel, if-statement, for-loop) onto four real pipeline tasks you know — then write the metric you would track for each to measure loop quality
**Reference doc:** *(extend existing planning-patterns.html)*

**Implementation:** LangGraph supports all four control flow types natively: sequential is just a chain of `.add_edge` calls; parallel fan-out uses `Send` to dispatch the same node with different inputs simultaneously (results merge via `Annotated[list, operator.add]`); routing uses `add_conditional_edges`; a for-loop is a node with a conditional self-edge that increments an index each cycle. The four patterns are shown separately below so you can copy-paste the one that fits your pipeline stage.

```python
from langgraph.graph import StateGraph, END
from langgraph.types import Send
from typing import TypedDict, Annotated, Literal
import operator

# ── 1. Sequential: A → B → C ─────────────────────────────────────────────────
class SeqState(TypedDict):
    value: str

def seq_a(s: SeqState) -> dict: return {"value": f"A({s['value']})"}
def seq_b(s: SeqState) -> dict: return {"value": f"B({s['value']})"}
def seq_c(s: SeqState) -> dict: return {"value": f"C({s['value']})"}

seq = StateGraph(SeqState)
for fn in [seq_a, seq_b, seq_c]:
    seq.add_node(fn.__name__, fn)
seq.set_entry_point("seq_a")
seq.add_edge("seq_a", "seq_b")
seq.add_edge("seq_b", "seq_c")
seq.add_edge("seq_c", END)

# ── 2. Parallel fan-out via Send ──────────────────────────────────────────────
class ParState(TypedDict):
    items: list[str]
    results: Annotated[list, operator.add]

def fan_out(state: ParState) -> list[Send]:
    return [Send("worker", {"item": item, "items": [], "results": []}) for item in state["items"]]

def worker(state: ParState) -> dict:
    return {"results": [f"processed({state['item']})"]}   # type: ignore[index]

par = StateGraph(ParState)
par.add_node("fan_out_node", lambda s: s)
par.add_node("worker", worker)
par.set_entry_point("fan_out_node")
par.add_conditional_edges("fan_out_node", fan_out)  # Send triggers parallel dispatch
par.add_edge("worker", END)

# ── 3. Routing (if-statement) ─────────────────────────────────────────────────
class RouteState(TypedDict):
    input: str
    category: str

def classify(state: RouteState) -> dict:
    return {"category": "short" if len(state["input"]) < 20 else "long"}

def handle_short(state: RouteState) -> dict: return {"input": f"brief({state['input']})"}
def handle_long(state: RouteState) -> dict: return {"input": f"detailed({state['input']})"}

def router(state: RouteState) -> Literal["handle_short", "handle_long"]:
    return "handle_short" if state["category"] == "short" else "handle_long"

route = StateGraph(RouteState)
route.add_node("classify", classify)
route.add_node("handle_short", handle_short)
route.add_node("handle_long", handle_long)
route.set_entry_point("classify")
route.add_conditional_edges("classify", router)
route.add_edge("handle_short", END)
route.add_edge("handle_long", END)

# ── 4. For-loop (self-edge with index) ────────────────────────────────────────
class LoopState(TypedDict):
    items: list[str]
    idx: int
    results: list[str]

def process(state: LoopState) -> dict:
    item = state["items"][state["idx"]]
    return {"results": state["results"] + [f"done({item})"], "idx": state["idx"] + 1}

def keep_going(state: LoopState) -> Literal["process", "__end__"]:
    return END if state["idx"] >= len(state["items"]) else "process"

loop = StateGraph(LoopState)
loop.add_node("process", process)
loop.set_entry_point("process")
loop.add_conditional_edges("process", keep_going)

# Compile and run any of the above:
# seq_app = seq.compile(); seq_app.invoke({"value": "start"})
# par_app = par.compile(); par_app.invoke({"items": ["a", "b", "c"], "results": []})
# route_app = route.compile(); route_app.invoke({"input": "hello world", "category": ""})
# loop_app = loop.compile(); loop_app.invoke({"items": ["x", "y", "z"], "idx": 0, "results": []})
```

---

## Module 4 — Failure Modes and Robust Design
*Turn from architecture to robustness — naming failure modes, understanding structural challenges, and internalizing the design principles that hold across all loop types.*

### Lesson 10: Taxonomy of Agentic Loop Failures
**File:** `lessons/0010-failure-taxonomy.html`
**Key concepts:** planning failures (invalid tools, wrong params, goal failures, reflection errors) · tool failures (incorrect output, translation errors, missing tools) · efficiency failures (excess steps, missed parallelism)
**Source paper:** Chip Huyen — "Agents" (2025-01), failure taxonomy section; three categories with mitigations
**Skill:** Read a short agent transcript (provided in the lesson) and annotate each failure point with its category from the taxonomy; then propose one mitigation for the most severe failure
**Reference doc:** `reference/failure-modes.html`

**Implementation:** Wrap every tool execution in try/except and emit structured failure records capturing `type` (planning / tool / efficiency), `subtype`, and a `mitigation` hint; tracking `error_count` in state lets the routing function detect runaway loops (efficiency failure) and hard-stop before consuming further tokens. Inspect `result["failures"]` after a run to get a per-execution failure audit trail.

```python
from langgraph.graph import StateGraph, END
from langchain_anthropic import ChatAnthropic
from langchain_core.messages import HumanMessage, BaseMessage
from langchain_core.tools import tool
from langgraph.prebuilt import ToolNode
from typing import TypedDict, Annotated, Literal
import operator

llm = ChatAnthropic(model="claude-sonnet-5")

@tool
def unreliable_search(query: str) -> str:
    """Search for information — may raise on bad queries."""
    if not query or len(query) < 3:
        raise ValueError(f"Query too short: {query!r}")
    return f"[results for '{query}']"

tools = [unreliable_search]

class DebugState(TypedDict):
    messages: Annotated[list[BaseMessage], operator.add]
    failures: list[dict]
    error_count: int

def agent(state: DebugState) -> dict:
    return {"messages": [llm.bind_tools(tools).invoke(state["messages"])]}

def safe_tools(state: DebugState) -> dict:
    last = state["messages"][-1]
    results, failures, error_count = [], list(state["failures"]), state["error_count"]
    for tc in getattr(last, "tool_calls", []):
        try:
            output = unreliable_search.invoke(tc["args"])
            results.append({"role": "tool", "content": output, "tool_call_id": tc["id"]})
        except Exception as exc:
            error_count += 1
            failures.append({
                "type": "tool_failure",
                "subtype": "incorrect_output",
                "tool": tc["name"],
                "args": tc["args"],
                "error": str(exc),
                "mitigation": "sanitise input before passing to tool",
            })
            results.append({"role": "tool", "content": f"ERROR: {exc}", "tool_call_id": tc["id"]})
    return {"messages": results, "failures": failures, "error_count": error_count}

def route(state: DebugState) -> Literal["safe_tools", "__end__"]:
    if state["error_count"] >= 3:    # efficiency failure: runaway error loop
        return END
    last = state["messages"][-1]
    return "safe_tools" if getattr(last, "tool_calls", None) else END

builder = StateGraph(DebugState)
builder.add_node("agent", agent)
builder.add_node("safe_tools", safe_tools)
builder.set_entry_point("agent")
builder.add_conditional_edges("agent", route)
builder.add_edge("safe_tools", "agent")

app = builder.compile()
result = app.invoke({"messages": [HumanMessage("Search for AI news")], "failures": [], "error_count": 0})
print(f"Failures logged: {result['failures']}")
```

---

### Lesson 11: Three Hard Structural Problems in Loop Design
**File:** `lessons/0011-hard-problems.html`
**Key concepts:** finite context limits · long-horizon planning degradation · natural language interface fragility · checkpointing · replanning triggers · structured output formats
**Source paper:** Lilian Weng — "LLM Powered Autonomous Agents" (2023), challenges section; each problem with architectural mitigation
**Skill:** For each of the three hard problems, write one concrete system design choice (e.g. "use a vector store for long-term memory to work around finite context") and explain how it addresses the root cause rather than just the symptom
**Reference doc:** *(extend existing agent-components.html)*

**Implementation:** Three mitigations map directly to three problems: (1) compress old messages into a `summary` field when the message list grows — this is cheaper and lossier than a vector store but sufficient for most tasks; (2) insert a replan check every N steps to catch long-horizon drift before the error compounds; (3) use `llm.with_structured_output(PydanticModel)` to replace free-form string parsing with validated, typed objects — the model retries automatically on schema mismatch. All three are shown as drop-in patterns below.

```python
from langgraph.graph import StateGraph, END
from langgraph.checkpoint.memory import MemorySaver
from langchain_anthropic import ChatAnthropic
from langchain_core.messages import HumanMessage, SystemMessage, BaseMessage
from pydantic import BaseModel
from typing import TypedDict, Annotated
import operator

llm = ChatAnthropic(model="claude-sonnet-5")

# ── Mitigation 1: finite context — rolling summary ────────────────────────────
class SummaryState(TypedDict):
    messages: Annotated[list[BaseMessage], operator.add]
    summary: str   # compressed long-term memory

def maybe_summarize(state: SummaryState) -> dict:
    if len(state["messages"]) > 10:
        compressed = llm.invoke(
            [HumanMessage(f"Summarize this conversation history in 3 bullet points:\n{state['messages'][:-4]}")]
        ).content
        return {"summary": compressed, "messages": state["messages"][-4:]}  # keep last 4 messages
    return {}

def respond(state: SummaryState) -> dict:
    context = [SystemMessage(f"Prior context:\n{state['summary']}")] if state["summary"] else []
    return {"messages": [llm.invoke(context + state["messages"])]}

# ── Mitigation 2: long-horizon degradation — periodic replan ─────────────────
class LongPlanState(TypedDict):
    task: str
    plan: list[str]
    step: int

def check_and_replan(state: LongPlanState) -> dict:
    if state["step"] > 0 and state["step"] % 5 == 0:
        verdict = llm.invoke(
            f"Is this plan still valid at step {state['step']}? Reply YES or NO.\n"
            f"Task: {state['task']}\nPlan: {state['plan']}"
        ).content.strip()
        if verdict.startswith("NO"):
            new_plan_text = llm.invoke(f"Replan from step {state['step']}: {state['task']}").content
            return {"plan": new_plan_text.split("\n")}
    return {}

# ── Mitigation 3: NL fragility — structured output via Pydantic ───────────────
class NextStep(BaseModel):
    action: str
    tool: str
    args: dict
    rationale: str

structured_llm = llm.with_structured_output(NextStep)

def plan_next(task: str, context: str) -> NextStep:
    # Returns a validated NextStep object — never a free-form string to parse
    return structured_llm.invoke(f"Plan the next step.\nTask: {task}\nContext: {context}")

# ── Wire up with checkpointing so restarts resume mid-task ───────────────────
checkpointer = MemorySaver()
builder = StateGraph(SummaryState)
builder.add_node("maybe_summarize", maybe_summarize)
builder.add_node("respond", respond)
builder.set_entry_point("maybe_summarize")
builder.add_edge("maybe_summarize", "respond")
builder.add_edge("respond", END)

app = builder.compile(checkpointer=checkpointer)
config = {"configurable": {"thread_id": "long-task-1"}}
# State is persisted at every node — any interrupt restores full context on resume
app.invoke({"messages": [HumanMessage("Start the analysis.")], "summary": ""}, config=config)
```

---

### Lesson 12: Design Principles + The Meta-Loop
**File:** `lessons/0012-design-principles.html`
**Key concepts:** simplicity first · transparency (explicit planning steps) · tool investment · trajectory storage · meta-loop (loops that improve loops) · fine-tuning from trajectories · LATS distillation
**Source paper:** Anthropic — "Building Effective Agents" design principles; LangChain — trajectory storage as fine-tuning mechanism
**Skill:** Audit a hypothetical agent system description against the three Anthropic principles (simplicity, transparency, tool investment); then describe how you would close the meta-loop — what trajectory data you would capture, how you would label it, and what improvement signal it would provide
**Reference doc:** *(extend existing reflection-patterns.html)*

**Implementation:** Start with the simplest possible graph (one LLM node); add an explicit logging node that appends every step to a JSONL file — this satisfies the transparency principle and produces the trajectory corpus needed for the meta-loop. Label accepted runs as positive training examples and feed them to a fine-tuning run; the model that generated the trajectories becomes the training signal for its own successor, closing the loop.

```python
from langgraph.graph import StateGraph, END
from langchain_anthropic import ChatAnthropic
from langchain_core.tools import tool
from langchain_core.messages import HumanMessage, BaseMessage
from langgraph.prebuilt import ToolNode
from typing import TypedDict, Annotated
import operator, json
from pathlib import Path

llm = ChatAnthropic(model="claude-sonnet-5")
TRAJECTORY_LOG = Path("trajectories.jsonl")

# ── Principle 1: Simplicity — one node, no premature orchestration ────────────
# Add complexity (reflection, planning) only when you have evidence it's needed.

# ── Principle 2: Transparency — log every step explicitly ────────────────────
class AgentState(TypedDict):
    task: str
    messages: Annotated[list[BaseMessage], operator.add]
    steps: list[dict]   # audit trail
    accepted: bool      # human labels this after reviewing the output

def call_llm(state: AgentState) -> dict:
    response = llm.invoke(state["messages"])
    step = {"node": "call_llm", "input": state["messages"][-1].content, "output": response.content}
    return {"messages": [response], "steps": state["steps"] + [step]}

def log_trajectory(state: AgentState) -> dict:
    # Append the full trajectory — this is the raw material for the meta-loop
    entry = {"task": state["task"], "steps": state["steps"], "accepted": state["accepted"]}
    with open(TRAJECTORY_LOG, "a") as f:
        f.write(json.dumps(entry) + "\n")
    return {}

# ── Principle 3: Tool investment — document tools so the model plans correctly
@tool
def lookup_kb(query: str) -> str:
    """Search the internal knowledge base. Use before answering any factual question.
    Returns up to 5 relevant excerpts from stored notes."""
    return f"[KB results for '{query}']"

# ── Meta-loop: convert logged trajectories into fine-tuning examples ──────────
def build_training_example(trajectory: dict) -> dict | None:
    """Convert an accepted trajectory into a supervised fine-tuning example."""
    if not trajectory.get("accepted"):
        return None   # only learn from accepted runs
    last_step = trajectory["steps"][-1]
    return {
        "messages": [
            {"role": "user",      "content": trajectory["task"]},
            {"role": "assistant", "content": last_step["output"]},
        ],
        "label": "positive",
    }

builder = StateGraph(AgentState)
builder.add_node("call_llm", call_llm)
builder.add_node("log_trajectory", log_trajectory)
builder.set_entry_point("call_llm")
builder.add_edge("call_llm", "log_trajectory")
builder.add_edge("log_trajectory", END)

app = builder.compile()
result = app.invoke({
    "task": "Summarise key ideas in transformer architectures.",
    "messages": [HumanMessage("Summarise key ideas in transformer architectures.")],
    "steps": [],
    "accepted": False,   # set to True after human review to include in fine-tuning
})
# After labelling: replay trajectories.jsonl through build_training_example()
# and submit the positive examples to your fine-tuning pipeline.
```

---

## Suggested Teaching Order

Work through modules in sequence: Module 1 establishes vocabulary (OODA, anatomy, empirical motivation), Module 2 adds the human oversight dimension before you need it in practice, Module 3 surveys the main loop patterns in order of complexity (reflection → advanced reflection → planning → state management), and Module 4 closes by systematically stress-testing what you've built. Don't skip ahead to Module 3 without Module 1 — the pattern vocabulary makes the failure taxonomy in Module 4 much clearer.

---

## Reference Documents to Build

| File | Contents |
|------|----------|
| `reference/ooda-loop.html` | OODA cycle diagram, Boyd's tempo principle, cross-domain applications |
| `reference/agentic-performance-data.html` | HumanEval benchmark numbers, zero-shot vs. agent loop comparison table |
| `reference/agent-components.html` | Planning/Memory/Tool Use taxonomy, ReAct cycle, context limit challenge |
| `reference/hitl-spectrum.html` | Three-tier HITL model, placement criteria, per-action autonomy matrix |
| `reference/reflection-patterns.html` | Basic Reflection, Reflexion, LATS — architecture diagrams and trade-offs |
| `reference/planning-patterns.html` | Planning vs. chaining decision criteria, control flow types, plan quality metrics |
| `reference/failure-modes.html` | Full failure taxonomy with mitigations (planning / tool / efficiency) |
