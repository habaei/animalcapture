# Animal Capture (NeoForge 1.21.1)

キーを押しながら動物を右クリックするとアイテム化し、ブロックを右クリックすると元の状態で解放するMODです。

## 使い方
- 捕獲: **I**(デフォルト)を押しながら動物を右クリック
- 解放: 捕獲アイテムを持ってブロックを右クリック
- 対象は `Animal` を継承したモブのみ。名前・体力・子供/大人・懐き状態・毛色などは保存されます
- アイテムのアイコンは、中の動物を斜め前から見た姿で自動描画されます(動物ごとのPNGは不要)
- 捕獲アイテムはクリエイティブタブには出ません。`/give` では中身が空になるため、動物を捕獲して入手してください

## キーについて
バニラの **T はチャットを開くキー**なので、押しっぱなしにするとチャットが開いて使えません。
そのためデフォルトは I にしています。T にしたい場合は
`client/ClientModEvents.java` の `InputConstants.KEY_I` を `KEY_T` に変え、
コントロール設定でチャットのキーを別のキーに変更してください。
(ゲーム内のコントロール設定からも捕獲キーを自由に変えられます)

## アイコンの角度調整
`client/CapturedAnimalRenderer.java` 冒頭の定数だけで調整できます。
- `BODY_YAW` 体の向き / `HEAD_YAW` 頭の向き / `TILT` 見下ろし角度 / `FIT` 大きさ

## ビルド・実行
- JDK 21 が必要です
- `gradlew`(Gradle 8.10.2)は同梱済みです。初回はMinecraft/NeoForgeを自動ダウンロードするのでネット接続が必要です
- jar作成: `./gradlew build`(Windowsは `gradlew.bat build`)→ `build/libs/animalcapture-1.0.0.jar` を `.minecraft/mods` に入れる
- 開発実行: `./gradlew runClient`
- GitHubにpushすると `.github/workflows/build.yml` が自動でjarをビルドします(Actionsの Artifacts からダウンロード)
- `build.gradle` のプラグイン版(`net.neoforged.moddev`)と `gradle.properties` の `neo_version` は、解決できない場合は最新の 21.1.x / 公式MDKの値に更新してください

## 構成
- `CapturePayload` クライアント→サーバーの捕獲リクエスト。サーバー側で距離・種類を検証
- `CapturedAnimalItem` 捕獲データの保存と解放
- `client/ClientGameEvents` キー押下中の右クリック検知
- `client/CapturedAnimalRenderer` 斜め前向きアイコンの描画

パッケージ名 `com.example.animalcapture` は仮です。公開する場合は変更してください。
