package com.example.playyourway;

import android.content.Intent;
import android.net.Uri;
import com.google.android.gms.tasks.OnSuccessListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class Signup extends AppCompatActivity implements ValueEventListener {

    private EditText firstname,lastname,age,username ;
    private RadioButton male,female;
    private DatabaseReference UsersRef;
    private FirebaseAuth mAuth;
    String currentuserID;
    String gender="male";
    private Uri resultUri;
     private ImageView ProfileImage;
    final static int Gallary_Pick = 1 ;
    private StorageReference UserProfileImageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        mAuth = FirebaseAuth.getInstance();
        currentuserID =  mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile images");

        firstname = (EditText)findViewById(R.id.firstname);
        lastname = (EditText)findViewById(R.id.lastname);
        age = (EditText)findViewById(R.id.age);
        username = (EditText)findViewById(R.id.username);
        male = (RadioButton)findViewById(R.id.male);
        female = (RadioButton) findViewById(R.id.female);
        ProfileImage = (ImageView)findViewById(R.id.profileimage);
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallaryintent = new Intent();
                gallaryintent.setAction(Intent.ACTION_GET_CONTENT);
                gallaryintent.setType("image/*");
                startActivityForResult(gallaryintent,Gallary_Pick);
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallary_Pick && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
          }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                resultUri = result.getUri();
              ProfileImage.setImageURI(resultUri);


            }


        }

    }

    public void PlayHost(View view) {

        if(resultUri==null){
            Toast.makeText(Signup.this,"Profile Image  required!", Toast.LENGTH_SHORT).show();
            return;

        }

        if((firstname.getText().toString().trim().isEmpty())||
                (lastname.getText().toString().trim().isEmpty())||(age.getText().toString().trim().isEmpty())||
                (username.getText().toString().trim().isEmpty())) {
            if ((firstname.getText().toString().trim().isEmpty())) {
                firstname.setError("Enter Firstname");
            }
            if ((lastname.getText().toString().trim().isEmpty())) {
                lastname.setError("Enter Lastname");
            }
            if ((username.getText().toString().trim().isEmpty())) {
                username.setError("Enter Username");
            }
            if ((age.getText().toString().trim().isEmpty())) {
                age.setError("Enter Age");
            }


            return;
        }

        HashMap userMap = new HashMap();
        String heading1  = firstname.getText().toString();
        String heading2  = lastname.getText().toString();
        String heading3  = username.getText().toString();
        String heading4  = age.getText().toString();
        String heading6 = gender.toString();



        userMap.put("firstname",heading1);
        userMap.put("lastname",heading2);
        userMap.put("username",heading3);
        userMap.put("age",heading4);
        userMap.put("gender",heading6);
        userMap.put("profileimage","d");

        final StorageReference filepath = UserProfileImageRef.child(currentuserID + ".jpg");
        final UploadTask uploadTask = filepath.putFile(resultUri);

        filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {


                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUrl = uri.toString();

                            UsersRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    Intent intent = new Intent(Signup.this,MapsActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        }});}}});



        UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(Signup.this,MapsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                }
                else{
                    String message = task.getException().getMessage();
                    Toast.makeText(Signup.this,"Error occured!"+message,Toast.LENGTH_SHORT).show();
                }

            }
        });



    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
    @Override
    protected void onStart() {

        super.onStart();

    }

    public void onRadioclicked(View view) {
        switch(view.getId()){
            case R.id.male:
                gender = "male";
                break;
            case R.id.female:
                gender = "female";
                break;

        }

    }
}