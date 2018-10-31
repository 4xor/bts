-- :name torrent-exists :? :1
-- :doc Check torrent exists in db
select (exists(select 1 from torrent where source = :source and source_id = :source_id) OR
        exists(select 1 from topic_failed where source = :source and source_id = :source_id)) as r


-- :name torrent-insert :! :*
-- :doc Insert torrent info
insert into torrent(torrent_type, source, source_id, magnet, size, name, tags)
values (:torrent_type, :source, :source_id, :magnet, :size, :name, :tags)
ON CONFLICT (source, source_id)
DO UPDATE
  SET
    tags = :tags,
    name = :name
    magnet = :magnet
    size = :size
RETURNING *;

-- :name update-seed-leech-info :! :n
-- :doc Update seeders and leechers info of torrent
update torrent
set
 seeders = :seeders, leechers = :leechers,
 seaders_leechers_last_update = now()
where id = :id

-- :name get-torrents-for-seed-update :? :*
-- :doc Return torrents for seed update
update torrent
set seaders_leechers_last_update = now()
where
 id in (
   select id from torrent
   where seaders_leechers_last_update is null or
   seaders_leechers_last_update <= NOW() - INTERVAL '120 minutes'
   limit 100
 )
 returning id, source, source_id;

-- :name register-failed-topic :! :n
insert into topic_failed(source, source_id, error_message)
values(:source, :source_id, :error)

-- :name get-torrents-for-update :? :*
update torrent
set update_at = now()
where id in (
  select id from torrent
  where update_at <= NOW() - INTERVAL '30 minutes'
  limit 1000
)
returning id, source, source_id, torrent_type;