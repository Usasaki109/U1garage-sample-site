# U1-GARAGE サンプルサイト（静的＋日次自動更新）

> **TL;DR**  
> GitHub Pages の静的サイトです。お問い合わせページの「**代車空き状況：残り n 台**」だけを、  
> **毎朝 9:00（JST）** に **Java + GitHub Actions** で自動更新します。  
> 現場は **Gist の数字（半角1桁）を上書き**するだけ。閲覧時の外部通信はゼロ。  
> 失敗時は **前日の表示を維持** し、サイトは壊れません。

---

## 公開URL
- Site: <!-- 例） --> https://usasaki109.github.io/U1garage-sample-site/
- 動作確認ポイント: **Contact** ページ内の「代車空き状況」

---

## 何を解決したのか
- サイトは静的の速さ・安定性を保ちつつ、**最小コストで“更新感”**を出す。
- **現場運用を簡単に**：数字1桁を変えたい日にだけ Gist を上書きすれば翌朝反映。
- **耐障害性**：更新に失敗しても公開ページは前日のまま（ユーザーにエラーを見せない）。

---

## 仕組み（概要）
[Gist: loaner_count.txt(数字1桁)]
│ 毎朝9:00(JST)
▼
[GitHub Actions] ── Java（LoanerUpdater）
│ 1) Gistから数字取得・検証
│ 2) data/loaner.json 生成
│ 3) contact.html の数字を置換
▼
[GitHub Pages を自動デプロイ]
▼
[閲覧者：静的HTMLを高速表示（外部通信なし）]
- スケジュールは `cron: 0 0 * * *`（= **UTC 0:00 → JST 9:00**）
- Java は JST で日付を書き出し（`Asia/Tokyo`）。

---

## リポジトリ構成（抜粋）
├─ contact.html # 「代車空き状況」表示（<span data-loaner-count>n</span>）
├─ data/
│ └─ loaner.json # {"count": n, "date": "YYYY-MM-DD"} を毎朝生成
└─ .github/
├─ workflows/
│ └─ loaner-daily.yml # 毎朝9:00 JST に Java を実行して反映
└─ scripts/
└─ LoanerUpdater.java # Gist取得→検証→JSON生成→HTML置換


---

## 運用のしかた（現場向け）

- **台数を変えたい日**  
  1) Gist の `loaner_count.txt` を開く  
  2) 中身を **半角 0〜9 の1文字**にして保存  
  3) **翌朝 9:00** にサイトへ自動反映

- **いますぐ反映したい**  
  GitHub → **Actions → Loaner daily update → Run workflow** をクリック
