# マスコット素材

ユーザー提供の「ChatGPT Image 2026年9月12日 19_45_18.png」をもとに、組み込み ImageGen ツールで背景透過版とアイコン用ポートレートを作成しました。元画像は変更していません。

## 利用先

- `app/src/main/res/drawable-nodpi/routina_mascot.png`: 「今日」のヘッダーと「ふりかえり → 成長」の全身イラスト。
- `app/src/main/res/drawable-nodpi/routina_mascot_portrait.png`: 通常・丸型のアダプティブアイコンで共用するポートレート。
- `app/src/main/res/drawable/ic_launcher_foreground.xml`: 108dp の前景レイヤーに対し上下左右を約19.44%内側に配置。中央66dpの範囲に収め、ランチャーのマスクに余白を確保。

両PNGは透過アルファを保持し、密度による自動拡大を避ける `drawable-nodpi` に保存しています。画面では `ContentScale.Fit`、ランチャーでは XML の inset と bitmap の fill で表示サイズを指定します。成長段階は同じマスコットにラベルとバッジを組み合わせて表現し、XP・ポイントの計算は既存のままです。

## 生成プロンプト（組み込みツール）

### 全身の背景除去

> Use case: background-extraction. Asset type: transparent mascot PNG used in an Android habit app and its launcher icon. Edit target: the attached image. Remove ONLY the entire photographic road, crosswalk, scenery and cast shadow behind/beneath the character. Preserve the exact illustrated character, face, big purple eyes, pale blonde hair, white/black hat, blue hair clip, blue white black outfit, pose, proportions, colors and bold dark outlines. Keep the entire character including hat and shoes intact, centered, front-facing at the same proportions, with small even transparent margins (about 5%) around its full silhouette. Actual transparent alpha background, not checkerboard, no backdrop, no ground shadow, no text, no new accessories, no redesign. Output a clean high resolution square PNG cutout ready for app use.

### アイコン用ポートレート

入力: 上記の全身透過画像。

> Use case: precise-object-edit. Asset type: Android launcher icon foreground portrait, genuine transparent PNG, square. Edit target: supplied transparent mascot. Create an icon portrait of this exact character by reframing her head, hat, face and upper shoulders; preserve the exact purple eyes, blonde hair, blue hair clip, black and white hat, blue/white/black costume and thick dark line style, no redesign. Show complete hat and frame the long hair as a compact bust ending just below her blue necktie. Make the head dominate this bust for recognizability at 48px icon sizes. Center the portrait in the square with a small 5% transparent margin around the silhouette. Actual alpha transparency outside the character, no colored background, no shadow, no border, no text, no rounded-square frame, no checkerboard. This is the foreground only; Android adds its own background and mask.

### ポートレートの透過修正

初回のポートレートには市松模様が描かれていたため、以下で透過アルファへ修正した出力を採用しました。

> Use case: background-extraction. Edit target: attached mascot portrait. Remove the entire gray-and-white checkerboard background and replace it with REAL ALPHA TRANSPARENCY. The output must be an RGBA PNG with zero-alpha outside the character, not a painted checkerboard or solid background. Keep the exact character pixels, composition, square size, complete hat, hair, face, and clothing unchanged. Change ONLY the background. No added artwork or shadow.
