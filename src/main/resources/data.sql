-- ADMIN
insert into app_user (id, username, password, role, unit)
values (1, 'sg@admin.com', '$2a$10$7nlrdq8b7WjbYgLjUoKcuuTQOt4JaVJEa/raBeXHop6hdRCg3XPMu', 'ADMIN', null);

-- RESIDENTS
insert into app_user (id, username, password, role, unit)
values (2, 'john.murphy@gmail.com', '$2a$10$UeztABapyv7jcnyG9tZYee6z7xkO6Y.dmf.mqMVHQUbR.mzXpxOlK', 'RESIDENT', 'Apt 12');

insert into app_user (id, username, password, role, unit)
values (3, 'sarah.obrien@outlook.com', '$2a$10$UeztABapyv7jcnyG9tZYee6z7xkO6Y.dmf.mqMVHQUbR.mzXpxOlK', 'RESIDENT', 'Apt 3');

insert into app_user (id, username, password, role, unit)
values (4, 'michael.walsh@yahoo.com', '$2a$10$UeztABapyv7jcnyG9tZYee6z7xkO6Y.dmf.mqMVHQUbR.mzXpxOlK', 'RESIDENT', 'Apt 8');

insert into app_user (id, username, password, role, unit)
values (5, 'emma.byrne@hotmail.com', '$2a$10$UeztABapyv7jcnyG9tZYee6z7xkO6Y.dmf.mqMVHQUbR.mzXpxOlK', 'RESIDENT', 'Apt 5');

insert into app_user (id, username, password, role, unit)
values (6, 'david.kelly@icloud.com', '$2a$10$UeztABapyv7jcnyG9tZYee6z7xkO6Y.dmf.mqMVHQUbR.mzXpxOlK', 'RESIDENT', 'Apt 14');

insert into app_user (id, username, password, role, unit)
values (7, 'olivia.smith@gmail.com', '$2a$10$UeztABapyv7jcnyG9tZYee6z7xkO6Y.dmf.mqMVHQUbR.mzXpxOlK', 'RESIDENT', 'Apt 2');

insert into app_user (id, username, password, role, unit)
values (8, 'liam.johnson@aol.com', '$2a$10$UeztABapyv7jcnyG9tZYee6z7xkO6Y.dmf.mqMVHQUbR.mzXpxOlK', 'RESIDENT', 'Apt 4');

-- STAFF
insert into app_user (id, username, password, role, unit)
values (9, 'Mark Doyle', '$2a$10$7nlrdq8b7WjbYgLjUoKcuuTQOt4JaVJEa/raBeXHop6hdRCg3XPMu', 'STAFF', null);

insert into app_user (id, username, password, role, unit)
values (10, 'Lisa Gallagher', '$2a$10$7nlrdq8b7WjbYgLjUoKcuuTQOt4JaVJEa/raBeXHop6hdRCg3XPMu', 'STAFF', null);

insert into app_user (id, username, password, role, unit)
values (11, 'Tom Brennan', '$2a$10$7nlrdq8b7WjbYgLjUoKcuuTQOt4JaVJEa/raBeXHop6hdRCg3XPMu', 'STAFF', null);



-- MAINTENANCE REQUESTS
insert into maintenance_request
(id, created_on, task, category, description, status, priority, unit, created_by_user_id, assigned_to_user_id)
values
    (1, '2026-02-10', 'Replace smoke detector', 'ELECTRICAL',
     'Smoke alarm is beeping / needs replacement', 'NEW', 'HIGH',
     'Apt 12', 2, 10);


insert into maintenance_request
(id, created_on, task, category, description, status, priority, unit, created_by_user_id, assigned_to_user_id)
values
    (2, '2026-02-09', 'Fix broken air vent', 'HEATING',
     'Vent is loose and rattling in living room', 'IN_PROGRESS', 'MEDIUM',
     'Apt 3', 3, 9);


insert into maintenance_request
(id, created_on, task, category, description, status, priority, unit, created_by_user_id, assigned_to_user_id)
values
    (3, '2026-02-08', 'Paint hallway wall', 'OTHER',
     'Small patch of paint peeling near door', 'CLOSED', 'LOW',
     'Apt 8', 4, 11);


insert into maintenance_request
(id, created_on, task, category, description, status, priority, unit, created_by_user_id, assigned_to_user_id)
values
    (4, '2026-02-11', 'Kitchen sink leak', 'PLUMBING',
     'Water leaking under the sink cabinet', 'NEW', 'HIGH',
     'Apt 5', 5, null);


insert into maintenance_request
(id, created_on, task, category, description, status, priority, unit, created_by_user_id, assigned_to_user_id)
values
    (5, '2026-02-07', 'Bathroom light flickering', 'ELECTRICAL',
     'Light flickers when switched on', 'IN_PROGRESS', 'MEDIUM',
     'Apt 14', 6, 10);


insert into maintenance_request
(id, created_on, task, category, description, status, priority, unit, created_by_user_id, assigned_to_user_id)
values
    (6, '2026-02-06', 'Radiator not heating', 'HEATING',
     'Bedroom radiator stays cold', 'NEW', 'MEDIUM',
     'Apt 12', 2, null);