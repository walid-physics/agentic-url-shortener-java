# Technical decisions and trade-offs

This is a lightweight architecture decision log. It records why important choices were made, what was
rejected, and what the prototype gives up in return.

## ADR-001: Java owns orchestration state

**Decision:** Implement the DAG scheduler, state transitions, retry budget, approval rules, and safe stop in
Java rather than asking an LLM to coordinate them.

**Why:** Governance rules become deterministic, testable, and enforceable even when a model produces an
incorrect response.

**Trade-off:** More orchestration code must be maintained. The benefit is a clear control boundary and a
workflow that can run in offline demo mode.

## ADR-002: Fixed lifecycle graph with parallel validation branches

**Decision:** Use a persisted seven-stage graph with `tests` and `security` running in parallel and `review`
depending on both.

**Why:** It directly demonstrates dependency scheduling, parallelism, synchronization, and release gating.

**Trade-off:** The Planner Agent currently produces a plan artifact but does not replace the active graph.
Production-grade dynamic replanning would require cycle detection, plan approval, and artifact revision
lineage before activating a new graph.

## ADR-003: Strict structured AI outputs

**Decision:** Each agent has a Java output type and a strict JSON Schema sent through the OpenAI Responses
API.

**Why:** Free-form prose is difficult to validate and unsafe to feed directly into workflow state or tools.

**Trade-off:** Schemas must evolve alongside Java records. This is deliberate because schema changes should
be reviewed like API changes.

## ADR-004: Deterministic agents by default

**Decision:** Keep AI disabled by default and provide stable Java-generated agent outputs.

**Why:** Reviewers and CI can execute the complete orchestration path without credentials, network access,
model variability, or API cost.

**Trade-off:** Demo mode proves orchestration behavior rather than model quality. AI-mode evaluation should
be performed separately with a controlled prompt suite.

## ADR-005: Synchronous workflow REST execution

**Decision:** The `execute` endpoint waits for the currently runnable stages to finish.

**Why:** It keeps the assessment prototype easy to run, debug, and demonstrate.

**Trade-off:** Long AI calls occupy an HTTP request. A production version should place workflow execution on
a durable queue and expose polling or server-sent events.

## ADR-006: Bounded tools instead of arbitrary shell access

**Decision:** File changes are limited to `UPSERT` operations under one normalized workspace. Test execution
uses a fixed `ProcessBuilder` argument list.

**Why:** Model-generated shell commands introduce command-injection and destructive-operation risks.

**Trade-off:** Agents have less flexibility. Additional operations should be implemented as explicit,
individually authorized tools.

## ADR-007: H2 locally, PostgreSQL for deployment

**Decision:** Use an H2 PostgreSQL-compatibility profile for local tests and PostgreSQL in Docker Compose.

**Why:** H2 makes the initial reviewer experience fast; PostgreSQL represents the intended persistent store.

**Trade-off:** H2 cannot reproduce every PostgreSQL behavior. Database-sensitive queries should also be
covered with PostgreSQL Testcontainers before production use.

## ADR-008: Basic privacy-preserving analytics

**Decision:** Store aggregate click count and last-accessed timestamp, not IP addresses or user-agent data.

**Why:** It satisfies basic analytics while minimizing personal-data collection.

**Trade-off:** The service cannot provide geographic, device, or unique-visitor analysis.
