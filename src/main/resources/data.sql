insert into maintenance_request (created_on, task, category, description, status, priority, unit)
values ('2026-02-10', 'Replace smoke detector', 'ELECTRICAL', 'Smoke alarm is beeping / needs replacement', 'NEW','HIGH','Apt 12');

insert into maintenance_request (created_on, task, category, description, status, priority, unit)
values ('2026-02-09', 'Fix broken air vent','HEATING', 'Vent is loose and rattling', 'IN_PROGRESS','MEDIUM','Apt 3');

insert into maintenance_request (created_on, task, category, description, status, priority, unit)
values ('2026-02-08','Paint', 'OTHER', 'Small patch needed in hallway', 'CLOSED','LOW','Apt 8');

insert into app_user (username, password, role, unit)
values ('admin', '$2a$10$7nlrdq8b7WjbYgLjUoKcuuTQOt4JaVJEa/raBeXHop6hdRCg3XPMu', 'ADMIN', null);

insert into app_user (username, password, role, unit)
values ('resident', '$2a$10$UeztABapyv7jcnyG9tZYee6z7xkO6Y.dmf.mqMVHQUbR.mzXpxOlK', 'RESIDENT', 'Apt 12');