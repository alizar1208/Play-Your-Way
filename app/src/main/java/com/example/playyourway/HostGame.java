package com.example.playyourway;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class HostGame extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private TextView date,time,teamname;
    private ImageButton calender,clock;
    private int day,month,year;
    private Calendar mCurrentDate;
    TimePickerDialog timePickerDialog;
    private DatabaseReference UsersRef,PostRef,FriendRef,HostedRef,PlayerRef;
    private FirebaseAuth mAuth;
    private int players;
    String[] listitems;
    String currentuserID,type;
    private Button addplayers;
    int i = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_game);





        final Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.Games, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        mAuth = FirebaseAuth.getInstance();
        currentuserID =  mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentuserID);
        HostedRef = FirebaseDatabase.getInstance().getReference().child("HostedGames").child(currentuserID);

        date = (TextView)findViewById(R.id.date);
        calender = (ImageButton)findViewById(R.id.calender);
        time = (TextView)findViewById(R.id.time);
        clock = (ImageButton)findViewById(R.id.clock);
        addplayers = (Button)findViewById(R.id.addplayers);
        teamname = (TextView)findViewById(R.id.teamname);

        addplayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap hostmap = new HashMap();

                HashMap postmap = new HashMap();
                String heading1 = spinner.getSelectedItem().toString();
                String heading2 = teamname.getText().toString();
                String heading3 = date.getText().toString();
                String heading4 = time.getText().toString();
                String teamno = String.valueOf(i);
                i = i+1;
                if(heading2.isEmpty()||heading3.isEmpty()||heading4.isEmpty()) {
                    if (heading2.isEmpty()) {
                        teamname.setError("Enter Teamname!");
                    }
                    if (heading3.isEmpty()) {
                        date.setError("Select Date!");
                    }
                    if (heading4.isEmpty()) {
                        time.setError("Select Time!");
                    }
                    return;
                }

                if(heading1.equals("Cricket"))
                {
                    PostRef = FirebaseDatabase.getInstance().getReference().child("Teams").child("cricketteams");
                    type = "cricketteams";
                    players = 10;
                }
                else if(heading1.equals("Football"))
                {
                    PostRef = FirebaseDatabase.getInstance().getReference().child("Teams").child("footballteams");
                    type = "footballteams";
                    players = 10;
                }
                else if(heading1.equals("Tennis"))
                {
                    PostRef = FirebaseDatabase.getInstance().getReference().child("Teams").child("tennisteams");
                    type = "tennisteams";
                    players = 1;
                }
                else if(heading1.equals("Badminton"))
                {
                    PostRef = FirebaseDatabase.getInstance().getReference().child("Teams").child("badmintonteams");
                    type = "badmintonteams";
                    players = 1;
                }
                else if(heading1.equals("Basketball"))
                {
                    PostRef = FirebaseDatabase.getInstance().getReference().child("Teams").child("basketballteams");
                    type = "basketballteams";
                    players = 4;
                }
                else if(heading1.equals("Volleyball"))
                {
                    PostRef = FirebaseDatabase.getInstance().getReference().child("Teams").child("volleyballteams");
                    type = "volleyballteams";
                    players = 5;
                }
                String pr = String.valueOf(players);

                postmap.put("gametype",heading1);
                postmap.put("teamname",heading2);
                postmap.put("date",heading3);
                postmap.put("time",heading4);
                postmap.put("captain",currentuserID);
                postmap.put("playersremaining",pr);


                final String id = currentuserID+heading4+teamno;

                hostmap.put("status",type);
                HostedRef.child(id).updateChildren(hostmap);

                PostRef.child(id).updateChildren(postmap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            PlayerRef = FirebaseDatabase.getInstance().getReference().child("Teams").child(type).child(id).child("Players");
                            PlayerRef.child(currentuserID).child("status").setValue("Added");

                            Intent intent = new Intent(HostGame.this,AddPlayersToTeam.class);
                            intent.putExtra("teamkey",id);
                            intent.putExtra("type",type);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            String message = task.getException().getMessage();
                            Toast.makeText(HostGame.this,"Error occured!"+message,Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });


        time.setOnClickListener(new View.OnClickListener() {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(HostGame.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String am_pm;
                        if(hourOfDay<12) {
                            am_pm="AM";
                                time.setText(hourOfDay + ":" + minute + " " + am_pm);

                        }
                        else{
                            am_pm="PM";
                                time.setText(hourOfDay + ":" + minute + " " + am_pm);
                        }

                    }
                },hour,minute,false);
                timePickerDialog.show();
            }
        });
        clock.setOnClickListener(new View.OnClickListener() {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(HostGame.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String am_pm;
                        if(hourOfDay<12) {
                            am_pm="AM";
                            if(minute<10) {
                                time.setText(hourOfDay + ":0" + minute + " " + am_pm);
                            }
                            else{
                                time.setText(hourOfDay + ":" + minute + " " + am_pm);
                            }
                        }
                        else{
                            am_pm="PM";
                            if(minute<10) {
                                time.setText(hourOfDay + ":0" + minute + " " + am_pm);
                            }
                            else{
                                time.setText(hourOfDay + ":" + minute + " " + am_pm);
                            }
                        }

                    }
                },hour,minute,false);
                timePickerDialog.show();
            }
        });


        mCurrentDate = Calendar.getInstance();
        day = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        month = mCurrentDate.get(Calendar.MONTH);
        year = mCurrentDate.get(Calendar.YEAR);


        calender.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(HostGame.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        date.setText(dayOfMonth+"/"+month+"/"+year);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(HostGame.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        date.setText(dayOfMonth+"/"+month+"/"+year);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String games = parent.getItemAtPosition(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
