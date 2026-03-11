function showDetailsError(msg) {
    document.getElementById("detailsMsg").innerHTML =
        `<div class="alert alert-danger">${msg}</div>`;
}

function showDetailsSuccess(msg) {
    document.getElementById("detailsMsg").innerHTML =
        `<div class="alert alert-success">${msg}</div>`;
}

function renderDetails(r) {
    document.getElementById("detailsCard").innerHTML = `
      <div class="card-body">
        <dl class="row mb-0">
          <dt class="col-sm-3">ID</dt><dd class="col-sm-9">${r.id}</dd>
          <dt class="col-sm-3">Task</dt><dd class="col-sm-9">${r.task}</dd>
          <dt class="col-sm-3">Category</dt><dd class="col-sm-9">${r.category}</dd>
          <dt class="col-sm-3">Description</dt><dd class="col-sm-9">${r.description}</dd>
          <dt class="col-sm-3">Status</dt><dd class="col-sm-9">${r.status}</dd>
          <dt class="col-sm-3">Priority</dt><dd class="col-sm-9">${r.priority}</dd>
          <dt class="col-sm-3">Unit</dt><dd class="col-sm-9">${r.unit}</dd>
          <dt class="col-sm-3">Created</dt><dd class="col-sm-9">${r.createdOn}</dd>
        </dl>
      </div>
    `;

    const cancelBtn = document.getElementById("cancelBtn");
    cancelBtn.style.display = r.status === "NEW" ? "inline-block" : "none";

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
        const r = await apiGet(`/api/requests/${id}`);
        renderDetails(r);
        return r;
    }

    try {
        await loadRequest();

        document.getElementById("cancelBtn").addEventListener("click", async () => {
            try {
                await apiPatch(`/api/requests/${id}/cancel`, {});
                await loadRequest();
                showDetailsSuccess("Request cancelled successfully.");
            } catch (e) {
                console.log(e);

                if (e.status === 409) {
                    showDetailsError("Cancellation is not allowed for this request status.");
                } else if (e.status === 403) {
                    showDetailsError("You are not allowed to cancel this request.");
                } else {
                    showDetailsError("Could not cancel request.");
                }
            }
        });

    } catch (e) {
        showDetailsError("Could not load request details. You may not be authorised, or the request does not exist.");
        console.log(e);
    }
})();