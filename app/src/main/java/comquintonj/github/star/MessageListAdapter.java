package comquintonj.github.star;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter for the home page in which you can see a ListView of past messages
 */
class MessageListAdapter extends BaseAdapter {

    /**
     * ArrayList to hold the message data
     */
    private ArrayList<String> data;

    /**
     * Infalter for the layout
     */
    private static LayoutInflater inflater = null;

    /**
     * Constructor
     * @param a the activity being used
     * @param d the data to inflate the View with
     */
    MessageListAdapter(Activity a, ArrayList<String> d) {
        this.data=d;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Get the number of data items
     * @return the number of items
     */
    public int getCount() {
        return data.size();
    }

    /**
     * Get item by a specific position
     * @param position the position desired
     * @return the Object in the designated position
     */
    public Object getItem(int position) {
        return position;
    }

    /**
     * Retrieve an item ID based on position
     * @param position the position desired
     * @return the ID of the item
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Set thet text of the item based on view
     * @param position the position of the itme
     * @param sourceView the input view
     * @param parent the parent ViewGroup
     * @return the ViewInflater after the rows have been set correctly
     */
    public View getView(int position, View sourceView, ViewGroup parent) {
        View viewInflater = sourceView;
        if(sourceView == null)
            viewInflater = inflater.inflate(R.layout.message_in_list, null);

        TextView title = (TextView) viewInflater.findViewById(R.id.title);
        TextView firstLetter = (TextView) viewInflater.findViewById(R.id.first_letter_text);

        String sender = data.get(position);
        title.setText(sender);
        String letter = String.valueOf(sender.charAt(0));
        firstLetter.setText(letter.toUpperCase());

        return viewInflater;
    }
}