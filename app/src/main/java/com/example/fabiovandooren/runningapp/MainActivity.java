package com.example.fabiovandooren.runningapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.util.Log;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TextToSpeech.OnInitListener {

    DatabaseReference databaseLoopTraject;
    ListView listViewLooptrajecten;
    String looptrajectID;
    String datumText;
    String kmsText;
    Button shareButton;
    Intent shareIntent;
    String shareBody = "Ik heb op [datum] [x aantal] kilometers gelopen!";
    private Button speakButton;
    private TextToSpeech myWiseWords;
    List<LoopTraject> loopTrajectList;
    Button locationButton;
    LocationManager location_manager;
    double x;
    double y;
    Geocoder geocoder;
    List<Address> addresses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        //checken of je ingelogd bent
        if (AccessToken.getCurrentAccessToken() == null) {
            openLoginScreen();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //SORTING DROPDOWN

        //Switch sortDate = (Switch) findViewById(R.id.switch2);
        //Switch sortDistance = (Switch) findViewById(R.id.switch3);
        Spinner mySpinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.sorting));

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    Log.d("tag", "date");
                    sortDateDesc();
                }

                if(position == 1){
                    Log.d("tag", "distance");
                    sortDistanceDesc();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //END SORTING DROPDOWN

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        databaseLoopTraject = FirebaseDatabase.getInstance().getReference("LoopTrajecten/");

        listViewLooptrajecten = (ListView) findViewById(R.id.listViewTrajecten);
        loopTrajectList = new ArrayList<>();

        shareButton = (Button) findViewById(R.id.fb_share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "LoopApp");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share via: "));
            }
        });


        FirebaseDatabase.getInstance().getReference().child("LoopTrajecten") //check database voor table LoopTrajecten
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            LoopTraject loopTraject = snapshot.getValue(LoopTraject.class);
                            System.out.println(loopTraject.getLoopTrajectDatum() + "met aantal gelopen kms: " + loopTraject.loopTrajectKms);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Couldn't reach Database.", Toast.LENGTH_SHORT).show();
                    }
                });


        /*https://stackoverflow.com/questions/3184672/what-does-adapterview-mean-in-the-onitemclick-method-what-is-the-use-of-ot*/

        final ListView listView = (ListView) findViewById(R.id.listViewTrajecten);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                TextView tvDatum = (TextView) view.findViewById(R.id.textViewDatum);
                datumText = tvDatum.getText().toString();

                TextView tvKms = (TextView) view.findViewById(R.id.textViewKms);
                kmsText = tvKms.getText().toString();

                TextView tvID = (TextView) view.findViewById(R.id.textViewID);
                String IDText = tvID.getText().toString();


                //looptrajectID = databaseLoopTraject.child("LoopTrajecten").getKey();
                System.out.println("Looptraject id: " + IDText);
                System.out.println("KMS: " + kmsText);

                openUpdateLooptrajectScreen(IDText);

                //Toast.makeText(MainActivity.this, "myPos " + i + " Datum: " + datumText + " Kms: " + kmsText, Toast.LENGTH_LONG).show();
            }
        });


        //SET THE FUNCTION "speakWiseWords" READY FOR KOEN PELLEGRIMS
        myWiseWords = new TextToSpeech(this, this);
        speakButton = (Button) findViewById(R.id.speak);
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakWiseWords();
            }
        });


        location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationButton = (Button) findViewById(R.id.location_button);
        locationButton.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View arg0) {

                //Request current location using GPS
                LocationListener listener = new MyLocationListener();
                location_manager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 0, 100, listener);
            }
        });

    }

    /*
    *
    * END OF ONCREATE()
    *
    * */

    //Check if languague is set or if it is supported
    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = myWiseWords.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakButton.setEnabled(true);
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }
        //Let this Android-phone speak wise words!
    private void speakWiseWords() {
        //Set the desired wise text
        String words = "Hey what's up Koen Pellegrims, we are Fabio Van Dooren and Robber Reygel and we have a message for you. Android has very powerful mechanisms! By the way: thanks for being our teacher this year!";
        //SPEAK THEM WISE WORDS :O
        myWiseWords.speak(words, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void openUpdateLooptrajectScreen(String IDText) {
        Intent intent = new Intent(this, editLooptraject.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("LOOPTRAJECT_ID", IDText);
        intent.putExtra("datumText", datumText);
        intent.putExtra("kmsText", kmsText);
        //Toast.makeText(MainActivity.this, "Looptraject id: " + IDText , Toast.LENGTH_LONG).show();

        startActivity(intent);
    }

    private void openLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logout(View view){
        LoginManager.getInstance().logOut();
        openLoginScreen();
    }


    protected void onStart(){
        super.onStart();

        sortDateDesc();

    }

    public void sortDateDesc() {
        databaseLoopTraject.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                loopTrajectList.clear(); // eerst leegmaken vooraleer we alles er bij gaan zetten.

                for (DataSnapshot loopTrajectSnapshot: dataSnapshot.getChildren() ){
                    LoopTraject loopTraject = loopTrajectSnapshot.getValue(LoopTraject.class);

                    loopTrajectList.add(loopTraject);
                }

                Collections.sort(loopTrajectList, new Comparator<LoopTraject>() {
                    DateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
                    @Override
                    public int compare(LoopTraject o1, LoopTraject o2) {
                        String date1 = o1.getLoopTrajectDatum();
                        String date2 = o2.getLoopTrajectDatum();

                        try {
                            return format.parse(date2).compareTo(format.parse(date1));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            throw new IllegalArgumentException(e);
                        }
                    }
                    /*
                    public int compare(LoopTraject o1, LoopTraject o2) {
                        if (o1.getLoopTrajectDatum() == null || o2.getLoopTrajectDatum() == null)
                            return 0;
                        return o2.getLoopTrajectDatum().compareTo(o1.getLoopTrajectDatum());
                    }
                    */
                });

                LoopTrajectList adapter = new LoopTrajectList(MainActivity.this, loopTrajectList);
                listViewLooptrajecten.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    public void sortDistanceDesc() {
        databaseLoopTraject.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                loopTrajectList.clear(); // eerst leegmaken vooraleer we alles er bij gaan zetten.

                for (DataSnapshot loopTrajectSnapshot: dataSnapshot.getChildren() ){
                    LoopTraject loopTraject = loopTrajectSnapshot.getValue(LoopTraject.class);

                    loopTrajectList.add(loopTraject);
                }

                Collections.sort(loopTrajectList, new Comparator<LoopTraject>() {
                    public int compare(LoopTraject o1, LoopTraject o2) {
                        Integer distance1 = Integer.parseInt(o1.getLoopTrajectKms());
                        Integer distance2 = Integer.parseInt(o2.getLoopTrajectKms());

                        return distance2.compareTo(distance1);

                    }
                });

                LoopTrajectList adapter = new LoopTrajectList(MainActivity.this, loopTrajectList);
                listViewLooptrajecten.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    //functie apart gezet omdat de floating button al een onclickListener van zichzelf is
    //dus OnClick in de XML laten verwijzen naar deze functie :)
    public void inputlooptraject(View view){
        Intent nieuwlooptraject = new Intent(this,inputLooptraject.class);
        startActivity(nieuwlooptraject);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String holdup = "Opening...";

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Toast.makeText(getApplicationContext(), holdup + "Camera", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }else if (id == R.id.nav_logout) {
            LoginManager.getInstance().logOut();
            openLoginScreen();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class MyLocationListener implements LocationListener {

        @SuppressWarnings("static-access")
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onLocationChanged(Location location) {
            //set long & lat coords of current location
            x = location.getLatitude();
            y = location.getLongitude();

            try {
                //call geocoder to get location out of long & lat
                geocoder = new Geocoder(MainActivity.this, Locale.ENGLISH);
                addresses = geocoder.getFromLocation(x, y, 1);
                StringBuilder str = new StringBuilder();
                if (geocoder.isPresent()) {
                    Address returnAddress = addresses.get(0);

                    String localityString = returnAddress.getLocality();
                    String city = returnAddress.getCountryName();
                    String region_code = returnAddress.getCountryCode();
                    String zipcode = returnAddress.getPostalCode();

                    //combine multiple string into one
                    str.append(localityString + " ");
                    str.append(city + "" + region_code + " ");
                    str.append(zipcode + " ");

                    //display location for 2.5s
                    Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(),"Geocoder unavailable", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }

        }

        @Override
        public void onProviderDisabled(String arg0) {


        }

        @Override
        public void onProviderEnabled(String arg0) {


        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {


        }

    }
}
