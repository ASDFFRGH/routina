# Routina

日々のルーティーンをカレンダーで記録し、継続によってポイントと経験値を獲得してキャラクターを育てる Android アプリです。

## MVP の機能

- 月間カレンダーで、予定なし・未達成・一部完了・完了を表示
- 毎日、3日ごと、週1回、任意の日数間隔でルーティーンを登録
- 過去日と当日の完了記録・取消
- 完了ごとのポイント・XP付与と、レベルに応じた3段階のキャラクター成長
- Roomによる端末内へのオフライン保存

## 技術構成

- Kotlin
- Jetpack Compose / Material 3
- Navigation Compose
- Room
- ViewModel / Kotlin Flow
- minSdk 26 / compileSdk 37 / targetSdk 37

## ビルド

Android Studioでプロジェクトを開くか、Android SDKとJDK 17を設定して次を実行します。

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Debug APKは `app/build/outputs/apk/debug/app-debug.apk` に生成されます。

## GitHub Releases

`v0.1.0` のような `v*` タグをpushすると、GitHub Actionsがテスト・lint・debug APKのビルドを行い、GitHub Releasesのプレリリースとして公開します。配布物はdebug署名済みのプレビュー版であり、本番署名版ではありません。

CIでは実行ごとに一時的なdebug鍵で署名されるため、将来のAPKでアプリを上書き更新することはできません。更新時は既存アプリのアンインストールが必要となり、端末内に保存したデータは削除されます。

## 現在の前提

- データはローカル端末内だけに保存します。
- 「週1回」は開始日を基準に7日間隔で予定します。
- アーカイブしたルーティーンも過去の履歴と獲得済み報酬を保持します。
