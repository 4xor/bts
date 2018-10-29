-- :name stats-tasks :? :*
-- :todo rewrite, performance check
-- :doc Task complete/wait statistics
select s.task_type, sum(waiting_count) as waiting_count, sum(complete_count) as complete_count
from
     (select task_type, count(*) as waiting_count, null as complete_count
      from queue
      where complete_at is null
      group by task_type
      union all
      select task_type, null as waiting_count, count(*) as complete_count
      from queue
      where complete_at is not null
      group by task_type) as s
group by s.task_type

-- :name stats-torrents :? :*
select source, count(*) as cnt from torrent
group by source;