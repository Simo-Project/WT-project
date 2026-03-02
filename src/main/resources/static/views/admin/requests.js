$(document).ready(function () {
    const STATUSES = ["NEW", "IN_PROGRESS", "AWAITING_PARTS", "CLOSED"];

    function showMsg(html) {
        document.getElementById("adminMsg").innerHTML = html;
    }

    const table = $("#requestsTable").DataTable({
        ajax: {
            url: "/api/admin/requests",
            dataSrc: ""
        },
        columns: [
            { data: "task" },
            { data: "status" },
            { data: "priority" },
            { data: "unit" },
            { data: "createdOn" },
            {
                data: null,
                orderable: false,
                searchable: false,
                render: function (data, type, row) {
                    const options = STATUSES.map(s =>
                        `<option value="${s}" ${s === row.status ? "selected" : ""}>${s.replaceAll("_", " ")}</option>`
                    ).join("");

                    return `
                      <div class="d-flex gap-2 align-items-center">
                        <select class="form-select form-select-sm status-select">
                          ${options}
                        </select>
                        <button class="btn btn-sm btn-primary update-status">Update</button>
                      </div>
                    `;
                }
            }
        ]
    });

    $("#requestsTable").on("click", ".update-status", async function () {
        const $btn = $(this);
        const rowApi = table.row($btn.closest("tr"));
        const rowData = rowApi.data();

        const newStatus = $btn.closest("tr").find(".status-select").val();

        $btn.prop("disabled", true);

        try {
            const updated = await apiPatch(`/api/admin/requests/${rowData.id}/status`, { status: newStatus });

            // Immediately reflect in the DataTable (no full reload needed)
            rowData.status = updated.status;
            rowApi.data(rowData).invalidate().draw(false);

            showMsg(`<div class="alert alert-success">Status updated to <strong>${updated.status.replaceAll("_", " ")}</strong>.</div>`);
        } catch (e) {
            console.log(e);
            showMsg(`<div class="alert alert-danger">Failed to update status.</div>`);
        } finally {
            $btn.prop("disabled", false);
        }
    });
});