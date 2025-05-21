create table positions
(
    positions_id   int auto_increment
        primary key,
    positions_name varchar(100)                       null,
    create_time    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '字典数据表' charset = utf8mb4
                         row_format = DYNAMIC;

create index idx_dict_label
    on positions (positions_name);

INSERT INTO doctor.positions (positions_id, positions_name, create_time, update_time) VALUES (1, '住院医师', '2025-04-24 03:45:34', '2025-04-24 03:45:34');
INSERT INTO doctor.positions (positions_id, positions_name, create_time, update_time) VALUES (2, '主治医师', '2025-04-24 03:45:34', '2025-04-24 03:45:34');
INSERT INTO doctor.positions (positions_id, positions_name, create_time, update_time) VALUES (3, '副主任医师', '2025-04-24 03:45:34', '2025-04-24 03:45:34');
INSERT INTO doctor.positions (positions_id, positions_name, create_time, update_time) VALUES (4, '主任医师', '2025-04-24 03:45:34', '2025-04-24 03:45:34');
INSERT INTO doctor.positions (positions_id, positions_name, create_time, update_time) VALUES (5, '助理医师', '2025-04-24 03:45:34', '2025-04-24 03:45:34');
INSERT INTO doctor.positions (positions_id, positions_name, create_time, update_time) VALUES (14, '测试', '2025-04-27 13:04:01', '2025-05-08 10:44:13');
INSERT INTO doctor.positions (positions_id, positions_name, create_time, update_time) VALUES (15, '摆烂王者', '2025-05-07 10:53:29', '2025-05-07 10:53:29');
INSERT INTO doctor.positions (positions_id, positions_name, create_time, update_time) VALUES (16, '摆烂白银', '2025-05-07 10:53:38', '2025-05-07 10:53:38');
INSERT INTO doctor.positions (positions_id, positions_name, create_time, update_time) VALUES (19, '摆烂青铜', '2025-05-07 17:33:44', '2025-05-07 17:33:44');
