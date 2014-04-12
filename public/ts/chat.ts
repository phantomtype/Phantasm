/// <reference path="../d.ts/DefinitelyTyped/jquery/jquery.d.ts" />
/// <reference path="../d.ts/DefinitelyTyped/angularjs/angular.d.ts" />

interface Room {
    id: number
    name: string
    owner: Member
    is_private: boolean
    latest_post: Comment
}

interface Message {
    members: Array<Member>
    error:   string
    user:    Member
    comment: Comment
    kind:    string
}

interface Member {
    id:     number
    avatarUrl: string
    firstName: string
    fullName:   string
}

interface Comment {
    user: Member
    message: string
    created: Date
}

module Rooms {
    export interface  Scope extends  ng.IScope {
        newRoom: Room
        rooms: Room[]
        list: () => void
        showCreateRoomForm: (e:MouseEvent) => void
        close: () => void
        createRoom: (e:MouseEvent) => void
    }

    export class Controller {
        constructor($scope: Scope, $http:ng.IHttpService) {
            $scope.list = () => {
                $scope.rooms = []
                $http.get("/rooms").success((result) => {
                    result.forEach((room: Room) => {
                        $scope.rooms.push(room)
                    })
                })
            }
            $scope.list()

            $("#new-room").hide()
            $scope.showCreateRoomForm = (e:MouseEvent) => {
                $("#new-room").show()
            }

            $scope.close = () => {
                $("#new-room").hide()
            }

            $scope.createRoom = (e:MouseEvent) => {
                $http.post("/room/create", $scope.newRoom).success((data) => {
                    $scope.close()
                    $scope.list()
                })
            }
        }
    }
}

module Chat {

    export interface Scope extends ng.IScope {
        roomId: number
        userId: number
        messages: Message[]
        talkBody: String
        talk: (KeyboardEvent) => void
        members: Member[]
        rooms: Room[]
    }

    export class Controller {
        constructor($scope:Scope, $http:ng.IHttpService) {
            $scope.messages = []
            $http.get("/recently_messages/" + $scope.roomId).success((result) => {
                result.reverse().forEach((message: Message) => {
                    $scope.messages.push(message)
                })
                $("div.messages").animate({ scrollTop: $("div.messages")[0].scrollHeight }, 1)
            })

            $scope.rooms = []
            $http.get("/rooms").success((result) => {
                result.forEach((room: Room) => {
                    $scope.rooms.push(room)
                })
            })

            $http.get("/room/" + $scope.roomId + "/" + $scope.userId + "/wspath").success((result) => {
                var chatSocket = new WebSocket(result.path)

                chatSocket.onopen = () => {
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

                        if (data.kind == "talk") {
                            $scope.messages.push(data)
                        } else {
                            $scope.members = []
                            data.members.forEach((member:Member) => {
                                $scope.members.push(member)
                            })
                        }

                        $scope.$digest()
                        $("div.messages").animate({ scrollTop: $("div.messages")[0].scrollHeight }, 'fast')
                    }

                    $scope.talk = (e:KeyboardEvent) => {
                        if (e.charCode == 13 || e.keyCode == 13) {
                            e.preventDefault()
                            chatSocket.send(JSON.stringify(
                                {text: $scope.talkBody}
                            ))
                            $scope.talkBody = ""
                            $("div.messages").animate({ scrollTop: $("div.messages")[0].scrollHeight }, 'fast')
                        }
                    }
                }
            })
        }
    }
}