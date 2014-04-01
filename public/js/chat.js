
var Chat;
(function (Chat) {
    var Controller = (function () {
        function Controller($scope, $http) {
            $scope.messages = [];
            $http.get("/recently_messages/" + $scope.roomId).success(function (result) {
                result.reverse().forEach(function (message) {
                    $scope.messages.push(message);
                });
                $("div.messages").animate({ scrollTop: $("div.messages")[0].scrollHeight }, 1);
            });

            $scope.rooms = [];
            $http.get("/rooms").success(function (result) {
                result.forEach(function (room) {
                    $scope.rooms.push(room);
                });
            });

            $http.get("/room/" + $scope.roomId + "/" + $scope.userId + "/wspath").success(function (result) {
                var chatSocket = new WebSocket(result.path);

                chatSocket.onopen = function () {
                    chatSocket.onmessage = function (event) {
                        var data = JSON.parse(event.data);

                        if (data.error) {
                            chatSocket.close();
                            console.log(data.error);
                            return;
                        } else {
                            $("#onChat").show();
                        }

                        if (data.kind == "talk") {
                            $scope.messages.push(data);
                        } else {
                            $scope.members = [];
                            data.members.forEach(function (member) {
                                $scope.members.push(member);
                            });
                        }

                        $scope.$digest();
                        $("div.messages").animate({ scrollTop: $("div.messages")[0].scrollHeight }, 'fast');
                    };

                    $scope.talk = function (e) {
                        if (e.charCode == 13 || e.keyCode == 13) {
                            e.preventDefault();
                            chatSocket.send(JSON.stringify({ text: $scope.talkBody }));
                            $scope.talkBody = "";
                            $("div.messages").animate({ scrollTop: $("div.messages")[0].scrollHeight }, 'fast');
                        }
                    };
                };
            });
        }
        return Controller;
    })();
    Chat.Controller = Controller;
})(Chat || (Chat = {}));
//# sourceMappingURL=chat.js.map
