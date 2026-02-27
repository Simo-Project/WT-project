$(document).ready(function () {
    const apiUrl = 'http://localhost:8081/api';
});

let table;

function loadTable() {
    // If the table already exists, just reload data
    if (table) {
        table.ajax.reload(null, false);
        return;
    }

    // First time: create the DataTable
    table = $("#requestsTable").DataTable({
        ajax: {
            url: "/api/admin/requests",
            dataSrc: ""
        },
        columns: [
            { data: "createdOn" },
            {data: "task"},
            { data: "status" },
            { data: "priority" },
            { data: "unit" }
        ]
    });
}

document.addEventListener("DOMContentLoaded", function () {
    const viewBtn = document.getElementById("viewAllBtn");
    const tableWrap = document.getElementById("tableWrap");

    viewBtn.addEventListener("click", function () {
        tableWrap.style.display = "block";
        loadTable();

        // Simple auto-refresh every 10 seconds (remove if you don’t want this yet)
        setInterval(function () {
            if (table) {
                table.ajax.reload(null, false);
            }
        }, 10000);
    });
});

