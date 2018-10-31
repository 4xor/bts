alter table torrent add column update_at timestamp with time zone not null default now();
--;
create index torrent_update_at_idx on torrent(update_at);
--;
update torrent set update_at = create_at;