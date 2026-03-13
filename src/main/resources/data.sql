insert into app_user (id, username, password, role, unit)
values (1, 'admin', '$2a$10$7nlrdq8b7WjbYgLjUoKcuuTQOt4JaVJEa/raBeXHop6hdRCg3XPMu', 'ADMIN', null);

insert into app_user (id, username, password, role, unit)
values (2, 'resident', '$2a$10$UeztABapyv7jcnyG9tZYee6z7xkO6Y.dmf.mqMVHQUbR.mzXpxOlK', 'RESIDENT', 'Apt 12');

insert into app_user (id, username, password, role, unit)
values (3, 'staff1', '$2a$10$7nlrdq8b7WjbYgLjUoKcuuTQOt4JaVJEa/raBeXHop6hdRCg3XPMu', 'STAFF', null);

insert into app_user (id, username, password, role, unit)
values (4, 'staff2', '$2a$10$7nlrdq8b7WjbYgLjUoKcuuTQOt4JaVJEa/raBeXHop6hdRCg3XPMu', 'STAFF', null);

insert into maintenance_request
(id, created_on, task, category, description, status, priority, unit, created_by_user_id, assigned_to_user_id)
values
    (1, '2026-02-10', 'Replace smoke detector', 'ELECTRICAL', 'Smoke alarm is beeping / needs replacement', 'NEW', 'HIGH', 'Apt 12', 2, null);

insert into maintenance_request
(id, created_on, task, category, description, status, priority, unit, created_by_user_id, assigned_to_user_id)
values
    (2, '2026-02-09', 'Fix broken air vent', 'HEATING', 'Vent is loose and rattling', 'IN_PROGRESS', 'MEDIUM', 'Apt 3', null, null);

insert into maintenance_request
(id, created_on, task, category, description, status, priority, unit, created_by_user_id, assigned_to_user_id)
values
    (3, '2026-02-08', 'Paint', 'OTHER', 'Small patch needed in hallway', 'CLOSED', 'LOW', 'Apt 8', null, null);