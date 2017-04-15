package comquintonj.github.star;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MainActivity that displays on startup
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * Button to add custom presets
     */
    private Button addPresetButton;

    /**
     * Button to say the input
     */
    private Button inputButton;

    /**
     * The first preset button that can be used to change to location responses
     */
    private Button preset1;

    /**
     * The second preset button that can be used to change to location responses
     */
    private Button preset2;

    /**
     * The third preset button that can be used to change to location responses
     */
    private Button preset3;

    /**
     * Adapter to set message chat view content
     */
    private ChatViewAdapter chatAdapter;

    /**
     * Current context of the application
     */
    private Context context;

    /**
     * The input text to be read
     */
    private EditText inputText;

    /**
     * The API Client used to connect to Google Places
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Int to keep track of the current preset id
     */
    private int id = 0;

    /**
     * Used to access permission request
     */
    public static final int PERMISSIONS_SINGLE_REQUEST = 12;

    /**
     * Keeps track of request code for the speech recognizer intent
     */
    private static final int SPEECH_REQUEST_CODE = 0;

    /**
     * Lineary layout for the presets
     */
    private LinearLayout presetValue;

    /**
     * The ListView for the messages
     */
    private ListView chatList;

    /**
     * The database to store custom presets
     */
    private SQLiteHelper dbhelp;

    /**
     * The TextToSpeech object used to read aloud input
     */
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        // Create SQLite database
        dbhelp = new SQLiteHelper(this);

        // Instantiate view
        inputText = (EditText) findViewById(R.id.inputText);
        inputButton = (Button) findViewById(R.id.inputButton);
        addPresetButton = (Button) findViewById(R.id.addPreset);
        presetValue = (LinearLayout) findViewById(R.id.presetValues);
        chatList = (ListView) findViewById(R.id.chatView);
        preset1 = (Button) findViewById(R.id.preset1);
        preset2 = (Button) findViewById(R.id.preset2);
        preset3 = (Button) findViewById(R.id.preset3);
        DrawableCompat.setTint(inputButton.getBackground(),
                ContextCompat.getColor(this, R.color.colorAccent));
        DrawableCompat.setTint(addPresetButton.getBackground(),
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

        // Build Client to connect to Google Places
        buildGoogleApiClient();
        if (checkLocationPermission()) {
            // Get result back from Google Places to retrieve nearby places
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);

            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                    if (placeLikelihoods.getCount() <= 0) {
                        // If there are no places found nearby
                        Toast.makeText(context, "No nearby locations", Toast.LENGTH_SHORT).show();
                    } else {
                        setLocationPreset(placeLikelihoods.get(0).getPlace());
                    }
                    placeLikelihoods.release();
                }
            });
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
        // Increment ID for new preset button
        id++;

        // Create and set the style of the new button
        Drawable d = getResources().getDrawable(R.drawable.roundedshapebtn);
        Button newButton = new Button(getApplicationContext());
        newButton.setId(id);
        newButton.setText(preset);
        newButton.setBackground(d);
        newButton.setPadding(20, 0, 20, 0);
        newButton.setTextColor(getApplication().getResources().getColor(R.color.white));
        LinearLayout.LayoutParams customValues =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        customValues.setMargins(0, 0, 15, 0);

        // Add a click listener for the new button
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presetClicked(v);
            }
        });

        presetValue.addView(newButton, customValues);
    }

    /**
     * Sets text in preset button as input text
     * @param v the Button that was clicked
     */
    private void presetHelp(View v){
        Button presetButton = (Button) findViewById(v.getId());
        String toSpeak = presetButton.getText().toString();
        inputText.setText(toSpeak);
    }

    /**
     * Check to see if the user has allowed permissions for reading storage and accessing location.
     */
    private void checkPermission() {
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
     * Build the API client for Google Places
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Toast.makeText(context, "Connection to location suspended",
                                Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Set Location presets based on the kind of place it is
     * @param currentPlace the current place the user is located
     */
    private void setLocationPreset(Place currentPlace) {
        // Get the list of types associated with the current place
        List<Integer> listOfTypes = currentPlace.getPlaceTypes();

        // Get the main type of the place
        Integer mainTypeInt = listOfTypes.get(0);

        // Retrieve the type of place in String format
        Field[] fields = Place.class.getDeclaredFields();
        String placeType = "";
        for (Field field : fields) {
            Class<?> type = field.getType();

            if(type == int.class) {
                try {
                    if(mainTypeInt == field.getInt(null)) {
                        Log.i("Testing", "onCreate: " + field.getName());
                        placeType = field.getName();
                        break;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // Switch based on the type of place and give context aware predictions
        switch (placeType) {
            case "TYPE_AIRPORT":
                preset1.setText(R.string.airport1);
                preset1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presetHelp(preset1);
                    }
                });
                preset2.setText(R.string.airport2);
                preset3.setText(R.string.airport3);
                break;
            case "TYPE_BANK":
                preset1.setText(R.string.bank1);
                preset2.setText(R.string.bank2);
                preset3.setText(R.string.bank3);
                break;
            case "TYPE_BAR":
                preset1.setText(R.string.bar1);
                preset1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presetHelp(preset1);
                    }
                });
                preset2.setText(R.string.bar2);
                preset3.setText(R.string.bar3);
                break;
            case "TYPE_CAFE":
                preset1.setText(R.string.cafe1);
                preset1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presetHelp(preset1);
                    }
                });
                preset2.setText(R.string.cafe2);
                preset3.setText(R.string.cafe3);
                break;
            case "TYPE_DOCTOR":
                preset1.setText(R.string.doctor1);
                preset1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presetHelp(preset1);
                    }
                });
                preset2.setText(R.string.doctor2);
                preset3.setText(R.string.doctor3);
                break;
            case "TYPE_GROCERY":
                preset1.setText(R.string.grocery1);
                preset1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presetHelp(preset1);
                    }
                });
                preset2.setText(R.string.grocery2);
                preset3.setText(R.string.grocery3);
                break;
            case "TYPE_PHARMACY":
                preset1.setText(R.string.pharmacy1);
                preset2.setText(R.string.pharmacy2);
                preset3.setText(R.string.pharmacy3);
                break;
            case "TYPE_RESTAURANT":
                preset1.setText(R.string.restaurant1);
                preset2.setText(R.string.restaurant2);
                preset3.setText(R.string.restaurant3);
                break;
            case "TYPE_UNIVERSITY":
                preset1.setText(R.string.university1);
                preset1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presetHelp(preset1);
                    }
                });
                preset2.setText(R.string.university2);
                preset3.setText(R.string.university3);
                break;
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

    /**
     * Called when the API client starts
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /**
     * Called when the API client stops
     */
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Called when the API client connects
     */
    @Override
    public void onConnected(Bundle bundle) {

    }

    /**
     * Called when the API client disconnects
     */
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(context, "Connection to location suspended", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the API client connection fails
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection to location failed", Toast.LENGTH_LONG).show();
    }
}

