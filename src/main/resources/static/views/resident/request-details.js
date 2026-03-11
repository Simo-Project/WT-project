function showDetailsError(msg) {
    document.getElementById("detailsMsg").innerHTML =
        `<div class="alert alert-danger">${msg}</div>`;
}

function showDetailsSuccess(msg) {
    document.getElementById("detailsMsg").innerHTML =
        `<div class="alert alert-success">${msg}</div>`;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function renderComments(comments) {
    const commentsList = document.getElementById("commentsList");

    if (!comments || comments.length === 0) {
        commentsList.innerHTML = `<div class="text-muted">No comments yet.</div>`;
        return;
    }

    commentsList.innerHTML = comments.map(c => `
        <div class="border rounded p-3 mb-2">
            <div class="d-flex justify-content-between align-items-center mb-1">
                <strong>${escapeHtml(c.authorUsername)}</strong>
                <small class="text-muted">${escapeHtml(c.createdAt)}</small>
            </div>
            <div>${escapeHtml(c.text)}</div>
        </div>
    `).join("");
}

function renderDetails(request) {
    document.getElementById("detailsCard").innerHTML = `
      <div class="card-body">
        <dl class="row mb-0">
          <dt class="col-sm-3">ID</dt><dd class="col-sm-9">${request.id}</dd>
          <dt class="col-sm-3">Task</dt><dd class="col-sm-9">${escapeHtml(request.task)}</dd>
          <dt class="col-sm-3">Category</dt><dd class="col-sm-9">${escapeHtml(request.category)}</dd>
          <dt class="col-sm-3">Description</dt><dd class="col-sm-9">${escapeHtml(request.description)}</dd>
          <dt class="col-sm-3">Status</dt><dd class="col-sm-9">${escapeHtml(request.status)}</dd>
          <dt class="col-sm-3">Priority</dt><dd class="col-sm-9">${escapeHtml(request.priority)}</dd>
          <dt class="col-sm-3">Unit</dt><dd class="col-sm-9">${escapeHtml(request.unit)}</dd>
          <dt class="col-sm-3">Created</dt><dd class="col-sm-9">${escapeHtml(request.createdOn)}</dd>
          <dt class="col-sm-3">Assigned To</dt><dd class="col-sm-9">${escapeHtml(request.assignedToUsername || "Unassigned")}</dd>
        </dl>
      </div>
    `;

    renderComments(request.comments || []);
}

(async function () {
    const id = window.routeParams?.id;

    if (!id) {
        showDetailsError("Missing request id.");
        return;
    }

    document.getElementById("backBtn").addEventListener("click", () => {
        window.router.navigate("/resident/my-requests");
    });

    async function loadRequest() {
        const request = await apiGet(`/api/requests/${id}`);
        renderDetails(request);
        return request;
    }

    try {
        await loadRequest();

        document.getElementById("commentForm").addEventListener("submit", async (e) => {
            e.preventDefault();

            const textEl = document.getElementById("commentText");
            const text = textEl.value;

            try {
                await apiPost(`/api/requests/${id}/comments`, { text });
                textEl.value = "";
                await loadRequest();
                showDetailsSuccess("Comment added successfully.");
            } catch (err) {
                console.log(err);

                if (err.status === 400) {
                    showDetailsError("Comment cannot be empty.");
                } else if (err.status === 403) {
                    showDetailsError("You are not allowed to comment on this request.");
                } else {
                    showDetailsError("Could not save comment.");
                }
            }
        });
    } catch (e) {
        showDetailsError("Could not load request details. You may not be authorised, or the request does not exist.");
        console.log(e);
    }
})();