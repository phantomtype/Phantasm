/// <reference path="../d.ts/DefinitelyTyped/bundle.d.ts" />

interface Meta {
    unread: number
}

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

    in_mouse_over: boolean
    unread: boolean
}

interface Member {
    id:     number
    avatarUrl: string
    firstName: string
    fullName:   string
    userSetting: UserSetting
    online: boolean
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

module Meta {
    export interface Scope extends ng.IScope {
        meta: Meta
    }

    export class Controller {
        constructor($scope: Scope, meta: Meta) {
            $scope.meta = meta
            $scope.meta.unread = 0
        }
    }
}

module Header {
    export interface Scope extends ng.IScope {
        room: Room
    }

    export class Controller {
        constructor($scope: Scope, RoomService) {
            $scope.room = RoomService
        }
    }
}

module Account {
    export interface  Scope extends  ng.IScope {
        userSetting: UserSetting
        get: () => void
        save: () => void

        isSupportedNotification : boolean
        needsPermission : boolean
        requestPermission: ()=> void
        notify : notify.INotify
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

            $scope.isSupportedNotification = Notify.isSupported()
            $scope.needsPermission = Notify.needsPermission()

            $scope.requestPermission = () => {
                Notify.requestPermission(()=> {
                    $scope.userSetting.desktopNotifications = true
                    $scope.needsPermission = false
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
        meta: Meta
        room: Room
        roomId: number
        userId: number
        private_room: boolean
        is_room_member: boolean
        is_owned_room: boolean
        messages: Message[]
        talkBody: string
        talk: (KeyboardEvent) => void
        reply: (msg: Message) => void
        replyCancel: () => void
        read_more: () => void
        replyTo: Comment
        members: Member[]
        showRoomMembers: () => void
        rooms: Room[]
        userSetting: UserSetting

        busy: boolean

        showAddMemberForm: boolean
        openAddMemberForm: () => void
        closeAddMemberForm: () => void
        newMember: Member
        addableUsers: Member[]
        addMember: (Member) => void
        join_room: () => void

        talkBodyExpand: () => void
        read_message: (msg: Message) => void
    }

    export class Controller {
        constructor($scope:Scope, $http:ng.IHttpService, RoomService, meta: Meta) {
            $scope.meta = meta
            $scope.replyTo = null
            $scope.messages = []
            $scope.busy = false
            $scope.room = RoomService
            $scope.talkBody = ""

            $scope.read_more = () => {
                if ($(".messages")[0].scrollTop > 0) {
                    return
                }

                if ($scope.busy) return
                $scope.busy = true

                var to: number = $scope.messages.length > 0 ? $scope.messages[0].comment.created : new Date().getTime()
                $http.get("/room/" + $scope.roomId + "/messages/" + to).success((result) => {
                    if (result.length > 0) {
                        result.forEach((message:Message) => {
                            $scope.messages.unshift(message)
                        })
                        setTimeout(() => {
                            $("div.messages").animate({ scrollTop: $("#message-" + result[0].comment.id)[0].offsetTop }, 1)
                            $scope.$digest()
                            $scope.busy = false
                        }, 100)
                    }
                })
            }

            $scope.rooms = []
            $http.get("/rooms").success((result) => {
                result.forEach((room: Room) => {
                    $scope.rooms.push(room)

                    if (room.id == $scope.roomId) {
                        $scope.room.name = room.name
                        $scope.room.is_private = room.is_private
                        $scope.room.latest_post = room.latest_post
                        $scope.room.owner = room.owner
                    }
                })
            })

            $scope.showRoomMembers = () => {
                $scope.members = []
                $http.get("/room/" + $scope.roomId + "/members").success((result) => {
                    result.forEach((member:Member) => {
                        $scope.members.push(member)
                    })
                })
            }
            $scope.showRoomMembers()

            if ($scope.userId != null) {
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

                                if ($scope.userSetting.desktopNotifications && data.user.id != $scope.userId) {
                                    var notify = new Notify("Phantasm - " + data.user.fullName, {body: data.comment.message, icon: data.user.avatarUrl});
                                    notify.show()

                                    setTimeout(() => {
                                        notify.close()
                                    }, 5000)
                                }

                                if (data.user.id != $scope.userId) {
                                    $scope.meta.unread ++
                                    data.unread = true
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
                                data.members.forEach((member:Member) => {
                                    var index = $scope.members.map((m:Member) => m.id).indexOf(member.id)
                                    $scope.members[index].online = true
                                })
                            }
                        }

                        $scope.reply = (message:Message) => {
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

                        $scope.talkBodyExpand = () => {
                            var body:any = $("#talkBody")[0]
                            $("#talkBody").height((body.value.split("\n").length * 19) + 24 + "px")
                        }
                    }
                })
            }

            $scope.read_message = (msg:Message) => {
                if (msg.unread) {
                    msg.unread = false
                    $scope.meta.unread --
                }
            }

            if ($scope.userId != null) {
                $http.get("/account/user_setting").success((data: UserSetting) => {
                    $scope.userSetting = data
                })
            }


            // add room member

            $scope.openAddMemberForm = () => {
                $scope.addableUsers = []
                $http.get("/room/" + $scope.roomId + "/addable_users").success((data: Member[]) => {
                    data.forEach((user: Member) => {
                        $scope.addableUsers.push(user)
                    })
                    $scope.showAddMemberForm = true
                })
            }

            $scope.closeAddMemberForm = () => { $scope.showAddMemberForm = false }

            $scope.addMember = (member: Member) => {
                $http.post("/room/" + $scope.roomId + "/add_member", member).success((data) => {
                    $scope.showRoomMembers()
                })
            }


            // join room

            $scope.join_room = () => {
                $http.post("/room/" + $scope.roomId + "/add_member", {id: $scope.userId}).success((data) => {
                    $scope.showRoomMembers()
                    $scope.is_room_member = true
                })
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
app.factory("RoomService", () => {
    return {}
})
app.factory("meta", () => {
    return {}
})
.controller("Meta.Controller", Meta.Controller)
.controller("Rooms.Controller", Rooms.Controller)
.controller("Chat.Controller", Chat.Controller)
.controller("Account.Controller", Account.Controller)
