-- Apply after the base schema has created the users table.
-- Adds account fields only when they are missing, then creates refresh-token storage.
DELIMITER $$

DROP PROCEDURE IF EXISTS ensure_auth_user_columns$$
CREATE PROCEDURE ensure_auth_user_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'users'
          AND column_name = 'must_change_password'
    ) THEN
        ALTER TABLE users
            ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'users'
          AND column_name = 'temp_password_expires_at'
    ) THEN
        ALTER TABLE users ADD COLUMN temp_password_expires_at DATETIME NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'users'
          AND column_name = 'password_changed_at'
    ) THEN
        ALTER TABLE users ADD COLUMN password_changed_at DATETIME NULL;
    END IF;
END$$

CALL ensure_auth_user_columns()$$
DROP PROCEDURE ensure_auth_user_columns$$

DELIMITER ;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    refresh_token_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    refresh_token CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (refresh_token_id),
    UNIQUE KEY uq_refresh_tokens_token (refresh_token),
    KEY idx_refresh_tokens_user_active (user_id, revoked_at, expires_at),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;
