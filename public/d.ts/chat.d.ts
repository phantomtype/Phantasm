
interface Message {
    members: Array<Member>
    error:   string
    user:    number
    message: string
    kind:    string
}

interface Member {
     id:     number
     avatar: string
     name:   string
}
