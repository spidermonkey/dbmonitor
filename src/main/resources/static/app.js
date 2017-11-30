var stompClient = null;

function setConnected(connected) {
    $("#connected").html("WebSocket connection established!");
    $("#connected").attr('class', "alert alert-success");
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
    });
}

//Not in use at the moment
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function showNotification(notification) {
    $("#notifications").prepend("<tr><td>" + notification.tableName  +  "</td><td>" + notification.rowId  +  "</td><td>" +  notification.timeStamp + "</td><td>" + notification.dataBaseEventType + "</td></tr>");
}

$(function () {
    connect();
});