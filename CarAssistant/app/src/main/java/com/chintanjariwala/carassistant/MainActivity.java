package com.chintanjariwala.carassistant;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity{

    private TextView speechInput;
    private final int INPUT_SPEECH_CODE = 100;

    Button speechBtn;

    GPSTracker gps;

    BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speechInput = (TextView) findViewById(R.id.txtSpeechInput);
        speechBtn = (Button) findViewById(R.id.btnSpeak);


        speechBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    private void promptSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, INPUT_SPEECH_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case INPUT_SPEECH_CODE: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speechInput.setText(result.get(0));
                    if (result.get(0).toString().equals("Bluetooth on")) {
                        if (!bluetooth.isEnabled()) {
                            Toast.makeText(getApplicationContext(),
                                    "Turning ON Bluetooth", Toast.LENGTH_LONG);
                            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
                        }

                    }
                    if (result.get(0).toString().equals("Bluetooth off")) {
                        if (bluetooth.isEnabled()) {
                            Toast.makeText(getApplicationContext(),
                                    "TURNING OFF BLUETOOTH", Toast.LENGTH_LONG);
                            bluetooth.disable();
                        }


                    }

                    if (result.get(0).toString().equalsIgnoreCase("Open google maps")) {

                        gps = new GPSTracker(MainActivity.this);
                        double latitude = 0;
                        double longitude = 0;
                        if(gps.canGetLocation()){

                            latitude = gps.getLatitude();
                             longitude = gps.getLongitude();

                            // \n is for new line
                            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                        }else{
                            // can't get location
                            // GPS or Network is not enabled
                            // Ask user to enable GPS/network in settings
                            gps.showSettingsAlert();
                        }

                        Uri gmmIntentUri = Uri.parse("geo:"+latitude+","+longitude);

                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Google Maps is not installed on your device", Toast.LENGTH_LONG).show();
                        }

                    }

                    if (result.get(0).toString().equalsIgnoreCase("Open Call Logs")) {
                        Intent showCallLog = new Intent();
                        showCallLog.setAction(Intent.ACTION_VIEW);
                        showCallLog.setType(CallLog.Calls.CONTENT_TYPE);
                        showCallLog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        showCallLog.putExtra(CallLog.Calls.TYPE, CallLog.Calls.MISSED_TYPE);
                        startActivity(showCallLog);
                    }

                    if (result.get(0).toString().equalsIgnoreCase("Help") || result.get(0).toString().equalsIgnoreCase("Emergency")) {

                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:911"));
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(callIntent);
                    }

                    if (result.get(0).toString().toLowerCase().contains("play") ) {
                        Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
                        startActivity(intent);
                         }

                    if (result.get(0).toString().toLowerCase().contains("nearby")) {

                        String search = result.get(0).toString().toLowerCase().replace("nearby","");
                        gps = new GPSTracker(MainActivity.this);
                        double latitude = 0;
                        double longitude = 0;
                        if(gps.canGetLocation()){

                            latitude = gps.getLatitude();
                            longitude = gps.getLongitude();

                            // \n is for new line
                            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                        }else{
                            // can't get location
                            // GPS or Network is not enabled
                            // Ask user to enable GPS/network in settings
                            gps.showSettingsAlert();
                        }

                        Uri gmmIntentUri = Uri.parse("geo:"+latitude+","+longitude+"?q="+search);

                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Google Maps is not installed on your device", Toast.LENGTH_LONG).show();
                        }

                    }

                    if(result.get(0).toString().toLowerCase().contains("navigate to")){
                        String s = result.get(0).toString();
                        String city = s.replace("navigate to","");


                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+city);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");

                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Google Maps is not installed on your device", Toast.LENGTH_LONG).show();
                        }
                    }

                    if(result.get(0).toString().toLowerCase().contains("send a message to")){
                        int MY_PERMISSIONS_REQUEST_READ_CONTACTS=0;
                        // Here, thisActivity is the current activity
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_CONTACTS)
                                != PackageManager.PERMISSION_GRANTED) {

                            // Should we show an explanation?
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                    Manifest.permission.READ_CONTACTS)) {

                                // Show an expanation to the user *asynchronously* -- don't block
                                // this thread waiting for the user's response! After the user
                                // sees the explanation, try again to request the permission.

                            } else {

                                // No explanation needed, we can request the permission.

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_CONTACTS},
                                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                                // app-defined int constant. The callback method gets the
                                // result of the request.
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),
                                    "Inside", Toast.LENGTH_LONG).show();
                            String s = result.get(0).toString();
                            String msg = s.replace("send a message to","");
                            String number=null;
                            boolean isitthere = false;

                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
                            while (phones.moveToNext())
                            {
                                String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                Log.d("String : - ",name);
                                if(msg.toLowerCase().contains(name.toLowerCase())){
                                    msg = msg.replace(name,"");
                                    Toast.makeText(getApplicationContext(),
                                            "We have the same name", Toast.LENGTH_LONG).show();
                                    number=phoneNumber;
                                    isitthere = true;
                                }
                            }
                            phones.close();

                            if(isitthere){
                                Uri uri = Uri.parse("smsto:"+number);
                                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                                it.putExtra("sms_body", msg);
                                startActivity(it);
                            }else{
                                Toast.makeText(getApplicationContext(),
                                        "I am Sorry we could not find the contact", Toast.LENGTH_LONG).show();
                            }
                        }


                    }

                    if(result.get(0).toString().toLowerCase().contains("search")){
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setClassName("com.google.android.googlequicksearchbox",
                                "com.google.android.googlequicksearchbox.VoiceSearchActivity");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException anfe) {
                            Toast.makeText(getApplicationContext(),
                                    "Cannot find Googel voice command", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    }
}
