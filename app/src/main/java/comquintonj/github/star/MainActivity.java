package comquintonj.github.star;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.support.design.widget.Snackbar;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * MainActivity that displays on startup
 */
public class MainActivity extends AppCompatActivity {

    private ChatViewAdapter chatAdapter;
    private HashMap<String, String> customPresets = new HashMap<>();
    private ListView chatList;
    private TextToSpeech textToSpeech;
    private EditText inputText;
    private static final int SPEECH_REQUEST_CODE = 0;
    private int id = 0;
    private SQLiteHelper dbhelp;
    private Button addPresetButton;
    private Button inputButton;
    private LinearLayout presetValue;
    private Snackbar snackbar;

    /**
     * Used to access permission request
     */
    public static final int PERMISSIONS_SINGLE_REQUEST = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbhelp = new SQLiteHelper(this);

        // Instantiate view
        inputText = (EditText) findViewById(R.id.inputText);
        inputButton = (Button) findViewById(R.id.inputButton);
        addPresetButton = (Button) findViewById(R.id.addPreset);
        presetValue = (LinearLayout) findViewById(R.id.presetValues);
        chatList = (ListView) findViewById(R.id.chatView);
        DrawableCompat.setTint(inputButton.getBackground(),
                ContextCompat.getColor(this, R.color.colorAccent));

        // Check location permission for predictive text
        checkPermission();

        // Adding adapter to ListView
        chatAdapter = new ChatViewAdapter(getApplicationContext(), R.layout.right);
        chatList.setAdapter(chatAdapter);

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

        // Instantiate TextToSpeech object
        textToSpeech =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        // Set onClickListeners for buttons
        addOnClickListeners();

        // Add the custom presets from the SQLite database
        ArrayList<String> customPreset = dbhelp.getPresets();
        for (String preset : customPreset) {
            addPreset(preset);
        }
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
        sendMessage(toSpeak, true);
    }

    /**
     * Add on click listeners to buttons
     */
    private void addOnClickListeners() {
        // If a user has decided to dictate
        inputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = inputText.getText().toString();
                sayIt(toSpeak);
                sendMessage(toSpeak, true);
            }
        });

        // If a user has decided to add a custom preset
        addPresetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonName = inputText.getText().toString();
                boolean flag = buttonName.isEmpty();
                if (!flag) {
                    addPreset(buttonName);
                    Toast.makeText(MainActivity.this, "Preset added", Toast.LENGTH_SHORT).show();
                    dbhelp.addPreset(buttonName);
                }
            }
        });
    }

    /**
     * Add a custom preset with the given text
     * @param preset the text for the new preset
     */
    private void addPreset(String preset) {
        id++;
        Drawable d = getResources().getDrawable(R.drawable.roundedshapebtn);
        Button newButton = new Button(getApplicationContext());
        newButton.setId(id);
        newButton.setText(preset);
        newButton.setBackground(d);
        newButton.setTextColor(getApplication().getResources().getColor(R.color.white));
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presetHelp(v);
            }
        });

        LinearLayout.LayoutParams customValues =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        customValues.setMargins(0, 0, 15, 0);
        presetValue.addView(newButton, customValues);
    }

    /**
     * Sets text in preset button as input text
     * @param v the Button that was clicked
     */
    public void presetHelp(View v){
        Button presetButton = (Button) findViewById(v.getId());
        String toSpeak = presetButton.getText().toString();
        inputText.setText(toSpeak);
    }

    /**
     * Check to see if the user has allowed permissions for reading storage and accessing location.
     */
    public void checkPermission() {
        if (ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                Snackbar.make(this.findViewById(android.R.id.content),
                        "Granting permissions will allow you to get predictive responses" +
                                "based on location",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestPermissions(
                                        new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                                        PERMISSIONS_SINGLE_REQUEST);
                            }
                        }).show();
            } else {
                requestPermissions(
                        new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                        PERMISSIONS_SINGLE_REQUEST);
            }
        }
    }

    /**
     * Check to see if the user has given permission to read external storage
     *
     * @return whether or not the user has given permission
     */
    public boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
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

    /**
     * Create the options menu found in the top right of the activity
     * @param menu the menu to be added
     * @return if the menu has been successfully added
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem microphone = menu.findItem(R.id.microphone);
        DrawableCompat.setTint(microphone.getIcon(), ContextCompat.getColor(this, R.color.white));
        return true;
    }

    /**
     * When a user selects an option in the menu
     * @param item the item the user has selected
     * @return if the user has successfully used an item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.microphone:
                displaySpeechRecognizer();
                //test
                return true;
            case R.id.clear:
                recreate();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}
