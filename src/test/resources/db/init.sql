SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;

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
-- マスタ
-- =========================================================

INSERT INTO departments (dept_name) VALUES
    ('営業部'),
    ('開発部'),
    ('経理部');

INSERT INTO positions (position_name, rank_level) VALUES
    ('一般社員', 1),
    ('係長', 2),
    ('課長', 3),
    ('部長', 4);

INSERT INTO expense_categories (category_name) VALUES
    ('交通費'),
    ('会議費'),
    ('宿泊費'),
    ('消耗品費'),
    ('その他');

INSERT INTO tax_rates (rate_percent, description) VALUES
    (10.00, '標準税率 10%'),
    (8.00, '軽減税率 8%'),
    (0.00, '非課税 0%');

-- =========================================================
-- テストユーザー（パスワードは全員 password / BCrypt cost=10）
-- ※ ハッシュは $2a$10$ 形式。全5件とも平文 "password" と一致することを検証済み。
-- =========================================================

INSERT INTO users (user_name, email, password, dept_id, position_id, role) VALUES
    ('yamada',    'yamada@example.com',    '$2a$10$xGKij9d5JT6NXC93RuJPJu/oe5g3ByYq4FjzKVrjNAHZ9TpbGTQEC', 1, 1, 'USER'),
    ('sato',      'sato@example.com',      '$2a$10$z0f6h8JZpV0jl/STC/WEr.5v3p7nxIDQpGHP.2V6ALLJGM6qPcLGW', 1, 3, 'APPROVER'),
    ('takahashi', 'takahashi@example.com', '$2a$10$4E8/fC3LQOKOQwTnsLVmeuNGkksZMWgVLdmBP/eNLB9Vl0uiIJiri', 2, 1, 'USER'),
    ('ito',       'ito@example.com',       '$2a$10$xGKij9d5JT6NXC93RuJPJu/oe5g3ByYq4FjzKVrjNAHZ9TpbGTQEC', 2, 3, 'APPROVER'),
    ('suzuki',    'suzuki@example.com',    '$2a$10$z0f6h8JZpV0jl/STC/WEr.5v3p7nxIDQpGHP.2V6ALLJGM6qPcLGW', 3, 4, 'APPROVER');

-- =========================================================
-- 承認ルート（部署ごとに step 順。最終ステップは経理部長）
-- =========================================================

INSERT INTO approval_routes (dept_id, step_number, approver_user_id) VALUES
    (1, 1, 2),
    (1, 2, 5),
    (2, 1, 4),
    (2, 2, 5);

-- =========================================================
-- サンプル経費（ログイン直後に一覧が空にならないようにする）
-- =========================================================

INSERT INTO expenses (user_id, title, status, total_amount, submitted_at, version) VALUES
    (1, '営業交通費（サンプル下書き）', 'DRAFT', 1200, NULL, 0),
    (1, '顧客訪問交通費（サンプル申請）', 'PENDING', 3500, NOW(), 0);

INSERT INTO expense_details (expense_id, expense_date, category_id, tax_id, amount, memo) VALUES
    (1, CURDATE(), 1, 1, 1200, 'サンプル下書き'),
    (2, CURDATE(), 1, 1, 3500, 'サンプル申請');

INSERT INTO approval_histories (expense_id, approver_user_id, action, comment) VALUES
    (2, 1, 'SUBMIT', NULL);

