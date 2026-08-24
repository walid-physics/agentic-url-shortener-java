# Testing approach

## Objectives

The test suite prioritizes governance behavior and externally observable API behavior. The most important
question is not whether an agent produced plausible prose; it is whether the Java control plane enforced the
correct state transition, dependency, approval, and safety rule.

## Test levels

| Level | Current coverage | Purpose |
| --- | --- | --- |
| Schema test | `AgentSchemasTest` | Ensures every agent schema is valid JSON and rejects undeclared properties |
| Tool security test | `SafeWorkspaceToolTest` | Verifies traversal and unauthorized operations are rejected |
| Workflow integration test | `WorkflowApiIntegrationTest` | Executes the DAG, verifies approval pause/resume, audit events, decisions, and metrics |
| Product integration test | `ShortUrlApiIntegrationTest` | Verifies creation, redirect, click analytics, deletion, scheme rejection, and HTTP 410 expiration |
| Container build | `Dockerfile` | Builds with Java 21 and runs the Maven test lifecycle before producing the runtime image |
| Continuous integration | `.github/workflows/ci.yml` | Runs `mvn verify` on every push and pull request using Java 21 |

## Behavior coverage matrix

| Behavior | Evidence |
| --- | --- |
| Explicit graph dependencies | Review runs only after test and security tasks pass |
| Parallel branches | Test and security tasks become ready in the same scheduler iteration |
| Human governance | Implementation becomes `WAITING_APPROVAL` and cannot execute before approval |
| Auditability | Ordered events include hashes, attempt numbers, duration, and transition details |
| Decision lineage | Agent decisions are stored independently with source, rationale, and timestamp |
| URL safety | Only HTTP and HTTPS destinations are accepted |
| Expiration semantics | Expired codes return `410 Gone` |
| Workspace containment | `../../outside.txt` raises `SecurityException` |
| Command safety | Maven arguments are fixed Java strings and are never built from model output |

## Running tests

```bash
mvn clean verify
```

- `clean` removes prior compiled output.
- `verify` runs compilation, unit tests, integration tests, and all earlier Maven lifecycle checks.

For a reproducible Java 21 container build:

```bash
docker compose build --no-cache
```

- `build` creates the application image defined by the Dockerfile.
- `--no-cache` forces every build layer to run again, including Maven tests, rather than reusing an older
  successful layer.

## AI-mode testing

Deterministic mode is used for CI because it is stable and requires no secret. AI mode should be evaluated
with a separate, manually triggered suite that checks:

1. Schema adherence rate.
2. Task success and retry rate.
3. Approval frequency.
4. Unsafe path and operation rejection.
5. Latency and token cost.
6. Consistency across repeated runs of the same requirement.

API keys should remain in CI secrets and must never appear in source, test fixtures, logs, or stored agent
artifacts.

## Current validation status

The repository contains executable JUnit and integration tests. The final archive was structurally checked
for valid Maven XML, YAML, source hygiene, and archive integrity. The environment used to assemble the
submission did not contain Maven, a Java compiler, or Docker, so the GitHub Actions run should be treated as
the authoritative compile-and-test result after the repository is pushed.
