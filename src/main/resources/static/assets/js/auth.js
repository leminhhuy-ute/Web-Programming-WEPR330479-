document.querySelector("#login-form")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    const message = document.querySelector("#message");
    try {
        await request("/api/auth/csrf");
        const user = await request("/api/auth/login", {
            method: "POST",
            redirectOnUnauthorized: false,
            body: JSON.stringify({
                username: form.get("username"),
                password: form.get("password"),
            }),
        });
        location.href = api(user.role === "DEAN" ? "/admin/dashboard.html" : user.role === "STUDENT" ? "/student" : "/lecturer/dashboard.html");
    } catch (error) {
        message.textContent = error.message;
        message.hidden = false;
    }
});
