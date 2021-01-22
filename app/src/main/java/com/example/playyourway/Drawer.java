package com.example.playyourway;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Drawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    ViewFlipper v_flipper;

   private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    String currentuserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        mAuth = FirebaseAuth.getInstance();
        currentuserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        int images[] = {R.drawable.cricket,R.drawable.football,R.drawable.fa,R.drawable.volleyball, R.drawable.badminton, R.drawable.basketball};

        v_flipper = findViewById(R.id.v_flipper);

        for(int image : images){

            flipperImages(image);

        }




        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navView = navigationView.inflateHeaderView(R.layout.nav_header_drawer);


         NavProfileImage = (CircleImageView)navView.findViewById(R.id.nav_profile_image);
         NavProfileUserName = (TextView)navView.findViewById(R.id.nav_user_fullname);

        NavProfileUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Drawer.this,ProfileActivity.class);
                startActivity(intent);

            }
        });

        UsersRef.child(currentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String fullname = dataSnapshot.child("firstname").getValue().toString();
                    String image = dataSnapshot.child("profileimage").getValue().toString();
                    String lastname = dataSnapshot.child("lastname").getValue().toString();


                    NavProfileUserName.setText(fullname+" " +lastname);
                    Picasso.get().load(image).into(NavProfileImage);
                }
                else
                {
                    Toast.makeText(Drawer.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(current_user_id)){

                }
                else {
                   sendusertomainactivity();

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(currentUser==null){
           sendusertomainactivity();
        }

    }


    private void sendusertomainactivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_myfriends) {
            Intent intent = new Intent(Drawer.this,MyFriends.class);
            startActivity(intent);

        } else if (id == R.id.nav_addfriends) {
            Intent intent = new Intent(Drawer.this,AddFriendsRequest.class);
            startActivity(intent);

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_tournaments) {

        } else if (id == R.id.nav_mygames) {
            Intent intent = new Intent(Drawer.this,MyGames.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            sendusertomainactivity();

        } else if (id == R.id.nav_send) {
        }
        else if (id == R.id.nav_profile) {

            Intent intent = new Intent(Drawer.this,ProfileActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share){
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void join(View view) {
        Intent intent = new Intent(this,Host.class);
        startActivity(intent);
    }

    public void host(View view) {
        Intent intent = new Intent(this,HostGame.class);
        startActivity(intent);
    }
    public void flipperImages(int image){
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(image);

        v_flipper.addView(imageView);
        v_flipper.setFlipInterval(3000);
        v_flipper.setAutoStart(true);

        v_flipper.setInAnimation(this,android.R.anim.slide_in_left);
        v_flipper.setOutAnimation(this,android.R.anim.slide_out_right);

    }
}
