package comquintonj.github.star;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatView adapter for MainActivty to show the messages in the conversation
 */
public class ChatViewAdapter extends ArrayAdapter<Message> {

    /**
     * The list of messages to show
     */
    private List<Message> chatMessageList = new ArrayList<Message>();

    @Override
    public void add(Message object) {
        chatMessageList.add(object);
        super.add(object);
    }

    /**
     * Constructor to create the adapter
     * @param context the current context of the application
     * @param textViewResourceId the text resource to use
     */
    public ChatViewAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }
    /**
     * Get the number of data items
     * @return the number of items
     */
    public int getCount() {
        return this.chatMessageList.size();
    }

    /**
     * Get item by a specific position
     * @param index the position desired
     * @return the Object in the designated position
     */
    public Message getItem(int index) {
        return this.chatMessageList.get(index);
    }

    /**
     * Set thet text of the item based on view
     * @param position the position of the itme
     * @param sourceView the input view
     * @param parent the parent ViewGroup
     * @return the ViewInflater after the rows have been set correctly
     */
    public View getView(int position, View sourceView, ViewGroup parent) {
        Message chatMessageObj = getItem(position);
        View row = sourceView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessageObj.left) {
            row = inflater.inflate(R.layout.right, parent, false);
        }else{
            row = inflater.inflate(R.layout.left, parent, false);
        }
        TextView chatText = (TextView) row.findViewById(R.id.msgr);
        chatText.setText(chatMessageObj.message);
        return row;
    }
}