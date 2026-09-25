# java-Expense-app

## 必要環境

- Docker / Docker Compose
- Git

Java や Gradle のインストールは不要です。

## 環境構築

① ブラウザで以下にアクセスする。

https://github.com/prum-jp/academy-java-Expense-app

② 右上の「Use this template」ボタンをクリックする。

③ 任意のリポジトリ名を入力して「Create repository」をクリックする。

④ 自分のリポジトリをクローンする。

```bash
git clone https://github.com/【自分のユーザー名】/【リポジトリ名】.git
cd 【リポジトリ名】
```

⑤ アプリを起動する。

```bash
docker compose up --build -d
```

初回はイメージのビルドと MySQL 初期化に数分かかることがあります。

起動後、ブラウザで http://localhost:8081 を開きます。

⑥ `main` ブランチから作業ブランチを切る。

```bash
git switch -c <ブランチ名>
```

⑦ 課題に取り組む。

⑧ 変更をコミット＆プッシュする。

```bash
git add .
git commit -m "【任意の内容】"
git push origin <ブランチ名>
```

⑨ `main` ブランチ向けの Pull Request（PR）を作成する。

不具合1件につき PR を1本作る。

## Pull Request（PR）の記載事項

PR を作成する際は、以下の内容を記載してください。

- 変更の意図: なぜそのように直したのか（理由を言語化してください）。
- 工夫した点: 読みやすくするために意識したこと。

## 評価・フィードバック

提出された PR に対して、メンターがコードレビューを行います。  
レビューでは、コードの綺麗さだけでなく、「なぜそのように直したのか」という意図が正しく説明できているかを重視します。

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
