CREATE TABLE departments (
    dept_id INT NOT NULL AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (dept_id),
    UNIQUE KEY uk_departments_dept_name (dept_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE positions (
    position_id INT NOT NULL AUTO_INCREMENT,
    position_name VARCHAR(100) NOT NULL,
    rank_level INT NOT NULL,
    PRIMARY KEY (position_id),
    UNIQUE KEY uk_positions_position_name (position_name),
    CONSTRAINT chk_positions_rank_level CHECK (rank_level >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE users (
    user_id INT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    dept_id INT NOT NULL,
    position_id INT NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_user_name (user_name),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_dept_id (dept_id),
    KEY idx_users_position_id (position_id),
    CONSTRAINT fk_users_department
        FOREIGN KEY (dept_id) REFERENCES departments (dept_id),
    CONSTRAINT fk_users_position
        FOREIGN KEY (position_id) REFERENCES positions (position_id),
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'APPROVER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE expense_categories (
    category_id INT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_expense_categories_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE tax_rates (
    tax_id INT NOT NULL AUTO_INCREMENT,
    rate_percent DECIMAL(5, 2) NOT NULL,
    description VARCHAR(100) NOT NULL,
    PRIMARY KEY (tax_id),
    CONSTRAINT chk_tax_rates_percent CHECK (rate_percent >= 0 AND rate_percent <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- トランザクション系
-- =========================================================

CREATE TABLE expenses (
    expense_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount INT NOT NULL DEFAULT 0,
    submitted_at DATETIME NULL,
    version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (expense_id),
    KEY idx_expenses_user_id (user_id),
    KEY idx_expenses_status (status),
    CONSTRAINT fk_expenses_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT chk_expenses_status
        CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_expenses_total_amount CHECK (total_amount >= 0),
    CONSTRAINT chk_expenses_version CHECK (version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE expense_details (
    detail_id INT NOT NULL AUTO_INCREMENT,
    expense_id INT NOT NULL,
    expense_date DATE NOT NULL,
    category_id INT NOT NULL,
    tax_id INT NOT NULL,
    amount INT NOT NULL,
    memo VARCHAR(255) NULL,
    PRIMARY KEY (detail_id),
    KEY idx_expense_details_expense_id (expense_id),
    KEY idx_expense_details_category_id (category_id),
    KEY idx_expense_details_tax_id (tax_id),
    CONSTRAINT fk_expense_details_expense
        FOREIGN KEY (expense_id) REFERENCES expenses (expense_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_expense_details_category
        FOREIGN KEY (category_id) REFERENCES expense_categories (category_id),
    CONSTRAINT fk_expense_details_tax
        FOREIGN KEY (tax_id) REFERENCES tax_rates (tax_id),
    CONSTRAINT chk_expense_details_amount CHECK (amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================================
-- 添付ファイル・履歴・ログ系
-- =========================================================

CREATE TABLE receipt_images (
    image_id INT NOT NULL AUTO_INCREMENT,
    expense_id INT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (image_id),
    KEY idx_receipt_images_expense_id (expense_id),
    CONSTRAINT fk_receipt_images_expense
        FOREIGN KEY (expense_id) REFERENCES expenses (expense_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE approval_routes (
    route_id INT NOT NULL AUTO_INCREMENT,
    dept_id INT NOT NULL,
    step_number INT NOT NULL,
    approver_user_id INT NOT NULL,
    PRIMARY KEY (route_id),
    UNIQUE KEY uk_approval_routes_dept_step (dept_id, step_number),
    KEY idx_approval_routes_approver (approver_user_id),
    CONSTRAINT fk_approval_routes_department
        FOREIGN KEY (dept_id) REFERENCES departments (dept_id),
    CONSTRAINT fk_approval_routes_approver
        FOREIGN KEY (approver_user_id) REFERENCES users (user_id),
    CONSTRAINT chk_approval_routes_step CHECK (step_number >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE approval_histories (
    history_id INT NOT NULL AUTO_INCREMENT,
    expense_id INT NOT NULL,
    approver_user_id INT NOT NULL,
    action VARCHAR(20) NOT NULL,
    comment VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (history_id),
    KEY idx_approval_histories_expense_id (expense_id),
    KEY idx_approval_histories_approver (approver_user_id),
    CONSTRAINT fk_approval_histories_expense
        FOREIGN KEY (expense_id) REFERENCES expenses (expense_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_approval_histories_approver
        FOREIGN KEY (approver_user_id) REFERENCES users (user_id),
    CONSTRAINT chk_approval_histories_action
        CHECK (action IN ('SUBMIT', 'APPROVE', 'REJECT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE audit_logs (
    log_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_table VARCHAR(64) NOT NULL,
    target_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    KEY idx_audit_logs_user_id (user_id),
    KEY idx_audit_logs_created_at (created_at),
    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
