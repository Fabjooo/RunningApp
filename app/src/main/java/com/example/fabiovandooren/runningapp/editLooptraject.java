package com.example.fabiovandooren.runningapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class editLooptraject extends AppCompatActivity {

    EditText editTextDatum;
    EditText editTextKms;
    Button buttonUpdateLoopTraject;
    DatabaseReference databaseLoopTraject;
    NumberPicker numberPicker = null;
    String kms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_looptraject);

        databaseLoopTraject = FirebaseDatabase.getInstance().getReference("LoopTrajecten/");

        editTextDatum = (EditText)  findViewById(R.id.editDate);
        //editTextKms = (EditText) findViewById(R.id.editAantalKms);
        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);

        buttonUpdateLoopTraject = (Button) findViewById(R.id.buttonUpdateLoopTraject);
        buttonUpdateLoopTraject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLoopTraject();
            }
        });

        //Number Picker
        numberPicker.setMaxValue(40);
        numberPicker.setMinValue(1);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setOnValueChangedListener( new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int
                    oldVal, int newVal) {
                kms = "" + newVal;
            }
        });

        String s = getIntent().getStringExtra("LOOPTRAJECT_ID");
        Query queryRef = databaseLoopTraject.orderByChild("loopTrajectId").equalTo(s);
        Log.e("QUERYREF-ID", "queryref" + queryRef);
    }

    //LoopTraject updaten online via Firebase Google
    public void updateLoopTraject(){

        String datum = editTextDatum.getText().toString();
        //String kms = editTextKms.getText().toString();
        //kms = numberPicker.getValue();

        if(!TextUtils.isEmpty(kms) && !TextUtils.isEmpty(datum)){
            String id = databaseLoopTraject.push().getKey();
            LoopTraject loopTraject = new LoopTraject(id, datum, kms);
            databaseLoopTraject.child(id).setValue(loopTraject);

            Toast.makeText(this, "Looptraject geupdated!", Toast.LENGTH_LONG).show();
            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);


        }
        else if (TextUtils.isEmpty(kms) && TextUtils.isEmpty(datum)){
            Toast.makeText(this, "Vergeet de datum en je aantal kilometers niet in te vullen.", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(kms)){
            Toast.makeText(this, "Vul je aantal kilometers in.", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "Vul de datum in.", Toast.LENGTH_LONG).show();
        }
    }
}
