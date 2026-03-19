(() => {
function initRoutes(user) {
    const router = new Navigo("/");
    window.router = router;

    router.on("/admin/requests", async () => {
        await loadView("/views/admin/requests.html", "/views/admin/requests.js");
    });

    router.on("/resident/create", async () => {
        await loadView("/views/resident/create-request.html", "/views/resident/create-request.js");
    });

    router.on("/resident/my-requests", async () => {
        await loadView("/views/resident/my-requests.html", "/views/resident/my-requests.js");
    });

    router.on("/resident/requests/:id", async (match) => {
        window.routeParams = match.data;
        await loadView("/views/resident/request-details.html", "/views/resident/request-details.js");
    });

    router.on("/admin/requests/:id", async (match) => {
        window.routeParams = match.data;
        await loadView("/views/admin/request-details.html", "/views/admin/request-details.js");
    });

    router.on("/", () => {
        if (user.role === "ADMIN") return router.navigate("/admin/requests");
        if (user.role === "RESIDENT") return router.navigate("/resident/create");
        document.getElementById("mainView").innerHTML = `<div class="alert alert-warning">Unknown role.</div>`;
    });

    router.resolve();
}
    window.initRoutes = initRoutes;
})();