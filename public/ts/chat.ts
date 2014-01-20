/// <reference path="../d.ts/DefinitelyTyped/jquery/jquery.d.ts" />
/// <reference path="../d.ts/DefinitelyTyped/angularjs/angular.d.ts" />
/// <reference path="../d.ts/chat.d.ts" />

module Chat {

    export interface Scope extends ng.IScope {
        roomId: number
        userId: number
        messages: Array<any>
        talkBody: String
        talk: (KeyboardEvent) => void
        members: Array<any>
    }

    export class Controller {
        constructor($scope:Scope, $http:ng.IHttpService) {
            $scope.messages = []
            $http.get("/recently_messages/" + $scope.roomId).success((result) => {
                result.reverse().forEach((message) => {
                    $scope.messages.push(message)
                })
            })

            $http.get("/room/" + $scope.roomId + "/" + $scope.userId + "/wspath").success((result) => {
                var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
                var chatSocket = new WS(result.path)

                $scope.talk = (e:KeyboardEvent) => {
                    if (e.charCode == 13 || e.keyCode == 13) {
                        e.preventDefault()
                        chatSocket.send(JSON.stringify(
                            {text: $scope.talkBody}
                        ))
                        $scope.talkBody = ""
                    }
                }

                chatSocket.onmessage = (event) => {
                    var data:Message = JSON.parse(event.data)

                    // Handle errors
                    if (data.error) {
                        chatSocket.close()
                        console.log(data.error)
                        return
                    } else {
                        $("#onChat").show()
                    }

                    $scope.messages.push(data)

                    $scope.members = []
                    data.members.forEach((member:Member) => {
                        $scope.members.push(member)
                    })

                    $scope.$digest()
                }
            })
        }
    }
}