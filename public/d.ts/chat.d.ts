
interface Message {
    members: Array<Member>
    error:   string
    user:    Member
    comment: Comment
    kind:    string
}

interface Member {
     id:     number
     avatar: string
     name:   string
}

interface Comment {
	message: string
	created: Date
}
