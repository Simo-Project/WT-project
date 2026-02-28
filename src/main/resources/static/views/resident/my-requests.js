$(document).ready(function () {
    $("#myRequestsTable").DataTable({
        ajax: {
            url: "/api/requests/my",
            dataSrc: ""
        },
        columns: [
            { data: "task" },
            { data: "status" },
            { data: "priority" },
            { data: "unit" },
            { data: "createdOn" }
        ]
    });
});