package com.example.playyourway;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView firstname,lastname,fullname,username,district,mobileno,gender1;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private CircleImageView NavProfileImage;
    private String currentuserid,pno;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fullname = (TextView)findViewById(R.id.userfullname);
        mobileno = (TextView)findViewById(R.id.phoneno);
        district = (TextView)findViewById(R.id.district);
        NavProfileImage = (CircleImageView)findViewById(R.id.nav_profile_image);
        username   = (TextView)findViewById(R.id.username);
        gender1 = (TextView)findViewById(R.id.gender);

        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        pno = mAuth.getCurrentUser().getPhoneNumber();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserid);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String fullname1 = dataSnapshot.child("firstname").getValue().toString()+" "+dataSnapshot.child("lastname").getValue().toString();
                    String district1 = dataSnapshot.child("location").child("district").getValue().toString();
                    String username1 = dataSnapshot.child("username").getValue().toString();
                    String image = dataSnapshot.child("profileimage").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    Picasso.get().load(image).into(NavProfileImage);
                    fullname.setText(fullname1);
                    district.setText(district1);
                    username.setText(username1);
                    mobileno.setText(pno);
                    gender1.setText(gender);

                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
