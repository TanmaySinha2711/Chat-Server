CREATE DATABASE IF NOT EXISTS prattle;
USE prattle;

CREATE TABLE IF NOT EXISTS Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(40) NOT NULL UNIQUE,
    password VARCHAR(255),
    parentalcontrol VARCHAR(45)
    );

CREATE TABLE IF NOT EXISTS Groups(
    id INT AUTO_INCREMENT PRIMARY KEY,
    groupname VARCHAR(40) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS Group_membership(
    user_id INT NOT NULL,
    group_id INT NOT NULL,
    PRIMARY KEY(group_id, user_id),
    FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE,
    FOREIGN KEY(group_id) REFERENCES Groups(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS Messages(
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sent_time TIMESTAMP,
    from_id INT NOT NULL,
    to_user_id INT DEFAULT NULL,
    to_group_id INT DEFAULT NULL,
    message_type VARCHAR(3) NOT NULL,
    text TEXT,
    media_link VARCHAR(260),
    received TINYINT(4),
    flagged TINYINT(4),
    sent_ip VARCHAR(45),
    received_ip VARCHAR(45),
    FOREIGN KEY(to_user_id) REFERENCES Users(id) ON DELETE CASCADE,
    FOREIGN KEY(to_group_id) REFERENCES Groups(id) ON DELETE CASCADE
    );
