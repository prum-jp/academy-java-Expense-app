# java-Expense-app

## 必要環境

- Docker / Docker Compose

Java や Gradle のインストールは不要です。

## 起動

プロジェクトルートで次を実行します。

```bash
docker compose up --build -d
```

初回はイメージのビルドと MySQL 初期化に数分かかることがあります。

起動後、ブラウザで http://localhost:8081 を開きます。

## よく使うコマンド

```bash
# ログ確認
docker compose logs -f app

# 停止
docker compose down

# データごと初期化（DB・領収書をリセット）
docker compose down -v
docker compose up --build -d
```

## ポート

| サービス | ホスト側ポート |
|---------|---------------|
| アプリ | 8081 |
| MySQL | 3307 |

## ログイン情報

パスワードは全員 `password` です。

| ユーザー名 | ロール | 部署 |
|-----------|--------|------|
| yamada | 申請者 | 営業部 |
| sato | 承認者（1次） | 営業部 |
| takahashi | 申請者 | 開発部 |
| ito | 承認者（1次） | 開発部 |
| suzuki | 承認者（最終） | 経理部 |
