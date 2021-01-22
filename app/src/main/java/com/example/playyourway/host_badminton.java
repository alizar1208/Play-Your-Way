package com.example.playyourway;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.playyourway.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class host_badminton extends Fragment {

    private View GameView;
    private DatabaseReference gamesref,reqhostref,userref;
    private FirebaseAuth mAuth;
    private RecyclerView postList;
    private String currentuserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View GameView = inflater.inflate(R.layout.host_cricket, container, false);

        gamesref = FirebaseDatabase.getInstance().getReference().child("Teams").child("badmintonteams");
        mAuth = FirebaseAuth.getInstance();
        currentuserId = mAuth.getCurrentUser().getUid();
        userref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserId);
        reqhostref = FirebaseDatabase.getInstance().getReference().child("Teams").child("badmintonteams");
        mAuth = FirebaseAuth.getInstance();

        postList = (RecyclerView)GameView.findViewById(R.id.list);


        postList.setLayoutManager(new LinearLayoutManager(getContext()));



        return GameView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Games>()
                        .setQuery(gamesref, Games.class)
                        .build();
        FirebaseRecyclerAdapter<Games,GamesView>adapter = new FirebaseRecyclerAdapter<Games, GamesView>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GamesView holder, int position, @NonNull Games model) {

                final String teamkey = getRef(position).getKey();
                final String childid = "badmintonteams";
                String teamIDs = getRef(position).getKey();
                gamesref.child(teamIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String teamname1 = dataSnapshot.child("teamname").getValue().toString();
                        String date1 = dataSnapshot.child("date").getValue().toString();
                        String time1 = dataSnapshot.child("time").getValue().toString();
                        String type = dataSnapshot.child("gametype").getValue().toString();
                        String pr = dataSnapshot.child("playersremaining").getValue().toString();
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
                                holder.dis.setText(d+"."+m+" km Away");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        holder.pl.setText(pr + " Players left");
                        {
                            holder.teamname.setText(teamname1);
                            holder.time.setText(time1);
                            holder.date.setText(date1);
                        }
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(),ClickPostActivity.class);
                                intent.putExtra("teamkey",teamkey);
                                intent.putExtra("child",childid);
                                startActivity(intent);
                            }
                        });
                        holder.request.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HashMap hostmap = new HashMap();
                                String currentuserId = mAuth.getCurrentUser().getUid();
                                hostmap.put("status","badminton");
                                reqhostref.child(teamkey).child("Requests").child(currentuserId).updateChildren(hostmap);
                                Toast.makeText(getActivity(), "Request Successful.", Toast.LENGTH_LONG).show();

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
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.displaygame,viewGroup,false);
                GamesView viewHolder = new GamesView(view);
                return viewHolder;


            }
        };
        postList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class GamesView extends RecyclerView.ViewHolder{

        TextView teamname,date,time,pl,dis;
        Button request;

        public GamesView(@NonNull View itemView) {
            super(itemView);
            teamname = itemView.findViewById(R.id.teamname);
            date = itemView.findViewById(R.id.date);
            pl = itemView.findViewById(R.id.playersleft);
            request = itemView.findViewById(R.id.request);
            time = itemView.findViewById(R.id.time);
            dis = itemView.findViewById(R.id.Distance);
        }
    }
}

