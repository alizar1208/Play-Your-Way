package com.example.playyourway;

import android.graphics.Paint;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClickPostActivity extends AppCompatActivity {

    private TextView distance,place,captain,teamname,time,date;
    private String teamkey,type;
    private FirebaseAuth mAuth;
    private DatabaseReference ClickPostRef,CapRef,userref;
    private CircleImageView img;
    private String currentuserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        teamkey= getIntent().getExtras().get("teamkey").toString();
        type= getIntent().getExtras().get("child").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Teams").child(type).child(teamkey);

        mAuth = FirebaseAuth.getInstance();
        currentuserId = mAuth.getCurrentUser().getUid();
        userref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserId);


        distance = findViewById(R.id.distance1);
        place = findViewById(R.id.place1);
        captain = findViewById(R.id.captain1);
        teamname = findViewById(R.id.teamname1);
        time = findViewById(R.id.time1);
        date = findViewById(R.id.date1);
        img = findViewById(R.id.capimg);


        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String tn = dataSnapshot.child("teamname").getValue().toString();
                String t = dataSnapshot.child("time").getValue().toString();
                String d = dataSnapshot.child("date").getValue().toString();
                String capkey = dataSnapshot.child("captain").getValue().toString();
                String p = dataSnapshot.child("address").getValue().toString();
                place.setText(p);
                final String lat = dataSnapshot.child("latitude").getValue().toString();
                final String lon = dataSnapshot.child("longitude").getValue().toString();


                final double end_lat,end_lon;
                end_lat = Double.parseDouble(lat);
                end_lon = Double.parseDouble(lon);
                userref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String slat = dataSnapshot.child("location").child("latitude").getValue().toString();
                        String slon = dataSnapshot.child("location").child("longitude").getValue().toString();
                        double latitude,longitude;
                        latitude = Double.parseDouble(slat);
                        longitude = Double.parseDouble(slon);
                        float results[] = new float[10];
                        Location.distanceBetween(latitude,longitude,end_lat,end_lon,results);
                        String d = String.valueOf((int)results[0]/1000);
                        String m = String.valueOf((int)((int)(results[0]%1000)-(int)(results[0]%100))/100);
                        distance.setText(d+"."+m+" km Away");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                CapRef = FirebaseDatabase.getInstance().getReference().child("Users").child(capkey);
                CapRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String fn = dataSnapshot.child("firstname").getValue().toString();
                        String ln = dataSnapshot.child("lastname").getValue().toString();
                        captain.setText(fn+" "+ln);
                        String image = dataSnapshot.child("profileimage").getValue().toString();

                        Picasso.get().load(image).into(img);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                teamname.setText(tn);
                time.setText(t);
                date.setText(d);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
