
interface Message {
    members: Array<Member>
    error:   string
    user:    Member
    message: string
    kind:    string
}

interface Member {
     id:     number
     avatar: string
     name:   string
}
