/// <reference path="../d.ts/DefinitelyTyped/bundle.d.ts" />

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
    created: number
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
        read_more: () => void
        replyTo: Comment
        members: Member[]
        rooms: Room[]
        userSetting: UserSetting

        isSupportedNotification : boolean
        needsPermission : boolean
        requestPermission: ()=> void
        setNotification: (boolean)=> void
        notify : notify.INotify

        busy: boolean
    }

    export class Controller {
        constructor($scope:Scope, $http:ng.IHttpService) {
            $scope.replyTo = null
            $scope.messages = []
            $scope.busy = false

            $scope.read_more = () => {
                if ($(".messages")[0].scrollTop > 0) {
                    return
                }

                if ($scope.busy) return
                $scope.busy = true
                var to: number = $scope.messages.length > 0 ? $scope.messages[0].comment.created : new Date().getTime()
                $http.get("/room/" + $scope.roomId + "/messages/" + to).success((result) => {
                    result.forEach((message: Message) => {
                        $scope.messages.unshift(message)
                    })
                    setTimeout(() => {
                        $("div.messages").animate({ scrollTop: $("#message-" + result[0].comment.id)[0].offsetTop }, 1)
                        $scope.busy = false
                        $scope.$digest()
                    }, 300)
                })
            }

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

                            if (!$scope.busy) {
                                $scope.busy = true
                                setTimeout(() => {
                                    $("div.messages").animate({ scrollTop: $("#message-" + data.comment.id)[0].offsetTop }, 1)
                                    $scope.busy = false
                                    $scope.$digest()
                                }, 300)
                            }
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

var app = angular.module('phantasm', ['hc.marked', 'infinite-scroll'])
app.config(["marked", (marked: MarkedStatic) => {
    marked.setOptions({
        gfm: true,
        breaks: true,
        sanitize: true,
        highlight: (code: string) => {
            return this.hljs.highlightAuto(code).value;
        }
    })
}])
.controller("Rooms.Controller", Rooms.Controller)
.controller("Chat.Controller", Chat.Controller)
.controller("Account.Controller", Account.Controller)
