package com.example.playyourway;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

public class Players_games extends Fragment {

    private String teamkey,type;
    private DatabaseReference userRef,PlayerRef;
    private View RequestView;
    private FirebaseAuth mAuth;
    private RecyclerView postList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View GameView = inflater.inflate(R.layout.games_request, container, false);


        Bundle bundle = getArguments();
        teamkey = bundle.getString("teamkey");
        type = bundle.getString("type");
        mAuth = FirebaseAuth.getInstance();
        PlayerRef = FirebaseDatabase.getInstance().getReference().child("Teams").child(type).child(teamkey).child("Players");

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postList = (RecyclerView)GameView.findViewById(R.id.list);
        postList.setLayoutManager(new LinearLayoutManager(getContext()));

        return GameView;
    }
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(PlayerRef, FindFriends.class)
                        .build();
        FirebaseRecyclerAdapter<FindFriends,Players_games.RequestView> adapter = new FirebaseRecyclerAdapter<FindFriends, Players_games.RequestView>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final Players_games.RequestView holder, int position, @NonNull FindFriends model) {

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

                        PlayerRef.child(userIDs).addValueEventListener(new ValueEventListener() {
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
            public RequestView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.display_my_friends,viewGroup,false);
                RequestView viewHolder = new RequestView(view);
                return viewHolder;


            }
        };
        postList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestView extends RecyclerView.ViewHolder{

        TextView firstname;
        CircleImageView userimage;

        public RequestView(@NonNull View itemView) {
            super(itemView);
            firstname = itemView.findViewById(R.id.all_users_profile_full_name);
            userimage = itemView.findViewById(R.id.all_users_profile_image);
        }
    }

}
