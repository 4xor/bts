create table search_history(
  id bigserial not null primary key,
  create_at timestamp with time zone default now(),
  duration int not null,
  q text not null,
  skip int not null,
  sort_field text not null,
  sort_direction text not null,
  result_count int not null
);