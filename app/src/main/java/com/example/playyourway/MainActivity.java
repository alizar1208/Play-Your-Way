package com.example.playyourway;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity   {

    private Spinner spinner;
    Button login;
    public EditText phone;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();


    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<String>(this, R.layout.color_layout_spinner, CountryData.countryNames));


        mVideoView = (VideoView) findViewById(R.id.bgVideoView);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bg_video);
        mVideoView.setVideoURI(uri);
        mVideoView.start();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });

        phone = (EditText) findViewById(R.id.mobile);
        login = (Button) findViewById(R.id.Login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String validphone = "[6-9]{1}" + "[0-9]{9}";
                String mobile = phone.getText().toString();
                Matcher matcher1 = Pattern.compile(validphone).matcher(mobile);

                String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];

                String number = phone.getText().toString().trim();

               if (number.isEmpty() || !matcher1.matches()) {
                    phone.setError("Valid number is required");
                    phone.requestFocus();
                    return;
                }

                String phonenumber = "+" + code + number;

                Intent intent = new Intent(MainActivity.this, Otp.class);
                intent.putExtra("phonenumber", phonenumber);
                Toast.makeText(getApplicationContext(), "OTP has been sent to your Mobile Number.", Toast.LENGTH_SHORT).show();
                startActivity(intent);


            }
        });
    }

}

