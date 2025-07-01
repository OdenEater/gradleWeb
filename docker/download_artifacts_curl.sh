#!/bin/bash

# 使用方法の表示
usage() {
  echo "使用方法: $0 [オプション]"
  echo "オプション:"
  echo "  -r, --repo OWNER/REPO   リポジトリ名（例: octocat/Hello-World）"
  echo "  -w, --workflow ID       ワークフローID（省略可）"
  echo "  -n, --name NAME         ダウンロードするArtifactの名前（省略可）"
  echo "  -d, --dir DIRECTORY     ダウンロード先のディレクトリ（デフォルト: current）"
  echo "  -h, --help              このヘルプメッセージを表示"
  exit 1
}

# 引数のパース
REPO=""
WORKFLOW=""
ARTIFACT_NAME=""
DOWNLOAD_DIR="."

while [[ $# -gt 0 ]]; do
  key="$1"
  case $key in
    -r|--repo)
      REPO="$2"
      shift
      shift
      ;;
    -w|--workflow)
      WORKFLOW="$2"
      shift
      shift
      ;;
    -n|--name)
      ARTIFACT_NAME="$2"
      shift
      shift
      ;;
    -d|--dir)
      DOWNLOAD_DIR="$2"
      shift
      shift
      ;;
    -h|--help)
      usage
      ;;
    *)
      echo "不明なオプション: $1"
      usage
      ;;
  esac
done

# 必須パラメータのチェック
if [ -z "$REPO" ]; then
  echo "エラー: リポジトリ名が必要です。"
  usage
fi

# GitHub CLIのインストール確認
if ! command -v gh &> /dev/null; then
  echo "エラー: GitHub CLI (gh) がインストールされていません。"
  echo "インストール方法: https://cli.github.com/manual/installation"
  exit 1
fi

# GitHub CLIの認証確認
if ! gh auth status &> /dev/null; then
  echo "GitHub CLIで認証が必要です。"
  gh auth login
fi

# ダウンロードディレクトリの作成
mkdir -p "$DOWNLOAD_DIR"
cd "$DOWNLOAD_DIR" || exit

echo "Artifactsをダウンロードしています..."

# ワークフローIDが指定されている場合
if [ -n "$WORKFLOW" ]; then
  if [ -n "$ARTIFACT_NAME" ]; then
    echo "リポジトリ: $REPO, ワークフロー: $WORKFLOW, Artifact: $ARTIFACT_NAME"
    gh run download "$WORKFLOW" -n "$ARTIFACT_NAME" -R "$REPO"
  else
    echo "リポジトリ: $REPO, ワークフロー: $WORKFLOW"
    gh run download "$WORKFLOW" -R "$REPO"
  fi
else
  # 最新の成功したワークフローからすべてのArtifactsをダウンロード
  LATEST_RUN=$(gh run list -R "$REPO" --status completed -L 1 --json databaseId --jq '.[0].databaseId')

  if [ -n "$LATEST_RUN" ]; then
    if [ -n "$ARTIFACT_NAME" ]; then
      echo "リポジトリ: $REPO, 最新の成功したワークフロー: $LATEST_RUN, Artifact: $ARTIFACT_NAME"
      gh run download "$LATEST_RUN" -n "$ARTIFACT_NAME" -R "$REPO"
    else
      echo "リポジトリ: $REPO, 最新の成功したワークフロー: $LATEST_RUN"
      gh run download "$LATEST_RUN" -R "$REPO"
    fi
  else
    echo "成功したワークフローが見つかりませんでした。"
    exit 1
  fi
fi

echo "ダウンロードが完了しました。場所: $DOWNLOAD_DIR"