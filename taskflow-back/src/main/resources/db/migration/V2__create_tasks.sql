CREATE TABLE tasks (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    owner_id      BIGINT        NOT NULL,
    title         VARCHAR(120)  NOT NULL,
    description   VARCHAR(2000) NULL,
    priority      VARCHAR(10)   NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    due_date      DATE          NULL,
    reminder_at   DATETIME(6)   NULL,
    reminder_sent BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at    DATETIME(6)   NOT NULL,
    updated_at    DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tasks_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_tasks_owner_status ON tasks (owner_id, status);
CREATE INDEX idx_tasks_owner_due_date ON tasks (owner_id, due_date);
CREATE INDEX idx_tasks_reminder ON tasks (reminder_sent, reminder_at);
