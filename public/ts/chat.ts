/// <reference path="../d.ts/DefinitelyTyped/jquery/jquery.d.ts" />

var chat_start = function(chatSocket, userId) {

    var sendMessage = function() {
        chatSocket.send(JSON.stringify(
            {text: $("#talk").val()}
        ))
        $("#talk").val('')
    }

    var receiveEvent = function(event) {
        var data = JSON.parse(event.data)

        // Handle errors
        if(data.error) {
            chatSocket.close()
            $("#onError span").text(data.error)
            $("#onError").show()
            return
        } else {
            $("#onChat").show()
        }

        // Create the message element
        var el = $('<div class="message"><img class="avatar" /><span></span><p></p></div>')
        var user = data.members.filter(function(member) {
            return member.id == data.user
        })[0];
        $("img", el).attr("src", user.avatar)
        $("span", el).text(user.name)
        $("p", el).text(data.message)
        $(el).addClass(data.kind)
        if(data.user == userId) $(el).addClass('me')
        $('#messages').append(el)

        // Update the members list
        $("#members").html('')
        $(data.members).each(function() {
            var li = $('<li><img class="avatar" /><span></span></li>');
            $("img", li).attr("src", this.avatar)
            $("span", li).text(this.name)
            $("#members").append(li);
        })
    }

    var handleReturnKey = function(e) {
        if(e.charCode == 13 || e.keyCode == 13) {
            e.preventDefault()
            sendMessage()
        }
    }

    $("#talk").keypress(handleReturnKey)

    chatSocket.onmessage = receiveEvent

}