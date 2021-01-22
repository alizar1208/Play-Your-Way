package com.example.playyourway;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class Details_games  extends Fragment {
    private TextView distance,place,captain,teamname,time,date;
    private String teamkey,type,tn1;
    private DatabaseReference ClickPostRef,CapRef,userRef;
    private ImageView chaticon;
    private FirebaseAuth mAuth;
    private CircleImageView img,navigation;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View GameView = inflater.inflate(R.layout.details_games, container, false);

        Bundle bundle = getArguments();
        teamkey = bundle.getString("teamkey");
        type = bundle.getString("type");

        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Teams").child(type).child(teamkey);

        distance = GameView.findViewById(R.id.distance1);
        place = GameView.findViewById(R.id.place1);
        captain = GameView.findViewById(R.id.captain1);
        teamname = GameView.findViewById(R.id.teamname1);
        time = GameView.findViewById(R.id.time1);
        date = GameView.findViewById(R.id.date1);
        chaticon = GameView.findViewById(R.id.chat);
        img = GameView.findViewById(R.id.capimg);
        navigation = GameView.findViewById(R.id.navigate);

        mAuth = FirebaseAuth.getInstance();
        String currentuserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserID).child("location");


        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(getActivity(),Directions.class);
                intent.putExtra("teamkey",teamkey);
                intent.putExtra("type",type);
                startActivity(intent);
            }
        });


        chaticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(getActivity(),GroupChat.class);
                intent.putExtra("teamkey",teamkey);
                intent.putExtra("teamname",tn1);
                startActivity(intent);
            }
        });

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String tn = dataSnapshot.child("teamname").getValue().toString();
                tn1 = tn;
                String t = dataSnapshot.child("time").getValue().toString();
                String d = dataSnapshot.child("date").getValue().toString();
                String capkey = dataSnapshot.child("captain").getValue().toString();
                String p = dataSnapshot.child("address").getValue().toString();
                final String lat = dataSnapshot.child("latitude").getValue().toString();
                final String lon = dataSnapshot.child("longitude").getValue().toString();

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String slat = dataSnapshot.child("latitude").getValue().toString();
                        String slon = dataSnapshot.child("longitude").getValue().toString();

                        float results[] = new float[10];
                        Location.distanceBetween(Double.parseDouble(slat),Double.parseDouble(slon),Double.parseDouble(lat),Double.parseDouble(lon),results);
                        String d = String.valueOf((int)results[0]/1000);
                        String m = String.valueOf((int)((int)(results[0]%1000)-(int)(results[0]%100))/100);
                        distance.setText(d+"."+m+" km Away");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                place.setText(p);
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


        return GameView;
    }
}
