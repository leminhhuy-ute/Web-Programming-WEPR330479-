document.querySelector("#login-form")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    const message = document.querySelector("#message");
    try {
        await request("/api/auth/csrf");
        await request("/api/auth/login", {
            method: "POST",
            redirectOnUnauthorized: false,
            body: JSON.stringify({
                username: form.get("username"),
                password: form.get("password"),
            }),
        });
        location.href = api("/admin/dashboard.html");
    } catch (error) {
        message.textContent = error.message;
        message.hidden = false;
    }
});
