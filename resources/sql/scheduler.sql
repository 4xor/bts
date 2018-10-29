-- :name find-all :? :*
-- :doc Find all schedulers records
select * from scheduler order by name;

-- :name find-one :? :1
-- :doc Find one schedulers records
select * from scheduler where name = :name;

-- :name start-execute-job :<!
-- :doc Start Execution Job
update scheduler
set
    status = 'work',
    last_start_at = now(),
    last_complete_at = null,
    last_result = null
where name = :name returning *

-- :name complete-execute-job :! :n
-- :doc  Complete of job execution
WITH upd AS (
  update scheduler
  set
      status = :status,
      last_complete_at = now(),
      last_result = :result
  where name = :name RETURNING *
)
insert into scheduler_history(scheduler_name, start_at, complete_at, result)
SELECT s.name, s.last_start_at, s.last_complete_at, s.last_result FROM upd s

-- :name insert-scheduler :<!
insert into scheduler(name, action_name, params, description, cron, status)
values (:name, :action_name, :params, :description, :cron, 'idle')
returning *

  -- :name update-scheduler :<!
update scheduler
set
  action_name = :action_name,
  params = :params,
  description = :description,
  cron = :cron
where name = :name RETURNING *


-- :name delete-scheduler :! :n
delete from scheduler where name = :name