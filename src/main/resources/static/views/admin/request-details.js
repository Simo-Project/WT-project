function showMsg(html) {
    document.getElementById("detailsMsg").innerHTML = html;
}

function renderDetails(request) {
    document.getElementById("detailsCard").innerHTML = `
      <div class="card-body">
        <dl class="row mb-0">
          <dt class="col-sm-3">ID</dt><dd class="col-sm-9">${request.id}</dd>
          <dt class="col-sm-3">Task</dt><dd class="col-sm-9">${request.task}</dd>
          <dt class="col-sm-3">Category</dt><dd class="col-sm-9">${request.category}</dd>
          <dt class="col-sm-3">Description</dt><dd class="col-sm-9">${request.description}</dd>
          <dt class="col-sm-3">Status</dt><dd class="col-sm-9">${request.status}</dd>
          <dt class="col-sm-3">Priority</dt><dd class="col-sm-9">${request.priority}</dd>
          <dt class="col-sm-3">Unit</dt><dd class="col-sm-9">${request.unit}</dd>
          <dt class="col-sm-3">Created</dt><dd class="col-sm-9">${request.createdOn}</dd>
          <dt class="col-sm-3">Assigned To</dt><dd class="col-sm-9">${request.assignedToUsername || "Unassigned"}</dd>
        </dl>
      </div>
    `;
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

    try {
        let request = await apiGet(`/api/admin/requests/${id}`);
        const staffUsers = await apiGet(`/api/admin/requests/staff`);

        renderDetails(request);

        const select = document.getElementById("staffSelect");
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

        document.getElementById("assignBtn").addEventListener("click", async () => {
            const selectedValue = select.value;

            try {
                const updated = await apiPatch(`/api/admin/requests/${id}/assign`, {
                    staffUserId: selectedValue ? Number(selectedValue) : null
                });

                request = await apiGet(`/api/admin/requests/${id}`);
                renderDetails(request);

                showMsg(`<div class="alert alert-success">Assignment updated successfully.</div>`);
            } catch (e) {
                console.log(e);
                showMsg(`<div class="alert alert-danger">Failed to update assignment.</div>`);
            }
        });

    } catch (e) {
        console.log(e);
        showMsg(`<div class="alert alert-danger">Could not load request details.</div>`);
    }
})();