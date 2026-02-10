insert into dummy (value, name) values (100, "Hello");
insert into dummy (value, name) values (200, "World");

insert into maintenance_request (status, priority, unit, created_at)
values ('NEW','HIGH','Apt 12','2026-02-10 10:00:00');

insert into maintenance_request (status, priority, unit, created_at)
values ('IN_PROGRESS','MEDIUM','Apt 3','2026-02-09 14:30:00');

insert into maintenance_request (status, priority, unit, created_at)
values ('CLOSED','LOW','Apt 8','2026-02-08 09:15:00');
