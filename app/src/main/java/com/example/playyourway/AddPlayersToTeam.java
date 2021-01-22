package com.example.playyourway;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddPlayersToTeam extends AppCompatActivity {



    private View FriendsView;
    private Button creategame;
    String teamkey,type;
    private DatabaseReference userRef,FriendRef,sendReqRef;
    private FirebaseAuth mAuth;
    String currentuserID;
    private RecyclerView postList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_players_to_team);

        teamkey= getIntent().getExtras().get("teamkey").toString();
        type= getIntent().getExtras().get("type").toString();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        currentuserID =  mAuth.getCurrentUser().getUid();
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentuserID);

        postList = (RecyclerView)findViewById(R.id.search_result_list);

        postList.setLayoutManager(new LinearLayoutManager(this));
        creategame = (Button)findViewById(R.id.sendtodrawer);
        creategame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddPlayersToTeam.this,GameLocation.class);
                intent.putExtra("teamkey",teamkey);
                intent.putExtra("type",type);
                startActivity(intent);
                finish();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();



        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(FriendRef, FindFriends.class)
                        .build();
        FirebaseRecyclerAdapter<FindFriends,AddPlayersToTeam.FriendsView> adapter = new FirebaseRecyclerAdapter<FindFriends, AddPlayersToTeam.FriendsView>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final AddPlayersToTeam.FriendsView holder, int position, @NonNull FindFriends model) {
                final String userIDs = getRef(position).getKey();
                userRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String fn = dataSnapshot.child("firstname").getValue().toString();
                        String ln = dataSnapshot.child("lastname").getValue().toString();
                        String image = dataSnapshot.child("profileimage").getValue().toString();

                        Picasso.get().load(image).into(holder.userimage);

                        holder.firstname.setText(fn+" "+ln);
                        holder.add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendReqRef = FirebaseDatabase.getInstance().getReference().child("GameRequestReceived");


                                if(!holder.add.isChecked()){
                                    sendReqRef.child(userIDs).child(teamkey).removeValue();
                                }
                                else{
                                    sendReqRef.child(userIDs).child(teamkey).child("status").setValue(type);
                                }

                            }
                        });




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
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.add_players_to_team_tile,viewGroup,false);
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
        CheckBox add;

        public FriendsView(@NonNull View itemView) {
            super(itemView);
            userimage = itemView.findViewById(R.id.all_users_profile_image);
            add = itemView.findViewById(R.id.add);
            firstname = itemView.findViewById(R.id.all_users_profile_full_name);
        }
    }
}
