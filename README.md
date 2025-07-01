初回ghインストールが必要

#AlmaLinuxの場合
sudo dnf install -y dnf-plugins-core

sudo dnf config-manager --add-repo https://cli.github.com/packages/rpm/gh-cli.repo

sudo dnf install -y gh
