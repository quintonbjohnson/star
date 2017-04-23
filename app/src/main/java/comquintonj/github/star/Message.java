package comquintonj.github.star;

/**
 * A class to represent a Message object
 */
public class Message {

    /**
     * Whether or not the message should appear as the user's message
     */
    public boolean left;

    /**
     * Who the sender of the message is
     */
    public String sender;

    /**
     * The text of the message
     */
    public String message;

    /**
     * Who the message is sent to
     */
    public String to;

    /**
     * Who the message is sent from
     */
    public String from;

    /**
     * Constructor for a Message object
     * @param left whether or not the message should appear as the user's message
     * @param message the text of the message
     */
    public Message(boolean left, String message) {
        super();
        this.left = left;
        this.message = message;
    }

    /**
     * Constructor for a Message object
     * @param sender the sender of the message
     * @param message the text of the message
     * @param to who the message is sent to
     */
    public Message(String sender, String message, String to) {
        this.sender = sender;
        this.message = message;
        this.to = to;
    }
}

