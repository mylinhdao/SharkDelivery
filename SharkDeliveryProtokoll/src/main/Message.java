public class Message {
    CharSequence receiver;
    CharSequence sender;
    byte[] content;

    public Message(byte[] content, CharSequence receiver, CharSequence sender) {
        this.receiver = receiver;
        this.sender = sender;
        this.content = content;
    }
}
