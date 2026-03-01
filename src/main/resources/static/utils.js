async function apiGet(url) {
    const res = await fetch(url);
    if (!res.ok) throw new Error(await res.text());
    return res.json();
}

async function apiPost(url, bodyObj) {
    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(bodyObj),
        credentials: "same-origin"
    });

    const contentType = res.headers.get("content-type") || "";
    let data = null;
    let text = null;

    if (contentType.includes("application/json")) {
        data = await res.json();
    } else {
        text = await res.text();
    }

    if (!res.ok) {
        const err = new Error("Request failed");
        err.status = res.status;
        err.data = data;
        err.text = text;
        throw err;
    }

    return data ?? text;
}

async function apiPatch(url, bodyObj) {
    const res = await fetch(url, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(bodyObj),
        credentials: "same-origin"
    });

    const contentType = res.headers.get("content-type") || "";
    let data = null;
    let text = null;

    if (contentType.includes("application/json")) data = await res.json();
    else text = await res.text();

    if (!res.ok) {
        const err = new Error("Request failed");
        err.status = res.status;
        err.data = data;
        err.text = text;
        throw err;
    }

    return data ?? text;
}

async function loadView(htmlPath, jsPath) {
    const res = await fetch(htmlPath);
    if (!res.ok) {
        document.getElementById("mainView").innerHTML =
            `<div class="alert alert-danger">View not found: ${htmlPath}</div>`;
        return;
    }

    const html = await res.text();
    document.getElementById("mainView").innerHTML = html;

    if (jsPath) {
        const old = document.getElementById("viewScript");
        if (old) old.remove();

        const s = document.createElement("script");
        s.id = "viewScript";
        s.src = jsPath + "?v=" + Date.now();

        // IMPORTANT: wait until the script loads (so event listeners are attached)
        await new Promise((resolve, reject) => {
            s.onload = resolve;
            s.onerror = () => reject(new Error("Failed to load view script: " + jsPath));
            document.body.appendChild(s);
        });
    }
}

function setUserInfoText(user) {
    const el = document.getElementById("userInfo");
    el.textContent = `${user.username} (${user.role}${user.unit ? ", " + user.unit : ""})`;
}

function buildMenu(user) {
    const menu = document.getElementById("roleMenu");
    menu.innerHTML = "";

    const items = [];

    if (user.role === "ADMIN") {
        items.push({ label: "Admin: All Requests", path: "/admin/requests" });
    }

    if (user.role === "RESIDENT") {
        items.push({ label: "Resident: Create Request", path: "/resident/create" });
        items.push({ label: "Resident: My Requests", path: "/resident/my-requests" });
    }

    // render
    items.forEach(item => {
        const li = document.createElement("li");
        const a = document.createElement("a");
        a.className = "dropdown-item";
        a.href = item.path;
        a.textContent = item.label;
        a.addEventListener("click", (e) => {
            e.preventDefault();
            window.router.navigate(item.path);
        });
        li.appendChild(a);
        menu.appendChild(li);
    });
}