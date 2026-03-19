(() => {
document.addEventListener("DOMContentLoaded", async () => {
    const logoutBtn = document.getElementById("logoutBtn");

    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            localStorage.removeItem("token");
            window.location.href = "/login.html";
        });
    }

    try {
        const user = await apiGet("/api/user/me");

        setUserInfoText(user);
        buildMenu(user);
        initRoutes(user);

    } catch (e) {
        document.getElementById("mainView").innerHTML =
            `<div class="alert alert-danger">Not logged in or user not found. Please go to <a href="/login.html">/login.html</a>.</div>`;
    }
});
})();