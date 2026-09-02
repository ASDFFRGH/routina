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

## 現在の前提

- データはローカル端末内だけに保存します。
- 「週1回」は開始日を基準に7日間隔で予定します。
- アーカイブしたルーティーンも過去の履歴と獲得済み報酬を保持します。
