create table queue (
  id bigserial not null primary key,
  task_type varchar(20) not null,
  create_at timestamp with time zone not null default now(),
  complete_at timestamp with time zone,
  start_at timestamp with time zone,
  params text not null,
  result text
);

--;
 create index queue_task_type_idx on queue (task_type);
--;
create index queue_complete_at_idx on queue (complete_at);
--;
create index queue_create_at on queue (create_at);
