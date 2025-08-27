document.addEventListener('DOMContentLoaded', function () {
    // タブ切り替え機能の追加
    const tabs = document.querySelectorAll('.tab');
    const formContainers = document.querySelectorAll('.form-container');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const targetTab = tab.getAttribute('data-tab');

            // タブのアクティブ状態を切り替え
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');

            // フォームの表示/非表示を切り替え
            formContainers.forEach(container => {
                container.classList.add('hidden');
            });
            document.getElementById(targetTab + 'FormContainer').classList.remove('hidden');
        });
    });

    // ログインフォーム処理（既存のコード）
    const form = document.getElementById('loginForm');
    const msg = document.getElementById('login-msg');
    const errorMsg = document.getElementById('login-error-msg');

    // URLパラメータでエラーをチェック
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('error')) {
        errorMsg.style.display = 'block';
    }

    // フォーム送信をJavaScriptで処理
    form.addEventListener('submit', function (e) {
        e.preventDefault(); // デフォルトのフォーム送信を防止
        msg.textContent = 'ログイン処理中...';
        errorMsg.style.display = 'none';

        // フォームデータの取得
        const formData = new URLSearchParams();
        formData.append('username', document.getElementById('username').value);
        formData.append('password', document.getElementById('password').value);

        // フェッチAPIでログインリクエスト送信
        fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData.toString(),
            credentials: 'same-origin'
        })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
            } else if (!response.ok) {
                errorMsg.style.display = 'block';
                msg.textContent = '';
            }
        })
        .catch(error => {
            console.error('ログイン処理中にエラーが発生しました:', error);
            errorMsg.style.display = 'block';
            msg.textContent = '';
        });
    });

    // 新規登録フォーム処理を追加
    const registerForm = document.getElementById('registerForm');
    const registerMsg = document.getElementById('register-msg');
    const registerErrorMsg = document.getElementById('register-error-msg');

    if (registerForm) {
        registerForm.addEventListener('submit', function (e) {
            e.preventDefault();
            registerMsg.textContent = '登録処理中...';
            registerErrorMsg.style.display = 'none';

            // パスワード一致チェック
            const password = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (password !== confirmPassword) {
                registerErrorMsg.textContent = 'パスワードが一致しません';
                registerErrorMsg.style.display = 'block';
                registerMsg.textContent = '';
                return;
            }

            // フォームデータの取得
            const formData = new URLSearchParams();
            formData.append('username', document.getElementById('newUsername').value);
            formData.append('password', password);

            // フェッチAPIで新規登録リクエスト送信
            fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData.toString(),
                credentials: 'same-origin'
            })
            .then(response => {
                if (response.ok) {
                    registerMsg.textContent = '登録が完了しました。ログインしてください。';
                    registerMsg.className = 'msg success-msg';
                    registerForm.reset();

                    // 3秒後にログインタブに切り替え
                    setTimeout(() => {
                        document.querySelector('.tab[data-tab="login"]').click();
                    }, 3000);
                } else {
                    if (response.status === 409) {
                        // ユーザー名重複時のエラー表示
                        registerErrorMsg.textContent = 'このユーザー名は既に使われています。';
                        registerErrorMsg.style.display = 'block';
                        registerMsg.textContent = '';
                        registerMsg.className = 'msg';
                    } else {
                        return response.json().then(data => {
                            registerErrorMsg.textContent = data.message || '登録に失敗しました';
                            registerErrorMsg.style.display = 'block';
                            registerMsg.textContent = '';
                            registerMsg.className = 'msg';
                        }).catch(() => {
                            registerErrorMsg.textContent = '登録に失敗しました';
                            registerErrorMsg.style.display = 'block';
                            registerMsg.textContent = '';
                            registerMsg.className = 'msg';
                        });
                    }
                }
            })
            .catch(error => {
                registerErrorMsg.textContent = error.message;
                registerErrorMsg.style.display = 'block';
                registerMsg.textContent = '';
            });
        });
    }
});