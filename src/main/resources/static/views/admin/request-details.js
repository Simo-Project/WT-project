function showMsg(html) {
    document.getElementById("detailsMsg").innerHTML = html;
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
        showMsg(`<div class="alert alert-danger">Missing request id.</div>`);
        return;
    }

    document.getElementById("backBtn").addEventListener("click", () => {
        window.router.navigate("/admin/requests");
    });

    const select = document.getElementById("staffSelect");

    async function loadPageData() {
        const request = await apiGet(`/api/admin/requests/${id}`);
        const staffUsers = await apiGet(`/api/admin/requests/staff`);

        renderDetails(request);

        select.innerHTML = `<option value="">Unassigned</option>`;

        staffUsers.forEach(u => {
            const option = document.createElement("option");
            option.value = u.id;
            option.textContent = u.username;

            if (request.assignedToUsername === u.username) {
                option.selected = true;
            }

            select.appendChild(option);
        });

        return request;
    }

    try {
        await loadPageData();

        document.getElementById("assignBtn").addEventListener("click", async () => {
            const selectedValue = select.value;

            try {
                await apiPatch(`/api/admin/requests/${id}/assign`, {
                    staffUserId: selectedValue ? Number(selectedValue) : null
                });

                await loadPageData();
                showMsg(`<div class="alert alert-success">Assignment updated successfully.</div>`);
            } catch (e) {
                console.log(e);
                showMsg(`<div class="alert alert-danger">Failed to update assignment.</div>`);
            }
        });

        document.getElementById("commentForm").addEventListener("submit", async (e) => {
            e.preventDefault();

            const textEl = document.getElementById("commentText");
            const text = textEl.value;

            try {
                await apiPost(`/api/admin/requests/${id}/comments`, { text });
                textEl.value = "";
                await loadPageData();
                showMsg(`<div class="alert alert-success">Comment added successfully.</div>`);
            } catch (err) {
                console.log(err);

                if (err.status === 400) {
                    showMsg(`<div class="alert alert-danger">Comment cannot be empty.</div>`);
                } else if (err.status === 403) {
                    showMsg(`<div class="alert alert-danger">You are not allowed to comment on this request.</div>`);
                } else {
                    showMsg(`<div class="alert alert-danger">Could not save comment.</div>`);
                }
            }
        });

    } catch (e) {
        console.log(e);
        showMsg(`<div class="alert alert-danger">Could not load request details.</div>`);
    }
})();