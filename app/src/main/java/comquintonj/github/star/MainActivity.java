package comquintonj.github.star;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;
import java.util.Locale;

/**
 * MainActivity that displays on startup
 */
public class MainActivity extends AppCompatActivity {

    private ChatViewAdapter chatAdapter;
    private ListView chatList;
    private TextToSpeech textToSpeech;
    private EditText inputText;
    private static final int SPEECH_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate view
        setTitle("Chat");
        inputText = (EditText) findViewById(R.id.inputText);
        Button inputButton = (Button) findViewById(R.id.inputButton);
        chatList = (ListView) findViewById(R.id.chatView);
        DrawableCompat.setTint(inputButton.getBackground(),
                ContextCompat.getColor(this, R.color.colorAccent));

        chatAdapter = new ChatViewAdapter(getApplicationContext(), R.layout.right);
        chatList.setAdapter(chatAdapter);

        // Instantiate TextToSpeech object
        textToSpeech =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        // If a user has decided to dictate
        inputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = inputText.getText().toString();
                sayIt(toSpeak);
                sendMessage(toSpeak, true);
            }
        });

        // Set scrolling adapter for the ListView holding chats
        chatList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        chatList.setAdapter(chatAdapter);

        // Scroll the list view to bottom on data change
        chatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                chatList.setSelection(chatAdapter.getCount() - 1);
            }
        });
    }

    /**
     * Show a message in the ListView of chats
     * @param spoken what has been said
     * @param whoSent whether or not the user has sent the message
     */
    private void sendMessage(String spoken, boolean whoSent) {
        chatAdapter.add(new Message(whoSent, spoken));
        inputText.setText("");
    }

    /**
     * Say the given string out loud
     * @param text the text to be said
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void sayIt(String text) {
        String utteranceId=this.hashCode() + "";
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    /**
     * Display the Google Speech Recognizer
     */
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    /**
     * Decide which preset response was clicked
     * @param v the Button that was clicked
     */
    public void presetClicked(View v){
        Button presetButton = (Button) findViewById(v.getId());
        String toSpeak = presetButton.getText().toString();
        sayIt(toSpeak);
    }


    /**
     * On result of the Google Speech Recognizer, use the input to transmit to the user
     * @param requestCode the request code to decide which intent the activity came from
     * @param resultCode the result of the intent
     * @param data the intent that the user has come from
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            sendMessage(spokenText, false);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem microphone = menu.findItem(R.id.microphone);
        DrawableCompat.setTint(microphone.getIcon(), ContextCompat.getColor(this, R.color.white));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.microphone:
                displaySpeechRecognizer();
                //test
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}
