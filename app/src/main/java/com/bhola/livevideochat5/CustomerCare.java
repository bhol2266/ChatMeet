package com.bhola.livevideochat5;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

public class CustomerCare extends AppCompatActivity {

    DatabaseReference chatRef;

    CardView sendbtn;
    EditText textmsg;
    TextView clearMessages;
    String userEmail;
    RecyclerView recyclerview;
    ArrayList<Message_Modelclass> messagesArrayList;
    MessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_care);

        init(CustomerCare.this);

        gotoAdminPanel(CustomerCare.this);
        ImageView backArrow=findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v->{
            onBackPressed();
        });
    }

    private void gotoAdminPanel(Context context) {

        LinearLayout adminPanel = findViewById(R.id.adminPanel);
        adminPanel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EditText passwordEdittext;
                Button passwordLoginBtn;


                AlertDialog dialog;

                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());
                LayoutInflater inflater = LayoutInflater.from(context);
                View promptView = inflater.inflate(R.layout.admin_panel_entry, null);
                builder.setView(promptView);
                builder.setCancelable(true);


                passwordEdittext = promptView.findViewById(R.id.passwordEdittext);
                passwordLoginBtn = promptView.findViewById(R.id.passwordLoginBtn);

                passwordLoginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (passwordEdittext.getText().toString().equals("5555")) {
                            startActivity(new Intent(context, admin_panel.class));

                        } else {
                            Toast.makeText(v.getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                dialog = builder.create();
                dialog.show();
                return false;
            }
        });
    }

    private void init(Context context) {

        Date date = new Date();

        messagesArrayList = new ArrayList<Message_Modelclass>();
        clearMessages = findViewById(R.id.clearMessages);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (messagesArrayList.size() == 0) {
                    Message_Modelclass messages1 = new Message_Modelclass("preset", date.getTime(), 3); //viewType 1 is sender 2 is receiver
                    messagesArrayList.add(messages1);
                    messageAdapter.notifyDataSetChanged();
                }
            }
        }, 500);


        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        recyclerview = findViewById(R.id.recylerview);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        recyclerview.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(context, messagesArrayList, recyclerview, chatRef);
        recyclerview.setAdapter(messageAdapter);

        if (MyApplication.userLoggedIAs.equals("Google")) {
            userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            chatRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userEmail.replace(".", "_")).child("customer_care");

            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Message_Modelclass messages = dataSnapshot.getValue(Message_Modelclass.class);
                        messagesArrayList.add(messages);
                    }

                    messageAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            getArraylist_Sharedpreference(context);
        }


        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = new Date();

                String message = textmsg.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(context, "Enter The Message First", Toast.LENGTH_SHORT).show();
                    return;
                }

                textmsg.setText("");
                Message_Modelclass messages = new Message_Modelclass(message, date.getTime(), 1); //viewType 1 is sender 2 is receiver
                messagesArrayList.add(messages);

                if (MyApplication.userLoggedIAs.equals("Google")) {
                    chatRef.push().setValue(messages, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                // Value was successfully written to the database
                                // Add your success logic here
                                Log.d(MyApplication.TAG, "Data saved successfully.: ");
                            } else {
                                // Failed to write value to the database
                                // Add your failure logic here
                                Log.d(MyApplication.TAG, "Data saved Failed.: " + databaseError.getMessage());

                            }
                        }
                    });
                } else {
                    sharedPreferences(context);
                }

                String[] presetsMsg = {"How to be a member?", "Cancel subscription", "Why am I banned", "Report & Complaints"};


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Message_Modelclass msg = new Message_Modelclass("preset", date.getTime(), 3); //viewType 1 is sender 2 is receiver
                        messagesArrayList.add(msg);
                        recyclerview.scrollToPosition(messagesArrayList.size() - 1);

                        if (MyApplication.userLoggedIAs.equals("Google")) {
                            chatRef.push().setValue(msg, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        // Value was successfully written to the database
                                        // Add your success logic here
                                        Log.d(MyApplication.TAG, "Data saved successfully.: ");
                                    } else {
                                        // Failed to write value to the database
                                        // Add your failure logic here
                                        Log.d(MyApplication.TAG, "Data saved Failed.: " + databaseError.getMessage());

                                    }
                                }
                            });
                        } else {
                            sharedPreferences(context);
                        }
                    }
                }, 1000);

                textmsg.setText("");
                recyclerview.scrollToPosition(messagesArrayList.size() - 1);


                if (messagesArrayList.size() > 4) {
                    clearMessaage(context);

                }
            }
        });

    }

    private void clearMessaage(Context context) {
        Date date = new Date();

        clearMessages.setVisibility(View.VISIBLE);
        clearMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messagesArrayList.clear();
                Message_Modelclass msg = new Message_Modelclass("preset", date.getTime(), 3); //viewType 1 is sender 2 is receiver
                messagesArrayList.add(msg);
                recyclerview.scrollToPosition(messagesArrayList.size() - 1);
                if (MyApplication.userLoggedIAs.equals("Google")) {

                    chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                // Remove each child node using the key
                                childSnapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    sharedPreferences(context);
                }
                Toast.makeText(context, "Cleared", Toast.LENGTH_SHORT).show();
                clearMessages.setVisibility(View.GONE);

            }
        });
    }


    private void sharedPreferences(Context context) {

        Gson gson = new Gson();
        String json = gson.toJson(messagesArrayList);

        SharedPreferences preferences = context.getSharedPreferences("customer_care_chats", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("messagesArrayList", json);
        editor.apply();


    }

    private void getArraylist_Sharedpreference(Context context) {

        SharedPreferences preferences = context.getSharedPreferences("customer_care_chats", Context.MODE_PRIVATE);
        String json = "";
        json = preferences.getString("messagesArrayList", "");
        Type type = new TypeToken<ArrayList<Message_Modelclass>>() {
        }.getType();
        Gson gson = new Gson();
        ArrayList<Message_Modelclass> myObjectList = gson.fromJson(json, type);


        if (json.length() > 20) {
            messagesArrayList.clear();
            messagesArrayList.addAll(myObjectList);
        }

    }
}