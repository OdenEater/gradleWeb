document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('loginForm');
    const msg = document.getElementById('msg');
    const errorMsg = document.getElementById('error-msg');

    // URLパラメータでエラーをチェック
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('error')) {
        errorMsg.style.display = 'block';
    }

    // Cookieからトークンを取得する関数
    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    }

    // フォーム送信をJavaScriptで処理
    form.addEventListener('submit', function (e) {
        e.preventDefault(); // デフォルトのフォーム送信を防止
        msg.textContent = 'ログイン処理中...';
        errorMsg.style.display = 'none';

        // CookieからCSRFトークンを直接取得
        const csrfToken = getCookie('XSRF-TOKEN');
        
        if (!csrfToken) {
            msg.textContent = 'CSRFトークンの取得に失敗しました。ページを再読み込みしてください。';
            msg.style.color = 'red';
            return false;
        }

        document.getElementById('csrfToken').value = csrfToken;
        console.log('Using CSRF token from cookie:', csrfToken);

        // フォームデータの取得
        const formData = new URLSearchParams();
        formData.append('username', document.getElementById('username').value);
        formData.append('password', document.getElementById('password').value);
        formData.append('_csrf', csrfToken);

        // フェッチAPIでログインリクエスト送信
        fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-XSRF-TOKEN': csrfToken
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

    // 既存のCSRF取得処理も残しておく（デバッグ用）
    fetch('/csrf', {
        method: 'GET',
        credentials: 'same-origin'
    })
    .then(response => response.json())
    .then(data => {
        console.log('CSRF Token from endpoint:', data.token);
        // 注：このトークンは参考用に表示するだけで、実際はCookieのトークンを使用
    })
    .catch(error => {
        console.error('CSRFトークンの取得に失敗しました:', error);
    });
});