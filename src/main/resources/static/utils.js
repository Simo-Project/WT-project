function getToken() {
    return localStorage.getItem("token");
}

function clearToken() {
    localStorage.removeItem("token");
}

function authHeaders() {
    const token = getToken();
    return token ? { "Authorization": `Bearer ${token}` } : {};
}

async function handleResponse(res) {
    const contentType = res.headers.get("content-type") || "";
    let data = null;
    let text = null;

    if (contentType.includes("application/json")) {
        data = await res.json();
    } else {
        text = await res.text();
    }

    if (res.status === 401) {
        clearToken();
        window.location.href = "/login.html";
        throw new Error("Unauthorized");
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

async function apiGet(url) {
    const res = await fetch(url, {
        headers: {
            ...authHeaders()
        }
    });

    return handleResponse(res);
}

async function apiPost(url, bodyObj) {
    const res = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            ...authHeaders()
        },
        body: JSON.stringify(bodyObj)
    });

    return handleResponse(res);
}

async function apiPatch(url, bodyObj) {
    const res = await fetch(url, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
            ...authHeaders()
        },
        body: JSON.stringify(bodyObj)
    });

    return handleResponse(res);
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