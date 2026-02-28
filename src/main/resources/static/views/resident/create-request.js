function showMsg(html) {
    document.getElementById("residentMsg").innerHTML = html;
}

function fieldErrors(errors) {
    const lines = Object.entries(errors).map(([k, v]) => `<li><b>${k}</b>: ${v}</li>`);
    return `<ul class="mb-0">${lines.join("")}</ul>`;
}

document.getElementById("createRequestForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const body = {
        title: document.getElementById("title").value,
        category: document.getElementById("category").value,
        description: document.getElementById("description").value
    };

    try {
        await apiPost("/api/requests", body);

        showMsg(`<div class="alert alert-success">Request saved successfully.</div>`);

        window.router.navigate("/resident/my-requests");

    } catch (err) {
        console.log("Submit error:", err);

        const details =
            err.data ? JSON.stringify(err.data) :
                (err.text ? err.text.substring(0, 200) : "No response body");

        showMsg(`<div class="alert alert-danger">
    Error submitting request.<br>
    <b>Status:</b> ${err.status}<br>
    <b>Details:</b> <pre class="mb-0">${details}</pre>
  </div>`);
    }
});