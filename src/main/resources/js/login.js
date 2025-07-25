document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('loginForm');
    const msg = document.getElementById('msg');

    form.addEventListener('submit', function () {
        msg.textContent = 'ログイン処理中...';
    });
});