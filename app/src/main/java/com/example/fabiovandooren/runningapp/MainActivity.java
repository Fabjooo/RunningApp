package com.example.fabiovandooren.runningapp;

import android.content.Intent;
import android.os.Bundle;
import java.util.Locale;

import android.speech.tts.Voice;
import android.util.Log;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.share.widget.ShareButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TextToSpeech.OnInitListener {

    DatabaseReference databaseLoopTraject;
    ListView listViewLooptrajecten;
    Button shareButton;
    Intent shareIntent;
    String shareBody = "Je hebt gelopen!";
    private Button speakButton;
    private TextToSpeech myWiseWords;
    List<LoopTraject> loopTrajectList;

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
        Switch sortDate = (Switch) findViewById(R.id.switch2);
        Switch sortDistance = (Switch) findViewById(R.id.switch3);

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
                    }
                });


        sortDate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    sortDateAsc();
                } else {
                    sortDateDesc();
                }
            }
        });

        sortDistance.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    sortDistanceAsc();
                } else {
                    sortDistanceDesc();
                }
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

    }

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
        String words = "Hi my name is Fabio Van Dooren and I have a message for Koen Pellegrims: Android has very powerful mechanisms, thanks for being our teacher this year!";
        //SPEAK THEM WISE WORDS :O
        myWiseWords.speak(words, TextToSpeech.QUEUE_FLUSH, null);
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
                    public int compare(LoopTraject o1, LoopTraject o2) {
                        if (o1.getLoopTrajectDatum() == null || o2.getLoopTrajectDatum() == null)
                            return 0;
                        return o2.getLoopTrajectDatum().compareTo(o1.getLoopTrajectDatum());
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

    public void sortDateAsc() {
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
                        if (o1.getLoopTrajectDatum() == null || o2.getLoopTrajectDatum() == null)
                            return 0;
                        return o1.getLoopTrajectDatum().compareTo(o2.getLoopTrajectDatum());
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
                        if (o1.getLoopTrajectKms() == null || o2.getLoopTrajectKms() == null)
                            return 0;
                        return o2.getLoopTrajectKms().compareTo(o1.getLoopTrajectKms());
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

    public void sortDistanceAsc() {
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
                        if (o1.getLoopTrajectKms() == null || o2.getLoopTrajectKms() == null)
                            return 0;
                        return o1.getLoopTrajectKms().compareTo(o2.getLoopTrajectKms());
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
