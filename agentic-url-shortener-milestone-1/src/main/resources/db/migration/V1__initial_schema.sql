create table workflows (
    id uuid primary key,
    requirement varchar(12000) not null,
    status varchar(40) not null,
    created_at timestamp with time zone not null,
    started_at timestamp with time zone,
    completed_at timestamp with time zone,
    version bigint not null
);

create table workflow_tasks (
    id uuid primary key,
    workflow_id uuid not null,
    sequence_number integer not null,
    task_key varchar(100) not null,
    name varchar(240) not null,
    agent_type varchar(40) not null,
    status varchar(40) not null,
    risk_level varchar(20) not null,
    retry_count integer not null,
    max_retries integer not null,
    approval_required boolean not null,
    approved boolean not null,
    failure_reason varchar(4000),
    output_artifact varchar(100000),
    started_at timestamp with time zone,
    completed_at timestamp with time zone,
    constraint fk_task_workflow foreign key (workflow_id) references workflows(id),
    constraint uk_workflow_task_key unique (workflow_id, task_key)
);

create table workflow_task_dependencies (
    task_id uuid not null,
    dependency_key varchar(100) not null,
    primary key (task_id, dependency_key),
    constraint fk_dependency_task foreign key (task_id) references workflow_tasks(id)
);

create table workflow_events (
    id uuid primary key,
    workflow_id uuid not null,
    task_key varchar(100),
    event_type varchar(50) not null,
    occurred_at timestamp with time zone not null,
    attempt integer not null,
    duration_ms bigint not null,
    input_hash varchar(64),
    output_hash varchar(64),
    details varchar(8000)
);
create index idx_event_workflow_time on workflow_events(workflow_id, occurred_at);

create table decision_records (
    id uuid primary key,
    workflow_id uuid not null,
    task_key varchar(100) not null,
    source_agent varchar(40) not null,
    decision varchar(4000) not null,
    rationale varchar(4000) not null,
    created_at timestamp with time zone not null
);
create index idx_decision_workflow_time on decision_records(workflow_id, created_at);

create table short_urls (
    id uuid primary key,
    code varchar(16) not null unique,
    original_url varchar(4000) not null,
    created_at timestamp with time zone not null,
    expires_at timestamp with time zone,
    click_count bigint not null,
    last_accessed_at timestamp with time zone,
    active boolean not null,
    version bigint not null
);
