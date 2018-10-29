create table scheduler (
  name varchar(64) not null primary key,
  action_name text not null,
  params text not null,
  description text not null,
  cron varchar(32) not null,
  status varchar(10) not null default 'idle',
  last_start_at timestamp with time zone,
  last_complete_at timestamp with time zone,
  last_result text
);
--;
create table scheduler_history (
  id serial not null primary key,
  scheduler_name varchar(64) not null references scheduler(name) on delete cascade,
  start_at timestamp with time zone not null,
  complete_at timestamp with time zone not null,
  result text not null
);