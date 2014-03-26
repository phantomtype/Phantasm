
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
	message: string
	created: Date
}
