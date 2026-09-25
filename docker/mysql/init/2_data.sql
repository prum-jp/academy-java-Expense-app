SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE expense_db;

-- =========================================================
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

