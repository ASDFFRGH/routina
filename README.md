# Routina

今日やるルーティーンに集中し、継続によってポイントと経験値を獲得してキャラクターを育てる Android アプリです。

## MVP の機能

- 起動直後の「今日」画面で、次に行うルーティーンと今日の完了状況を確認・記録
- 毎日、3日ごと、週1回、任意の日数間隔でルーティーンを登録
- 「ルーティーン」画面で習慣を管理し、「ふりかえり」画面で履歴と成長を確認
- 月間カレンダーで、予定なし・未達成・一部完了・完了を表示
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

`v0.1.0` のような `v*` タグをpushすると、GitHub Actionsがテスト・lint・本番署名APKのビルドを行い、GitHub Releasesの正式リリースとして公開します。

アプリ起動時に公開中の最新リリースを確認し、新しいバージョンがあれば更新ダイアログを表示します。「更新する」を選ぶとブラウザでAPKを開き、Androidの確認画面からインストールできます。確認に失敗した場合はアプリの利用を妨げません。

v0.1.5は旧debug版とパッケージ名が異なるため、既存のRoutinaと併存してインストールできます。ただし、旧アプリの端末内データは移行されません。v0.1.5以降の更新は、この新しいパッケージに対して適用されます。

## 現在の前提

- データはローカル端末内だけに保存します。
- 「週1回」は開始日を基準に7日間隔で予定します。
- アーカイブしたルーティーンも過去の履歴と獲得済み報酬を保持します。
