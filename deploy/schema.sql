-- 새 데이터베이스 전용. 기존 DB에는 자동 적용하지 않습니다.
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS running_records (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    distance DOUBLE,
    running_time INT,
    pace INT,
    running_date DATE,
    content VARCHAR(500),
    CONSTRAINT fk_runs_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_runs_user_date (user_id, running_date, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
