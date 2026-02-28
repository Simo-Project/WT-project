$(document).ready(function () {
    $("#requestsTable").DataTable({
        ajax: {
            url: "/api/admin/requests",
            dataSrc: ""
        },
        columns: [
            { data: "task" },
            { data: "status" },
            { data: "priority" },
            { data: "unit" },
            { data: "createdOn" } // make sure your DTO JSON uses createdOn
        ]
    });
});