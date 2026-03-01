$(document).ready(function () {
    const table = $("#myRequestsTable").DataTable({
        ajax: {
            url: "/api/requests/my",
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
                    return `<button type="button"
                                class="btn btn-sm btn-outline-primary view-request"
                                data-id="${row.id}">
                                View
                            </button>`;
                }
            }
        ]
    });

    $("#myRequestsTable").on("click", ".view-request", function () {
        const id = $(this).data("id");
        window.router.navigate(`/resident/requests/${id}`);
    });
});