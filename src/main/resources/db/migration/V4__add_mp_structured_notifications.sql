CREATE TABLE mp_scheduled_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id VARCHAR(100) UNIQUE,
    resource_id VARCHAR(100),
    topic VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    retry_count INT DEFAULT 0,
    last_error TEXT,
    next_attempt DATETIME,
    payload TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_mp_sched_status_retry (status, retry_count, next_attempt)
);
