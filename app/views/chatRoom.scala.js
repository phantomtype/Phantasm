@(roomId: Long, userId: Long)(implicit r: RequestHeader)

$(function() {

    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var chatSocket = new WS("@routes.Application.chat(roomId, userId).webSocketURL()")

    chat_start(chatSocket, @userId)
})
