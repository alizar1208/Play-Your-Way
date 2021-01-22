package com.example.playyourway;

import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChat extends AppCompatActivity {

    private ImageView sendmessage;
    private EditText writemessage,title;
    private RecyclerView userMessagesList;
    private DatabaseReference teamRef,chatRef,userRef;
    private FirebaseAuth mAuth;
    private String teamkey,teamname,messageSenderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        sendmessage = (ImageView)findViewById(R.id.sendmessage);
        writemessage = (EditText)findViewById(R.id.writemessage);
        userMessagesList = (RecyclerView)findViewById(R.id.list);
        title = (EditText)findViewById(R.id.teamname);
        teamkey = getIntent().getExtras().get("teamkey").toString();
        teamname = getIntent().getExtras().get("teamname").toString();
        title.setText(teamname);

        chatRef = FirebaseDatabase.getInstance().getReference().child("TeamChats").child(teamkey);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userMessagesList.setLayoutManager(new LinearLayoutManager(this));

        sendmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = writemessage.getText().toString();
                if(TextUtils.isEmpty(messageText))
                {
                    Toast.makeText(GroupChat.this,"Please type a message first.",Toast.LENGTH_SHORT).show();
                }
                else{

                    DatabaseReference user_message_key = chatRef.push();
                    String message_push_id = user_message_key.getKey();
                    Map messageTextBody = new HashMap();
                    messageTextBody.put("message",messageText);
                    messageTextBody.put("from",messageSenderID);

                    chatRef.child(message_push_id).updateChildren(messageTextBody);
                    writemessage.setText("");

                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Messages>()
                        .setQuery(chatRef, Messages.class)
                        .build();
        FirebaseRecyclerAdapter<Messages,GroupChat.FriendsView> adapter = new FirebaseRecyclerAdapter<Messages, GroupChat.FriendsView>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final GroupChat.FriendsView holder, int position, @NonNull Messages model) {
                final String userIDs = getRef(position).getKey();
                chatRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String a = dataSnapshot.child("from").getValue().toString();
                        final String m = dataSnapshot.child("message").getValue().toString();
                        if(a.equals(messageSenderID)){
                            holder.r.setVisibility(View.INVISIBLE);
                            holder.send.setText(m);
                        }
                        else {
                            userRef.child(a).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    String fn = dataSnapshot.child("firstname").getValue().toString();
                                    String ln = dataSnapshot.child("lastname").getValue().toString();
                                    holder.send.setVisibility(View.INVISIBLE);
                                    holder.id.setText(fn + " " + ln);
                                    holder.message.setText(m);


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                    chatRef.child(userIDs).addValueEventListener(new ValueEventListener() {
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

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }


            @NonNull
            @Override
            public FriendsView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout_of_users,viewGroup,false);
                FriendsView viewHolder = new FriendsView(view);
                return viewHolder;


            }
        };
        userMessagesList.setAdapter(adapter);
        adapter.startListening();



    }

    public static class FriendsView extends RecyclerView.ViewHolder{

        TextView id,message,send;
        LinearLayout r;

        public FriendsView(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.receivername);
            message = itemView.findViewById(R.id.receivedmessage);
            send = itemView.findViewById(R.id.messagesend);
            r = itemView.findViewById(R.id.received);

        }
    }

}
