create table t_admin
(
    admin_id    int auto_increment
        primary key,
    username    varchar(50)                        not null,
    password    varchar(100)                       not null,
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null,
    constraint username
        unique (username)
);

INSERT INTO doctor.t_admin (admin_id, username, password, create_time, update_time) VALUES (1, 'admin', '123456', '2025-04-03 16:13:14', '2025-04-03 16:13:15');
