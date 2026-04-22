# GitHubでの自動APKビルドと公開

このプロジェクトは main ブランチへの push を契機に Release APK を自動ビルドし、以下へ公開します。

- GitHub Actions Artifact
- GitHub Releases (tag: latest)

ワークフロー定義: `.github/workflows/build-release-apk.yml`

## 1. 事前準備

以下を GitHub Repository Secrets に登録してください。

- `ANDROID_KEYSTORE_BASE64`
  - keystore ファイル (`.jks` または `.keystore`) の Base64 文字列
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

PowerShell での Base64 変換例:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\release.keystore"))
```

## 2. 実行トリガー

- `main` への push で自動実行

## 3. 生成物

- Artifact: `app-release-<commit-sha>`
- Release: `latest` タグのアセットとして `app-release.apk`

## 4. 失敗時の確認ポイント

1. Secrets がすべて登録済みか
2. Alias / password が keystore と一致しているか
3. `app` モジュールの release 署名設定が環境変数を受け取れているか

## 5. 補足

- `latest` リリースは毎回更新される運用です。
- コミットごとの履歴は Artifact 側に残ります。
