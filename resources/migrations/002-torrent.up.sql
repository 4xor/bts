create table torrent (
  id bigserial not null primary key,
  source varchar(20) not null,
  source_id varchar(10) not null,
  torrent_type smallint not null,
  magnet text not null,
  size bigint not null,
  name text not null,
  tags text[] not null,
  create_at timestamp with time zone not null default now(),
  seeders int,
  leechers int,
  seaders_leechers_last_update timestamp with time zone, 
  constraint source_uidx unique(source, source_id)
);
--;
create index torrent_name_idx on torrent ((lower(name)));
--;
create index torrent_tags_idx on torrent (tags);
