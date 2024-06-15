package com.bhola.livevideochat5;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bhola.livevideochat5.Models.ChatItem_ModelClass;
import com.bhola.livevideochat5.Models.Model_Profile;
import com.bhola.livevideochat5.Models.UserBotMsg;
import com.bhola.livevideochat5.Models.UserQuestionWithAns;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;


public class Fragment_Messenger extends Fragment {


    RecyclerView recyclerview;
    public static ArrayList<ChatItem_ModelClass> userList;
    public static ArrayList<ChatItem_ModelClass> userListTemp;
    ArrayList<Model_Profile> favourite_slider;
    FavouriteSliderAdapter sliderAdapter;
    LinearLayoutManager layoutManager;
    public static MessengeItemsAdapter adapter;
    public static String currentActiveUser = "";
    EditText searchEdittext;

    private Dialog alertNotificationDialog;
    private static final long AUTO_DISMISS_DELAY = 4000; // 4 seconds
    public static int count = 0;


    public Fragment_Messenger() {
        // Required empty public constructor
    }

    View view;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_messenger, container, false);

        context = getContext();
        // Inflate the layout for this fragment


        setRecyclerView();
        setup_CustomerCare_Chat();
        setUpSlider();
        searchFunction();
        return view;


    }

    private void searchFunction() {
        searchEdittext = view.findViewById(R.id.searchEdittext);
        searchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString().toLowerCase());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void setup_CustomerCare_Chat() {

        TextView userName = view.findViewById(R.id.userName);
        TextView lastMessage = view.findViewById(R.id.lastMessage);
        TextView messageTime = view.findViewById(R.id.messageTime);
        TextView messageCount = view.findViewById(R.id.messageCount);
        LinearLayout chatItemClick = view.findViewById(R.id.chatItemClick);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        String formattedTime = dateFormat.format(currentTime);
        messageTime.setText(formattedTime);


        chatItemClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(context, CustomerCare.class));
            }
        });


    }


    private void setRecyclerView() {
        recyclerview = view.findViewById(R.id.recyclerview);

        loadChatBot_firebase();


    }

    private void loadChatBot_firebase() {

        //this is to check if chatbots from firebase already saved in SharedPrefrence, so that we dont have to load from firebase again and again
        boolean userlist_available = save_retreive_Chatbots_insharedPrefrence("retreive");
        if (userlist_available) {
            sendDataToRecyclerview();
            return;
        }

        String databaseRef = "";
        if (MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Google") && MyApplication.App_updating.equals("inactive")) {
            databaseRef = "Chatbots_UnCensored/chatbots_uncensored";
        } else {
            databaseRef = "Chatbots_Censored/chatbots_censored";
        }
        userList = new ArrayList<>();
        String finalDatabaseRef = databaseRef;

        new Thread(new Runnable() {
            @Override
            public void run() {

                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference(finalDatabaseRef);
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                            String name = (String) userSnapshot.child("name").getValue();
                            String username = (String) userSnapshot.child("username").getValue();

                            ArrayList<String> contentImages = new ArrayList<>();
                            for (DataSnapshot document : userSnapshot.child("images").getChildren()) {
                                contentImages.add(document.getValue(String.class));
                            }

                            String profileImage = (String) userSnapshot.child("profileImage").getValue();
                            profileImage = MyApplication.databaseURL_images + profileImage;
                            Boolean booleanValue = userSnapshot.child("containsQuestion").getValue(Boolean.class);
                            boolean containsQuestion = booleanValue.booleanValue();

                            String recommendationType = (String) userSnapshot.child("recommendationType").getValue();


                            ArrayList<UserBotMsg> userBotMsgList = new ArrayList<>();
                            if (!containsQuestion) {
                                for (DataSnapshot userBotmsq_Snapshot : userSnapshot.child("userBotMsg").getChildren()) {
                                    UserBotMsg userBotMsg = userBotmsq_Snapshot.getValue(UserBotMsg.class);
                                    userBotMsgList.add(userBotMsg);
                                }
                            }

                            UserQuestionWithAns questionWithAns = null;
                            if (containsQuestion) {

                                String action = (String) userSnapshot.child("questionWithAns").child("action").getValue();
                                String dateTime = (String) userSnapshot.child("questionWithAns").child("dateTime").getValue();
                                String question = (String) userSnapshot.child("questionWithAns").child("question").getValue();
                                String reply = (String) userSnapshot.child("questionWithAns").child("reply").getValue();


                                Long longValue1 = (Long) userSnapshot.child("questionWithAns").child("read").getValue();
                                int read = longValue1.intValue();
                                Long longValue3 = (Long) userSnapshot.child("questionWithAns").child("sent").getValue();
                                int sent = longValue3.intValue();

                                ArrayList<String> answersList = new ArrayList<>();


                                for (DataSnapshot snapshot : userSnapshot.child("questionWithAns").child("answers").getChildren()) {
                                    String value = snapshot.getValue(String.class);
                                    if (value != null) {
                                        answersList.add(value);
                                    }
                                }


                                ArrayList<UserBotMsg> replyToUserList = new ArrayList<>();
                                for (DataSnapshot userBotmsq_Snapshot : userSnapshot.child("questionWithAns").child("replyToUser").getChildren()) {
                                    UserBotMsg userBotMsg = userBotmsq_Snapshot.getValue(UserBotMsg.class);
                                    replyToUserList.add(userBotMsg);
                                }

                                questionWithAns = new UserQuestionWithAns(question, answersList, dateTime, action, read, sent, reply, replyToUserList);
                            }

                            for (int i = 0; i < userBotMsgList.size(); i++) {
                                UserBotMsg userBotMsg = userBotMsgList.get(i);
                                if (userBotMsg.getMimeType().equals("mimeType/image")) {
                                    Log.d("ASDfasd", "username: " + username);
                                    userBotMsgList.get(i).setExtraMsg(MyApplication.databaseURL_images + contentImages.get(Integer.parseInt(userBotMsg.getExtraMsg())));
                                }
                            }


                            if (containsQuestion) {
                                for (int i = 0; i < questionWithAns.getReplyToUser().size(); i++) {
                                    UserBotMsg userBotMsg = questionWithAns.getReplyToUser().get(i);
                                    if (userBotMsg.getMimeType().equals("mimeType/image")) {
                                        questionWithAns.getReplyToUser().get(i).setExtraMsg(MyApplication.databaseURL_images + contentImages.get(Integer.parseInt(userBotMsg.getExtraMsg())));
                                    }
                                }
                            }


                            ChatItem_ModelClass user = new ChatItem_ModelClass(name, username, profileImage, contentImages, containsQuestion, recommendationType, userBotMsgList, questionWithAns);

                            userList.add(user);

                        }

                        Collections.shuffle(userList);
                        userList.subList(0, Math.min(userList.size(), 10));

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                save_retreive_Chatbots_insharedPrefrence("save");
                                sendDataToRecyclerview();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(MyApplication.TAG, " userList.size(): " + databaseError.getMessage());
                    }
                });

            }
        }).start();

    }

    private boolean save_retreive_Chatbots_insharedPrefrence(String saveORretreive) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("messenger_chats", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (saveORretreive.equals("save")) {
            Gson gson = new Gson();
            String json = gson.toJson(userList);
            editor.putString("userList_Firebase_Chatbots", json);
            editor.apply();
            return true;
        } else {
            // Retrieve the JSON string from SharedPreferences
            String json = sharedPreferences.getString("userList_Firebase_Chatbots", null);
            // Convert the JSON string back to ArrayList
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<ChatItem_ModelClass>>() {
            }.getType();


            if (json == null) {
                // Handle case when no ArrayList is saved in SharedPreferences
                return false;
            } else {
                userList = gson.fromJson(json, type);
                return true;
            }
        }
    }

    private void sendDataToRecyclerview() {

        userListTemp = new ArrayList<>();
        layoutManager = new LinearLayoutManager(context);
        recyclerview.setLayoutManager(new WrapContentLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        adapter = new MessengeItemsAdapter(userListTemp, context, adapter, recyclerview);

        recyclerview.setAdapter(adapter);

        if (retreive_sharedPreferences(context)) {

            adapter = new MessengeItemsAdapter(userListTemp, context, adapter, recyclerview);
            recyclerview.setAdapter(adapter);

            int temp = 0;
            for (int i = userListTemp.size(); i < userList.size(); i++) {
                int delayTime = temp * 180000;
                temp++;
                int finalI = i;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        userListTemp.add(0, userList.get(finalI));
                        adapter.notifyItemInserted(0);

                        String activityName = MessengeItemsAdapter.getCurrentlyRunningActivity(context);
                        if (activityName != null && activityName.equals("com.bhola.livevideochat5.MainActivity")) {
                            playSentAudio();
                        }
                    }
                }, delayTime);
            }

            return;
        }

        for (int i = 0; i < 4; i++) {

            int finalI = i;
            int delayTime = finalI * 20000;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    userListTemp.add(0, userList.get(finalI));
                    adapter.notifyItemInserted(0);

                    String activityName = MessengeItemsAdapter.getCurrentlyRunningActivity(context);
                    if (activityName != null && activityName.equals("com.bhola.livevideochat5.MainActivity")) {
                        playSentAudio();
                    }

                }
            }, delayTime);
        }


    }

    public static void playSentAudio() {

        boolean foregroud = false;
        try {
            foregroud = new ForegroundCheckTask().execute(adapter.context).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!foregroud) {
            return;
        }
        MediaPlayer mediaPlayer = MediaPlayer.create(adapter.context, R.raw.message_received);
        mediaPlayer.start();
    }


    public static void save_sharedPrefrence(Context context, ArrayList<ChatItem_ModelClass> userListt) {

        //this code is just for removing duplicate chatbots if available and updating adapter
        HashSet<String> seenUsernames = new HashSet<>();
        ArrayList<ChatItem_ModelClass> uniqueList = new ArrayList<>();

        for (ChatItem_ModelClass item : userListt) {
            String username = item.getUsername(); // Replace getUsername() with your actual method to get the username.

            // Check if the username has been seen before.
            if (!seenUsernames.contains(username)) {
                seenUsernames.add(username); // Add the username to the set to mark it as seen.
                uniqueList.add(item); // Add the item to the unique list.
            }
        }
        Fragment_Messenger.adapter.userList.clear();
        Fragment_Messenger.adapter.userList.addAll(uniqueList);


        SharedPreferences sharedPreferences = context.getSharedPreferences("messenger_chats", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


// Convert the ArrayList to JSON string
        Gson gson = new Gson();
        String json = gson.toJson(userListt);

// Save the JSON string to SharedPreferences
        if (MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Google")) {
            editor.putString("userListTemp_Google", json);
        } else {
            editor.putString("userListTemp_Guest", json);
        }
        editor.apply();

    }

    public static boolean retreive_sharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("messenger_chats", MODE_PRIVATE);

// Retrieve the JSON string from SharedPreferences
        String json = "";
        if (MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Google")) {
            json = sharedPreferences.getString("userListTemp_Google", null);
        } else {
            json = sharedPreferences.getString("userListTemp_Guest", null);
        }

// Convert the JSON string back to ArrayList
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<ChatItem_ModelClass>>() {
        }.getType();


        if (json == null) {
            // Handle case when no ArrayList is saved in SharedPreferences
            return false;
        } else {
            userListTemp = gson.fromJson(json, type);
            Log.d("ASdfsdfa", "retreive_sharedPreferences: " + userListTemp.size());
            return true;
        }


    }

    public static void updateUnreadmessageCount(Context context) {

        if (userListTemp == null) {
            return;
        }
        count = 0;

        for (int i = 0; i < Fragment_Messenger.userListTemp.size(); i++) {

            ChatItem_ModelClass modelclass = Fragment_Messenger.userListTemp.get(i);

            for (int j = 0; j < modelclass.getUserBotMsg().size(); j++) {
                UserBotMsg userBotMsg = modelclass.getUserBotMsg().get(j);
                if (userBotMsg.getSent() == 1 && userBotMsg.getRead() == 0) {
                    count = count + 1;
                }
            }
            if (modelclass.isContainsQuestion()) {
                if (modelclass.getQuestionWithAns().getSent() == 1 && modelclass.getQuestionWithAns().getRead() == 0) {
                    count = count + 1;
                }
            }
        }

        try {
            //crashlytics crash
            MainActivity.unreadMessage_count = count;
            MainActivity.badge_text.setText(String.valueOf(count));
            MainActivity.badge_text.setVisibility(View.VISIBLE);
            MainActivity.badge_text.setBackgroundResource(R.drawable.badge_background);

            if (count == 0) {
                MainActivity.badge_text.setVisibility(View.GONE);
            }
        } catch (Exception e) {
        }
    }

    public void setUpSlider() {


        favourite_slider = new ArrayList<>();
        loadDatabase_slider();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        CustomRecyclerView recyclerView_slider = view.findViewById(R.id.recyclerView_slider);

        recyclerView_slider.setLayoutManager(layoutManager);
        sliderAdapter = new FavouriteSliderAdapter(context, favourite_slider);
        recyclerView_slider.setAdapter(sliderAdapter);
        sliderAdapter.notifyDataSetChanged();
    }

    private void loadDatabase_slider() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readFavoriteGirls();
                if (cursor.moveToFirst()) {
                    do {
                        favourite_slider.add(Utils.readCursor(cursor));
                    } while (cursor.moveToNext());

                }
                cursor.close();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (favourite_slider.size() != 0) {
                            sliderAdapter.notifyDataSetChanged();
                        } else {
                            LinearLayout favouriteSlideLayout = view.findViewById(R.id.favouriteSlideLayout);
                            favouriteSlideLayout.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }).start();

    }


    @Override
    public void onResume() {
        super.onResume();
        updateUnreadmessageCount(context);


    }

}


class MessengeItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    Context context;

    public static ArrayList<ChatItem_ModelClass> userList;
    ArrayList<ChatItem_ModelClass> backup_userList; //for filtering
    MessengeItemsAdapter adapter;
    RecyclerView recyclerview;
    boolean favourite = false;

    private HashMap<String, Handler> itemHandlers = new HashMap<>();


    public MessengeItemsAdapter(ArrayList<ChatItem_ModelClass> userList, Context context, MessengeItemsAdapter adapter, RecyclerView recyclerview) {
        this.userList = userList;
        this.context = context;
        this.adapter = adapter;
        this.recyclerview = recyclerview;
        backup_userList=new ArrayList<>(userList);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View UserItem_Viewholder = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_items_recyclerview, parent, false);
        return new MessengeItemsAdapter.UserItem_Viewholder(UserItem_Viewholder);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {


        MessengeItemsAdapter.UserItem_Viewholder userItem_viewholder = (MessengeItemsAdapter.UserItem_Viewholder) holder;
        ChatItem_ModelClass modelClass = userList.get(position);

        userItem_viewholder.userName.setText(modelClass.getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        Date currentTime = new Date();
        String formattedTime = dateFormat.format(currentTime);

        Picasso.get().load(modelClass.getProfileImage().replace("profile","profile_original")).fit().into(userItem_viewholder.profileUrl);


        userItem_viewholder.chatItemClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        userItem_viewholder.messageCount.setVisibility(View.GONE);
                    }
                }, 500);

                SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userName", modelClass.getUsername());
                editor.apply(); // Apply the changes to SharedPreferences

                Intent intent = new Intent(context, ChatScreen_User.class);
                intent.putExtra("online", true);
                context.startActivity(intent);
            }
        });

        if (modelClass.isContainsQuestion()) {

            isContainQuestion(modelClass, userItem_viewholder, holder, currentTime);  //method to handler it chat is question

        } else {
            boolean messageFound = false; // the below forloop only works if the  UserBotMsg()
            for (int i = 0; i < modelClass.getUserBotMsg().size() - 1; i++) {
                if (modelClass.getUserBotMsg().get(i).getSent() == 0) {
                    messageFound = true;
                    userItem_viewholder.lastMessage.setText(modelClass.getUserBotMsg().get(i).getMsg());
                    modelClass.getUserBotMsg().get(i).setSent(1);
                    modelClass.getUserBotMsg().get(i).setDateTime(String.valueOf(currentTime.getTime()));
                    setMessageCount(modelClass.getUserBotMsg(), userItem_viewholder.messageCount, modelClass.getUsername());//set messageCount textview in each chatbots items


                    Random random = new Random();
                    int randomNumber = random.nextInt(1001) + 50; // Generate a random number between 0 and 5000, then add 50
                    int nextMegDelay = modelClass.getUserBotMsg().get(i).getNextMsgDelay() + randomNumber;

                    Handler handler = checkPreviousHandler(modelClass); // this method cancels any privious handlers which is pending


                    int finalI = i;
                    Runnable updateTimeRunnable = new Runnable() {
                        @Override
                        public void run() {

                            int time = 0;
                            if (holder.getAbsoluteAdapterPosition() == -1) {
                                time = 200000;
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int currentPostion = -1;
                                    for (int j = 0; j < userList.size(); j++) {
                                        if (userList.get(j).getUsername().equals(modelClass.getUsername())) {
                                            currentPostion = j;
                                        }
                                    }

                                    try {
                                        userList.remove(currentPostion);
                                        userList.add(0, modelClass);
                                        notifyItemMoved(currentPostion, 0);

                                    } catch (Exception e) {
                                    }
                                    notifyItemChanged(0);

                                    recyclerview.smoothScrollToPosition(0);
                                    Fragment_Messenger.updateUnreadmessageCount(context);
                                    Fragment_Messenger.save_sharedPrefrence(context, userList);


                                    String activityName = MessengeItemsAdapter.getCurrentlyRunningActivity(context);
                                    if (activityName != null && activityName.equals("com.bhola.livevideochat5.MainActivity")) {
                                        Fragment_Messenger.playSentAudio();
                                    }
                                    if (activityName != null && activityName.equals("com.bhola.livevideochat5.ChatScreen_User")) {
                                        if (Fragment_Messenger.currentActiveUser.equals(modelClass.getUsername())) {
                                            Fragment_Messenger.playSentAudio();
                                        }
                                    }
                                }
                            }, time);


                        }
                    };
                    handler.postDelayed(updateTimeRunnable, nextMegDelay);

                    break;
                }
                if (i == modelClass.getUserBotMsg().size() - 2) { //last loop
                    messageFound = true;

                    userItem_viewholder.lastMessage.setText(modelClass.getUserBotMsg().get(i + 1).getMsg());
                    modelClass.getUserBotMsg().get(modelClass.getUserBotMsg().size() - 1).setSent(1);
                    modelClass.getUserBotMsg().get(modelClass.getUserBotMsg().size() - 1).setDateTime(String.valueOf(currentTime.getTime()));
                    Fragment_Messenger.save_sharedPrefrence(context, userList);
                    Fragment_Messenger.updateUnreadmessageCount(context);
                    setMessageCount(modelClass.getUserBotMsg(), userItem_viewholder.messageCount, modelClass.getUsername());//set messageCount
                }
            }
            if (!messageFound) {
                userItem_viewholder.lastMessage.setText(modelClass.getUserBotMsg().get(0).getMsg());
                setMessageCount(modelClass.getUserBotMsg(), userItem_viewholder.messageCount, modelClass.getUsername());//set messageCount

            }
        }

        userItem_viewholder.messageCount.setOnClickListener(v -> {
            // this is because the size of the favourite btn is so small
            userItem_viewholder.favouriteImage.performClick();
        });
        userItem_viewholder.favouriteImage.setOnClickListener(v -> {
            if (!favourite) {
                String res = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").updateLike(modelClass.getUsername(), 1);
                userItem_viewholder.favouriteImage.setImageDrawable(context.getResources().getDrawable(R.drawable.star));
                favourite = true;
            } else {
                String res = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").updateLike(modelClass.getUsername(), 0);
                userItem_viewholder.favouriteImage.setImageDrawable(context.getResources().getDrawable(R.drawable.favorite_start));
                favourite = false;

            }

        });
        checkFavouriteStatus(modelClass.getUsername(), userItem_viewholder.favouriteImage);
    }

    private void isContainQuestion(ChatItem_ModelClass modelClass, UserItem_Viewholder userItem_viewholder, RecyclerView.ViewHolder holder, Date currentTime) {


        UserQuestionWithAns userQuestionWithAns = modelClass.getQuestionWithAns();


        if (userQuestionWithAns.getReply().length() == 0) {
            // not replied yet so ask question here
            userItem_viewholder.lastMessage.setText(userQuestionWithAns.getQuestion());
            userQuestionWithAns.setSent(1);
            userQuestionWithAns.setDateTime(String.valueOf(currentTime.getTime()));
        }

        if (userQuestionWithAns.getReply().length() != 0) {
            //already replied
            int messageIndex = -1;

            for (int i = 0; i < userQuestionWithAns.getReplyToUser().size(); i++) {
                UserBotMsg userBotMsg = userQuestionWithAns.getReplyToUser().get(i);

                if (userBotMsg.getSent() == 0) {


                    Handler newHandler = checkPreviousHandler(modelClass); // this method cancels any privious handlers which is pending
                    newHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int time = 0;
                            if (holder.getAbsoluteAdapterPosition() == -1) {
                                time = 300000;
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    userBotMsg.setSent(1);
                                    userBotMsg.setDateTime(String.valueOf(currentTime.getTime()));


                                    int currentPostion = -1;
                                    for (int j = 0; j < userList.size(); j++) {
                                        if (userList.get(j).getUsername().equals(modelClass.getUsername())) {
                                            currentPostion = j;
                                        }
                                    }

                                    userList.remove(currentPostion);
                                    userList.add(0, modelClass);
                                    notifyItemMoved(currentPostion, 0);
                                    notifyItemChanged(0);

                                    recyclerview.smoothScrollToPosition(0);
                                    Fragment_Messenger.updateUnreadmessageCount(context);
                                    Fragment_Messenger.playSentAudio();

                                }
                            }, time);


                        }
                    }, userBotMsg.getNextMsgDelay());
                    break;
                } else {
                    messageIndex = i;
                }
            }

            if (messageIndex != -1) {
                userItem_viewholder.lastMessage.setText(userQuestionWithAns.getReplyToUser().get(messageIndex).getMsg());
            } else {
                userItem_viewholder.lastMessage.setText(userQuestionWithAns.getReply());
            }
        }

        setMessageCountQuestion(modelClass.getQuestionWithAns(), userItem_viewholder.messageCount, modelClass.getUsername());//set messageCount method
        Fragment_Messenger.save_sharedPrefrence(context, userList);
        Fragment_Messenger.updateUnreadmessageCount(context);

    }

    private Handler checkPreviousHandler(ChatItem_ModelClass modelClass) {
        Handler previousHandler = itemHandlers.get(modelClass.getUsername());
        if (previousHandler != null) {
            previousHandler.removeCallbacksAndMessages(null);
        }
        Handler newHandler = new Handler();
        itemHandlers.put(modelClass.getUsername(), newHandler);

        return newHandler;
    }

    private void setMessageCount(ArrayList<UserBotMsg> userBotMsg, TextView messageCount, String userName) {

        String activityName = getCurrentlyRunningActivity(context);
        if (Fragment_Messenger.currentActiveUser.equals(userName) && activityName != null && activityName.equals("com.bhola.livevideochat5.ChatScreen_User")) {
            messageCount.setVisibility(View.GONE);
            return; //this is because when we are in the chat screem and pressed back the message count is shwoing which should not show
        }


        messageCount.setVisibility(View.VISIBLE);

        int count = 0;
        for (int i = 0; i < userBotMsg.size(); i++) {
            if (userBotMsg.get(i).getSent() == 1 && userBotMsg.get(i).getRead() == 0) {
                count = count + 1;
            }
        }
        messageCount.setText(String.valueOf(count));
        if (count == 0) {
            messageCount.setVisibility(View.GONE);
        }

    }

    private void setMessageCountQuestion(UserQuestionWithAns userQuestionWithAns, TextView messageCount, String userName) {

        String activityName = getCurrentlyRunningActivity(context);
        if (Fragment_Messenger.currentActiveUser.equals(userName) && activityName != null && activityName.equals("com.bhola.livevideochat5.ChatScreen_User")) {
            messageCount.setVisibility(View.GONE);
            return; //this is because when we are in the chat screem and pressed back the message count is shwoing which should not show
        }

        messageCount.setVisibility(View.VISIBLE);

        int count = 0;
        if (userQuestionWithAns.getSent() == 1 && userQuestionWithAns.getRead() == 0) {
            count = count + 1;
        }

        for (int i = 0; i < userQuestionWithAns.getReplyToUser().size(); i++) {
            UserBotMsg userBotMsg = userQuestionWithAns.getReplyToUser().get(i);
            if (userBotMsg.getSent() == 1 && userBotMsg.getRead() == 0) {
                count = count + 1;
            }
        }
        messageCount.setText(String.valueOf(count));
        if (count == 0) {
            messageCount.setVisibility(View.GONE);
        }

    }


    private void checkFavouriteStatus(String username, ImageView favouriteImage) {

        Cursor cursor = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readSingleGirl(username);
        if (cursor.moveToFirst()) {
            Model_Profile model_profile = Utils.readCursor(cursor);
            if (model_profile.getLike() == 1) {
                favourite = true;
                favouriteImage.setImageDrawable(context.getResources().getDrawable(R.drawable.star)); // Set image drawable properly
                //set imageview favourite
            } else {
                favourite = false;
                favouriteImage.setImageDrawable(context.getResources().getDrawable(R.drawable.favorite_start)); // Set image drawable properly

            }
        }

    }


    public static String getCurrentlyRunningActivity(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getPackageName();

        // Get the list of running tasks
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);

        if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
            ActivityManager.RunningTaskInfo taskInfo = runningTaskInfos.get(0);

            // Check if the top activity in the task matches the package name of your app
            if (taskInfo.topActivity != null && taskInfo.topActivity.getPackageName().equals(packageName)) {
                return taskInfo.topActivity.getClassName();
            }
        }

        return null;
    }


    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        // background thread
        protected FilterResults performFiltering(CharSequence keyword) {
            ArrayList<ChatItem_ModelClass> filtereddata = new ArrayList<>();

            if (keyword.toString().isEmpty())
                filtereddata.addAll(backup_userList);
            else {
                for (ChatItem_ModelClass obj : backup_userList) {
                    if (obj.getUsername().toString().toLowerCase().trim().contains(keyword.toString().toLowerCase()))
                        filtereddata.add(obj);
                }
            }

            FilterResults results = new FilterResults();
            results.values = filtereddata;
            return results;
        }

        @Override  // main UI thread
        protected void publishResults(CharSequence constraint, FilterResults results) {
            userList.clear();
            userList.addAll((ArrayList<ChatItem_ModelClass>) results.values);
            notifyDataSetChanged();
        }
    };


    @Override
    public int getItemCount() {
        return userList.size();
    }


    public static class UserItem_Viewholder extends RecyclerView.ViewHolder {

        TextView userName, lastMessage, messageCount;
        CircleImageView profileUrl;
        ImageView favouriteImage;
        LinearLayout chatItemClick;
        private Handler handler;


        public UserItem_Viewholder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            favouriteImage = itemView.findViewById(R.id.favouriteImage);
            messageCount = itemView.findViewById(R.id.messageCount);
            profileUrl = itemView.findViewById(R.id.profileUrl);
            chatItemClick = itemView.findViewById(R.id.chatItemClick);


        }
    }
}

class FavouriteSliderAdapter extends RecyclerView.Adapter<FavouriteSliderAdapter.viewholder> {

    Context context;
    ArrayList<Model_Profile> girllist;


    public FavouriteSliderAdapter(Context context, ArrayList<Model_Profile> girllist) {
        this.context = context;
        this.girllist = girllist;
    }

    @androidx.annotation.NonNull
    @Override
    public viewholder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.slider_item, parent, false);
        return new viewholder(view);
    }

    @Override
    public void onViewRecycled(viewholder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull viewholder holder, int position) {
        Model_Profile item = girllist.get(position);
        holder.title.setText(item.getName());
        Picasso.get().load(item.getProfilePhoto()).fit().into(holder.thumbnail);

        holder.sliderlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Profile.class);
                intent.putExtra("userName", item.getUsername());
                intent.putExtra("online", true);
                context.startActivity(intent);
            }
        });


        getUnreadMessageCount(holder.messageCount, item.getUsername(), position, item);

    }

    private void getUnreadMessageCount(TextView messageCount, String username, int position, Model_Profile item) {

        int count = 0;
        for (int i = 0; i < Fragment_Messenger.userListTemp.size(); i++) {

            ChatItem_ModelClass modelclass = Fragment_Messenger.userListTemp.get(i);
            if (modelclass.getUsername().equals(username)) {
                for (int j = 0; j < modelclass.getUserBotMsg().size(); j++) {
                    UserBotMsg userBotMsg = modelclass.getUserBotMsg().get(j);
                    if (userBotMsg.getSent() == 1 && userBotMsg.getRead() == 0) {
                        count = count + 1;
                    }
                }
                if (modelclass.isContainsQuestion()) {
                    if (modelclass.getQuestionWithAns().getSent() == 1 && modelclass.getQuestionWithAns().getRead() == 0) {
                        count = count + 1;
                    }
                }
            }
        }

        messageCount.setText(String.valueOf(count));
        if (count == 0) {
            messageCount.setVisibility(View.GONE);
        }


    }


    @Override
    public int getItemCount() {
        return girllist.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        ImageView thumbnail, online_imageView;
        TextView title, messageCount;
        LinearLayout sliderlayout;

        public viewholder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.imageview);
            title = itemView.findViewById(R.id.categorytextview);
            sliderlayout = itemView.findViewById(R.id.sliderlayout);
            online_imageView = itemView.findViewById(R.id.online_imageView);
            online_imageView.setVisibility(View.GONE);
            messageCount = itemView.findViewById(R.id.messageCount);
            messageCount.setVisibility(View.VISIBLE);

        }
    }
}


class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Context... params) {
        final Context context = params[0].getApplicationContext();
        return isAppOnForeground(context);
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}

class WrapContentLinearLayoutManager extends LinearLayoutManager {
    public WrapContentLinearLayoutManager(Context context, int horizontal, boolean b) {
        super(context);
    }

    //... constructor
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.e("TAG", "meet a IOOBE in RecyclerView");
        }
    }
}