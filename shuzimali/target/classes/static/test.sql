CREATE DATABASE IF NOT EXISTS shuzimali CHARACTER SET utf8mb4 COLLATE utf8mb4_da_0900_ai_ci;
USE shuzimali;
CREATE TABLE IF NOT EXISTS t_user(
    id  INT AUTO_INCREMENT PRIMARY KEY comment '用户id',
    email VARCHAR(255),
    password VARCHAR(255)
)