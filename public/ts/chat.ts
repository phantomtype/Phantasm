/// <reference path="../d.ts/DefinitelyTyped/jquery/jquery.d.ts" />
/// <reference path="../d.ts/DefinitelyTyped/angularjs/angular.d.ts" />
/// <reference path="../d.ts/DefinitelyTyped/notify.js/notify.js.d.ts" />

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
    userSetting: UserSetting
}

interface Comment {
    id: number
    user: Member
    replyTo: Comment
    message: string
    created: Date
}

interface UserSetting {
    id: number
    user_id: number
    desktopNotifications: boolean
}

module Account {
    export interface  Scope extends  ng.IScope {
        userSetting: UserSetting
        get: () => void
        save: () => void
    }

    export class Controller {
        constructor($scope: Scope, $http:ng.IHttpService) {
            $scope.get = () => {
                $http.get("/account/user_setting").success((data: any) => {
                   $scope.userSetting = data
                })
            }
            $scope.get()

            $scope.save = () => {
                $http.post("/account/save", $scope.userSetting).success((data) => {
                    alert("save changed!")
                })
            }
        }
    }
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
        reply: (msg: Message) => void
        replyCancel: () => void
        replyTo: Comment
        members: Member[]
        rooms: Room[]
        userSetting: UserSetting

        isSupportedNotification : boolean
        needsPermission : boolean
        requestPermission: ()=> void
        setNotification: (boolean)=> void
        notify : notify.INotify
    }

    export class Controller {
        constructor($scope:Scope, $http:ng.IHttpService) {
            $scope.replyTo = null
            $scope.messages = []
            $http.get("/recently_messages/" + $scope.roomId).success((result) => {
                result.reverse().forEach((message: Message) => {
                    $scope.messages.push(message)
                })
                setTimeout(() => {
                    $("div.messages").animate({ scrollTop: $("div.messages")[0].scrollHeight }, 1)
                }, 50)
            })

            $scope.rooms = []
            $http.get("/rooms").success((result) => {
                result.forEach((room: Room) => {
                    $scope.rooms.push(room)
                })
            })

            $http.get("/room/" + $scope.roomId + "/wspath").success((result) => {
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

                            if($scope.userSetting.desktopNotifications && data.user.id != $scope.userId) {
                                var notify = new Notify("Phantasm - " + data.user.fullName, {body : data.comment.message, icon: data.user.avatarUrl});
                                notify.show()

                                setTimeout(() => {
                                    notify.close()
                                }, 5000)
                            }

                            $scope.$digest()
                            $("div.messages").animate({ scrollTop: $("div.messages")[0].scrollHeight }, 'fast')
                        } else {
                            $scope.members = []
                            data.members.forEach((member:Member) => {
                                $scope.members.push(member)
                            })
                        }
                    }

                    $scope.reply = (message: Message) => {
                        $scope.replyTo = message.comment
                        $("#talkBody").focus()
                    }

                    $scope.replyCancel = () => {
                        $scope.replyTo = null
                    }

                    $scope.talk = (e:KeyboardEvent) => {
                        if ((e.charCode == 13 || e.keyCode == 13) && (e.shiftKey || detectmob())) {
                            e.preventDefault()
                            chatSocket.send(JSON.stringify({
                                text: $scope.talkBody,
                                replyTo: $scope.replyTo == null ? null : $scope.replyTo.id
                            }))
                            $scope.talkBody = ""
                            $scope.replyTo = null
                            $("div.messages").animate({ scrollTop: $("div.messages")[0].scrollHeight }, 'fast')
                        }
                    }
                }
            })

            $http.get("/account/user_setting").success((data: UserSetting) => {
                $scope.userSetting = data
            })

            $scope.isSupportedNotification = Notify.isSupported()
            $scope.needsPermission = Notify.needsPermission()

            $scope.requestPermission = () => {
                Notify.requestPermission(()=> {
                    $scope.userSetting.desktopNotifications = true
                })
            }

            $scope.setNotification = (value: boolean) => {
                $scope.userSetting.desktopNotifications = value
            }
        }
    }
}

function detectmob() {
    if( navigator.userAgent.match(/Android/i)
        || navigator.userAgent.match(/webOS/i)
        || navigator.userAgent.match(/iPhone/i)
        || navigator.userAgent.match(/iPad/i)
        || navigator.userAgent.match(/iPod/i)
        || navigator.userAgent.match(/BlackBerry/i)
        || navigator.userAgent.match(/Windows Phone/i)
        ){
        return true;
    }
    else {
        return false;
    }
}

var app = angular.module('phantasm', ['hc.marked'])
app.config(["marked", (marked) => {
    marked.setOptions({gfm: true, breaks: true, sanitize: true})
}])
.controller("Rooms.Controller", Rooms.Controller)
.controller("Chat.Controller", Chat.Controller)
.controller("Account.Controller", Account.Controller)
