package com.example.playyourway;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyFriends extends AppCompatActivity {


    private View FriendsView;
    private DatabaseReference userRef,FriendRef;
    private FirebaseAuth mAuth;
    String currentuserID;
    private RecyclerView postList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);

        mAuth = FirebaseAuth.getInstance();
        currentuserID =  mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentuserID);

        postList = (RecyclerView)findViewById(R.id.search_result_list);

        postList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();



        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(FriendRef, FindFriends.class)
                        .build();
        FirebaseRecyclerAdapter<FindFriends,MyFriends.FriendsView> adapter = new FirebaseRecyclerAdapter<FindFriends, MyFriends.FriendsView>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MyFriends.FriendsView holder, int position, @NonNull FindFriends model) {
                final String userIDs = getRef(position).getKey();
                userRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String fn = dataSnapshot.child("firstname").getValue().toString();
                        String ln = dataSnapshot.child("lastname").getValue().toString();
                        String image = dataSnapshot.child("profileimage").getValue().toString();

                        Picasso.get().load(image).into(holder.userimage);

                        holder.firstname.setText(fn+" "+ln);



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        FriendRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
            }


            @NonNull
            @Override
            public FriendsView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.display_my_friends,viewGroup,false);
                FriendsView viewHolder = new FriendsView(view);
                return viewHolder;


            }
        };
        postList.setAdapter(adapter);
        adapter.startListening();



    }

    public static class FriendsView extends RecyclerView.ViewHolder{

        TextView firstname;
        CircleImageView userimage;

        public FriendsView(@NonNull View itemView) {
            super(itemView);
            userimage = itemView.findViewById(R.id.all_users_profile_image);
            firstname = itemView.findViewById(R.id.all_users_profile_full_name);
        }
    }
}
