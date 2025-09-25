#用户表
create table if not exists t_user (
    id INT auto_increment  COMMENT '用户id',
    email varchar(255)  unique NOT NULL comment '邮箱',
    password VARCHAR(255) not null comment '密码',
    create_at DATETIME default current_timestamp COMMENT '注册时间',
    primary key (id)
);
# 聊天表
CREATE TABLE IF NOT EXISTS t_chat(
    chat_id bigint auto_increment comment '聊天记录唯一id',
    user_id int comment '对应用户',
    sender ENUM('user','ai') not null comment '消息发送方',
    message text not null comment '消息内容',
    create_at datetime default current_timestamp comment '消息时间',
    primary key (chat_id),
    foreign key (user_id) references t_user(id)
);
# 番茄表
create table if not exists t_tomato(
    focus_id bigint auto_increment comment '记录ID',
    user_id int comment '对应用户',
    date date not null comment '日期',
    duration_min int not null comment '专注时长（分钟）',
    task_desc varchar(255) comment '专注内容简述',
    primary key (focus_id),
    foreign key (user_id) references t_user(id)
);
#目标表
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_goal
-- ----------------------------
DROP TABLE IF EXISTS `t_goal`;
CREATE TABLE `t_goal`  (
                           `goal_id` int NOT NULL AUTO_INCREMENT COMMENT '目标ID',
                           `user_id` int NULL DEFAULT NULL COMMENT '用户ID',
                           `goal_type` enum('short','long') CHARACTER SET utf8mb4 COLLATE utf8mb4_da_0900_ai_ci NOT NULL COMMENT '短期/长期',
                           `goal_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_da_0900_ai_ci NOT NULL COMMENT '目标内容',
                           `create_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_da_0900_ai_ci NOT NULL COMMENT '标题',
                           `completed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否完成',
                           PRIMARY KEY (`goal_id`) USING BTREE,
                           INDEX `user_id`(`user_id` ASC) USING BTREE,
                           CONSTRAINT `t_goal_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_da_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
#复盘表
create table if not exists t_review(
    file_id bigint auto_increment comment '文件ID',
    user_id int comment '用户ID',
    file_name varchar(255) not null comment '文件名',
    file_path varchar(255) not null comment '文件路径',
    file_type varchar(50) comment '文件类型(pdf / jpg)',
    vector_id varchar(100) comment '向量ID',
    create_at datetime default current_timestamp comment '创建时间',
    primary key (file_id),
    foreign key (user_id) references t_user(id)
);
#课程表
create table if not exists t_course(
    course_id int auto_increment comment '课程ID',
    user_id int comment '用户ID',
    course_name varchar(255) not null comment '课程名称',
    status enum('ongoing','completed') not null comment '课程状态(进行中 / 已完成)',
    create_at datetime default current_timestamp comment '创建时间',
    primary key (course_id),
    foreign key (user_id) references t_user(id)
);
#考试表
create table if not exists t_exam(
    exam_id int auto_increment comment '考试ID',
    user_id int comment '用户ID',
    exam_name varchar(100) not null comment '考试/赛事名称',
    exam_data date not null comment '考试时间',
    reminder boolean default true comment '考试提醒',
    create_at datetime default current_timestamp comment '创建时间',
    primary key (exam_id),
    foreign key (user_id) references t_user(id)
);
#推荐视频表
create table if not exists t_video(
    video_id int auto_increment comment '视频ID',
    user_id int comment '用户ID',
    title varchar(255) not null comment '视频标题',
    platform varchar(50) comment '视频平台',
    link varchar(255) not null comment '视频链接',
    thumbnail varchar(255) comment '视频缩略图',
    create_at datetime default current_timestamp comment '创建时间',
    primary key (video_id),
    foreign key (user_id) references t_user(id)
);
#职业规划表
create table if not exists t_plan(
    plan_id int auto_increment comment '规划ID',
    user_id int comment '用户ID',
    direction enum('考公','考研','就业','出国') not null comment '规划方向',
    resume_path varchar(255) comment '简历路径',
    ai_feedback text comment 'AI反馈',
    create_at datetime default current_timestamp comment '创建时间',
    primary key (plan_id),
    foreign key (user_id) references t_user(id)
);
#用户设置表
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_setting
-- ----------------------------
DROP TABLE IF EXISTS `t_setting`;
CREATE TABLE `t_setting`  (
                              `setting_id` int NOT NULL AUTO_INCREMENT COMMENT '设置ID',
                              `user_id` int NULL DEFAULT NULL COMMENT '用户ID',
                              `personal_profile` char(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_da_0900_ai_ci NULL DEFAULT NULL,
                              `create_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `preferred_study_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_da_0900_ai_ci NULL DEFAULT NULL COMMENT '喜好学习时间',
                              `daily_focus_minute` int NULL DEFAULT NULL COMMENT '每天专注时间',
                              `weekly_available_days` json NULL,
                              `tone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_da_0900_ai_ci NULL DEFAULT NULL COMMENT '语句',
                              `response_length` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_da_0900_ai_ci NULL DEFAULT NULL COMMENT '回复长度',
                              `allow_emojis` tinyint(1) NULL DEFAULT NULL COMMENT '是否允许emoji',
                              `show_citations` tinyint(1) NULL DEFAULT NULL COMMENT '是否显示引证',
                              PRIMARY KEY (`setting_id`) USING BTREE,
                              INDEX `user_id`(`user_id` ASC) USING BTREE,
                              CONSTRAINT `t_setting_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_da_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;