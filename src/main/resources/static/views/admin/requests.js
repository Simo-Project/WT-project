$(document).ready(function () {
    const STATUSES = ["NEW", "IN_PROGRESS", "AWAITING_PARTS", "CLOSED"];

    function showMsg(html) {
        document.getElementById("adminMsg").innerHTML = html;
    }

    const statusFilter = document.getElementById("statusFilter");
    const priorityFilter = document.getElementById("priorityFilter");
    const clearFiltersBtn = document.getElementById("clearFiltersBtn");

    const table = $("#requestsTable").DataTable({
        ajax: {
            url: "/api/admin/requests",
            data: function (d) {
                const status = statusFilter.value;
                const priority = priorityFilter.value;

                if (status) d.status = status;
                if (priority) d.priority = priority;
            },
            dataSrc: "",
            beforeSend: function (xhr) {
                const token = localStorage.getItem("token");
                if (token) {
                    xhr.setRequestHeader("Authorization", "Bearer " + token);
                }
            },
            error: function (xhr) {
                console.log("requestsTable ajax error", xhr.status, xhr.responseText);

                if (xhr.status === 401) {
                    localStorage.removeItem("token");
                    window.location.href = "/login.html";
                }
            }
        },
        columns: [
            { data: "task" },
            { data: "status" },
            { data: "priority" },
            { data: "unit" },
            { data: "createdOn" },
            { data: "assignedToUsername",
                render: function(data) { return data || "Unassigned"; }
            },
            {
                data: null,
                orderable: false,
                searchable: false,
                render: function (data, type, row) {
                    const isCancelled = row.status === "CANCELLED"; // ADDED

                    const options = STATUSES.map(s =>
                        `<option value="${s}" ${s === row.status ? "selected" : ""}>${s.replaceAll("_", " ")}</option>`
                    ).join("");

                    return `
      <div class="d-flex flex-column gap-2">
        <button class="btn btn-sm btn-outline-primary view-request" data-id="${row.id}">
          View
        </button>

        <div class="d-flex gap-2 align-items-center">
          <select class="form-select form-select-sm status-select" ${isCancelled ? "disabled" : ""}>
            ${options}
          </select>
          <button class="btn btn-sm btn-primary update-status" ${isCancelled ? "disabled" : ""}>Update</button>
        </div>
      </div>
    `;
                }
            }
        ]
    });

    statusFilter.addEventListener("change", function () {
        table.ajax.reload();
    });

    priorityFilter.addEventListener("change", function () {
        table.ajax.reload();
    });

    clearFiltersBtn.addEventListener("click", function () {
        statusFilter.value = "";
        priorityFilter.value = "";
        table.ajax.reload();
    });

    $("#requestsTable").on("click", ".view-request", function () {
        const id = $(this).data("id");
        window.router.navigate(`/admin/requests/${id}`);
    });

    $("#requestsTable").on("click", ".update-status", async function () {
        const $btn = $(this);
        const rowApi = table.row($btn.closest("tr"));
        const rowData = rowApi.data();

        const newStatus = $btn.closest("tr").find(".status-select").val();

        $btn.prop("disabled", true);

        try {
            const updated = await apiPatch(`/api/admin/requests/${rowData.id}/status`, { status: newStatus });

            rowData.status = updated.status;
            rowApi.data(rowData).invalidate().draw(false);

            showMsg(`<div class="alert alert-success">Status updated to <strong>${updated.status.replaceAll("_", " ")}</strong>.</div>`);
        } catch (e) {
            console.log(e);
            const msg = e?.data?.message || e?.text || "Failed to update status.";
            showMsg(`<div class="alert alert-danger">${msg}</div>`);
        } finally {
            $btn.prop("disabled", false);
        }
    });
});