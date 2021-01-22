package com.example.playyourway;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindPeople extends Fragment {
    private View FindPeopleView;
    private TextView SearchInputText;
    private ImageButton SearchButton;
    private DatabaseReference userRef,RequestRef,ReceivedRef;
    private FirebaseAuth mAuth;
    String currentuserID;
    private RecyclerView postList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.findpeople, container, false);


        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        RequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequestsSent");
        ReceivedRef = FirebaseDatabase.getInstance().getReference().child("FriendRequestsReceived");
        currentuserID =  mAuth.getCurrentUser().getUid();
        postList = (RecyclerView)rootView.findViewById(R.id.search_result_list);

        SearchInputText = (EditText)rootView.findViewById(R.id.search);
        SearchButton = (ImageButton)rootView.findViewById(R.id.search_people);



        postList.setLayoutManager(new LinearLayoutManager(getContext()));
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        SearchButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                String searchBoxInput = SearchInputText.getText().toString();

                Query searchPeopleandFriendsQuery = userRef.orderByChild("firstname").startAt(searchBoxInput).endAt(searchBoxInput+"\uf8ff");



                FirebaseRecyclerOptions options =
                        new FirebaseRecyclerOptions.Builder<FindFriends>()
                                .setQuery(userRef, FindFriends.class)
                                .build();
                FirebaseRecyclerAdapter<FindFriends,FindPeopleView> adapter = new FirebaseRecyclerAdapter<FindFriends, FindPeople.FindPeopleView>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FindPeople.FindPeopleView holder, int position, @NonNull FindFriends model) {
                        final String userIDs = getRef(position).getKey();
                        userRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(userIDs.equals(currentuserID)){holder.ll.setVisibility(View.INVISIBLE);}
                                else{
                                    String fn = dataSnapshot.child("firstname").getValue().toString();
                                    String ln = dataSnapshot.child("lastname").getValue().toString();

                                    String image = dataSnapshot.child("profileimage").getValue().toString();

                                    Picasso.get().load(image).into(holder.userimage);
                                    holder.firstname.setText(fn+" "+ln);
                                    holder.RequestUser.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            RequestRef.child(currentuserID).child(userIDs).child("request_type").
                                                    setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        ReceivedRef.child(userIDs).child(currentuserID).child("request_type").
                                                                setValue("recieved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(getActivity(),"Friend Request Sent Successfully",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });}

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                RequestRef.child(userIDs).addValueEventListener(new ValueEventListener() {
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
                    public FindPeopleView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout,viewGroup,false);
                        FindPeopleView viewHolder = new FindPeopleView(view);
                        return viewHolder;


                    }
                };
                postList.setAdapter(adapter);
                adapter.startListening();
            }
        });
    }



    public static class FindPeopleView extends RecyclerView.ViewHolder{

        TextView firstname;
        ImageView RequestUser;
        CircleImageView userimage;
        LinearLayout ll;

        public FindPeopleView(@NonNull View itemView) {
            super(itemView);
            firstname = itemView.findViewById(R.id.all_users_profile_full_name);
            ll = itemView.findViewById(R.id.LL);
            RequestUser = itemView.findViewById(R.id.requestuser);
            userimage = itemView.findViewById(R.id.all_users_profile_image);


        }
    }

}
