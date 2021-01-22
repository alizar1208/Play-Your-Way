package com.example.playyourway;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class Hosted_Games extends Fragment {

    private View GamesView;
    private DatabaseReference gamesref,hostref,userRef;
    private FirebaseAuth mAuth;
    private RecyclerView postList;
    private String currentuserID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View GameView = inflater.inflate(R.layout.hosted_games, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentuserID = mAuth.getCurrentUser().getUid();
        gamesref = FirebaseDatabase.getInstance().getReference().child("Teams");
        hostref = FirebaseDatabase.getInstance().getReference().child("HostedGames").child(currentuserID);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserID).child("location");
        postList = (RecyclerView)GameView.findViewById(R.id.list);


        postList.setLayoutManager(new LinearLayoutManager(getContext()));
        return GameView;
    }

    @Override
    public void onStart() {
        super.onStart();



        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Games>()
                        .setQuery(hostref, Games.class)
                        .build();
        FirebaseRecyclerAdapter<Games,Hosted_Games.GamesView> adapter = new FirebaseRecyclerAdapter<Games, GamesView>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GamesView holder, int position, @NonNull Games model) {
                final String userIDs = getRef(position).getKey();
                hostref.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String type = dataSnapshot.child("status").getValue().toString();
                        gamesref.child(type).child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String  teamtype = dataSnapshot.child("gametype").getValue().toString();
                                String teamname1 = dataSnapshot.child("teamname").getValue().toString();
                                String date1 = dataSnapshot.child("date").getValue().toString();
                                String time1 = dataSnapshot.child("time").getValue().toString();
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
                                        holder.distance.setText(d+"."+m+" km Away");

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                holder.teamname.setText(teamtype+"  :  "+ teamname1);
                                holder.time.setText(time1);
                                holder.date.setText(date1);

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity(),ClickHostGame.class);
                                        intent.putExtra("teamkey",userIDs);
                                        intent.putExtra("child",type);
                                        startActivity(intent);
                                    }
                                });




                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                hostref.child(userIDs).addValueEventListener(new ValueEventListener() {
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }


            @NonNull
            @Override
            public GamesView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.hosted_games_tile,viewGroup,false);
                GamesView viewHolder = new GamesView(view);
                return viewHolder;


            }
        };
        postList.setAdapter(adapter);
        adapter.startListening();



    }

    public static class GamesView extends RecyclerView.ViewHolder{

        TextView teamname,date,time,distance;

        public GamesView(@NonNull View itemView) {
            super(itemView);
            teamname = itemView.findViewById(R.id.teamname);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            distance = itemView.findViewById(R.id.Distance);

        }
    }
}
