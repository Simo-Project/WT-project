let myTable;

function showMessage(type, text) {
    // type: success | danger | warning | info
    const html = `<div class="alert alert-${type}" role="alert">${text}</div>`;
    document.getElementById("messageArea").innerHTML = html;
}

function initMyRequestsTable() {
    myTable = $("#myRequestsTable").DataTable({
        ajax: {
            url: "/api/requests/my",
            dataSrc: ""
        },
        columns: [
            { data: "createdOn" },
            { data: "task" },
            { data: "status" },
            { data: "priority" }
        ],
        paging: true,
        searching: true,
        ordering: true
    });
}

async function submitRequest(payload) {
    const res = await fetch("/api/requests", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    if (res.ok) {
        return { ok: true, data: await res.json() };
    }

    let errBody = null;
    try {
        errBody = await res.json();
    } catch (e) {
        // ignore
    }
    return { ok: false, status: res.status, error: errBody };
}

document.addEventListener("DOMContentLoaded", function () {
    initMyRequestsTable();

    const form = document.getElementById("createRequestForm");
    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        const title = document.getElementById("title").value.trim();
        const category = document.getElementById("category").value;
        const description = document.getElementById("description").value.trim();

        const payload = { title, category, description };

        const result = await submitRequest(payload);

        if (result.ok) {
            showMessage("success", "Request created successfully.");
            form.reset();
            if (myTable) myTable.ajax.reload(null, false);
            return;
        }

        // Validation errors (400)
        if (result.status === 400 && result.error && result.error.errors) {
            const errors = result.error.errors;
            const msg = Object.keys(errors)
                .map(k => `${k}: ${errors[k]}`)
                .join("<br>");
            showMessage("danger", msg);
            return;
        }

        // Forbidden/unauthorised
        if (result.status === 401 || result.status === 403) {
            showMessage("danger", "You are not authorised. Please log in as a resident.");
            return;
        }

        showMessage("danger", "Something went wrong. Please try again.");
    });
});
