document.addEventListener("DOMContentLoaded", async () => {
    try {
        const user = await apiGet("/api/user/me");

        setUserInfoText(user);
        buildMenu(user);
        initRoutes(user);

    } catch (e) {
        document.getElementById("mainView").innerHTML =
            `<div class="alert alert-danger">Not logged in or user not found. Please go to <a href="/login">/login</a>.</div>`;
    }
});