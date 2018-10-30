create table topic_failed(
  id serial not null primary key,
  source varchar(20) not null,
  source_id varchar(10) not null,
  error_message text not null
);
--;
create index topic_failed_idx on topic_failed (source, source_id);
