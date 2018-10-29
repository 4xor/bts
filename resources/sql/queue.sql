-- The Task Queue Storage

-- :name insert-task :<!
-- :doc Insert new task in queue and return id
insert into queue(task_type, params) values(:type, :params) returning id

-- :name take-task :<!
-- :doc lock one task for execute it return id & params
update queue
set start_at = now()
where id = (
  select id from queue
  where task_type = :type and start_at is null and complete_at is null
  order by create_at
  for update skip locked
  limit 1
)
returning id, params

-- :name complete-task :! :n
-- :doc complete task execution
update queue
set complete_at = now(), result = :result
where id = :id

-- :name get-task :? :1
-- :doc get task item
select * from queue where id = :id

-- :name waiting-count :? :1
select count(*) as cnt from queue
where task_type = :type and start_at is null and complete_at is null 


-- :name delete-completed :! :n
delete from queue where complete_at is not null