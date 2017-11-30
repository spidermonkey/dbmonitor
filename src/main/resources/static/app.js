var stompClient = null;

function setConnected(connected) {
    if (connected) {
        $("#connected").html("WebSocket connection established!");
        $("#connected").attr('class', "alert alert-success");
    }
    else {
        $("#connected").html("WebSocket connection failed!");
        $("#connected").attr('class', "alert alert-error");
    }
}

function connect() {
    var socket = new SockJS('/betvictor');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/dbInsertNotifications', function (dbInsert) {
            showNotification(JSON.parse(dbInsert.body));
        });
    }, function() {
        setConnected(false);
    });
}

function showNotification(notification) {
    $("#notifications").prepend("<tr><td>" + notification.tableName  +  "</td><td>" + notification.rowId  +  "</td><td>" +  notification.timeStamp + "</td><td>" + notification.dataBaseEventType + "</td></tr>");
}

$(function () {
    connect();
});