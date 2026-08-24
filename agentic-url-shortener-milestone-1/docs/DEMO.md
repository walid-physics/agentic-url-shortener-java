# Interview demonstration

## 1. Greenfield workflow

Create: `Build a URL shortener with redirect, expiration, analytics, and deletion.`

Show that requirements, architecture, and planning pass; implementation pauses for approval; after approval,
tests and security run in parallel and review runs only after both pass.

## 2. Brownfield workflow

Create: `Change expired URL behavior from 404 to 410 and update tests and documentation.`

Discuss the architecture artifact's affected components and the decision records explaining why `410 Gone`
is more precise for a resource known to have expired.

## 3. Governance evidence

Open the workflow's `/events`, `/decisions`, and `/metrics` endpoints. Point out attempts, duration, hashes,
approval events, release-gate completion, end-to-end latency, and MTTR behavior after a retry.

## 4. Guardrail demonstration

Run `SafeWorkspaceToolTest` or explain its traversal case. The normalized target must begin with the managed
workspace root, so `../../outside.txt` is rejected before any write.

## 5. URL product demonstration

Create a URL, visit `/r/{code}`, verify the `302 Location` header, and read analytics. Then show that a
`javascript:` destination is rejected and an expired URL produces `410 Gone`.
