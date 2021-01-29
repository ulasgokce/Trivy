package com.ulasgokce.myapplication.Home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ulasgokce.myapplication.Models.Chat;
import com.ulasgokce.myapplication.Models.User;
import com.ulasgokce.myapplication.Models.UserAccountSettings;
import com.ulasgokce.myapplication.Models.Client;
import com.ulasgokce.myapplication.Models.Data;
import com.ulasgokce.myapplication.Models.MyResponse;
import com.ulasgokce.myapplication.Models.Sender;
import com.ulasgokce.myapplication.Models.Token;
import com.ulasgokce.myapplication.R;
import com.ulasgokce.myapplication.Utils.APIService;
import com.ulasgokce.myapplication.Utils.ViewChatAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private Intent intent;
    private User mSendingUser, myUser;
    private CircleImageView mProfileImage;
    private String mProfileImageUrl;
    private TextView username;
    private ImageView backArrow, btnSend;
    private EditText txtSend;
    private ViewChatAdapter mAdapter;
    private List<Chat> mChat;
    private RecyclerView recyclerView;
    private APIService apiService;
    private boolean notify = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        backArrow = findViewById(R.id.backArrow);
        mProfileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btnSend = findViewById(R.id.ivSendMessage);
        txtSend = findViewById(R.id.txtMessage);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        intent = getIntent();
        mSendingUser = intent.getParcelableExtra(getString(R.string.intent_user));
        setUserProfilePhoto();
        username.setText(mSendingUser.getUsername());


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = txtSend.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(), mSendingUser.getUser_id(), msg);
                } else {
                    Toast.makeText(ChatActivity.this, "You can't send an empty message", Toast.LENGTH_SHORT).show();
                }
                txtSend.setText("");
            }
        });

        readMessages(FirebaseAuth.getInstance().getCurrentUser().getUid(), mSendingUser.getUser_id());


    }

    private void setUserProfilePhoto() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(getString(R.string.dbname_user_account_settings)).orderByChild(getString(R.string.field_user_id)).equalTo(mSendingUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user " +
                            ds.getValue(UserAccountSettings.class).toString());
                    mProfileImageUrl = ds.getValue(UserAccountSettings.class).getProfile_photo();
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(ds.getValue(UserAccountSettings.class).getProfile_photo(), mProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(getString(R.string.field_sender), sender);
        hashMap.put(getString(R.string.field_receiver), receiver);
        hashMap.put(getString(R.string.field_message), message);

        reference.child(getString(R.string.dbname_chats)).push().setValue(hashMap);

        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.dbname_users)).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myUser = snapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, myUser.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String receiver, String username, String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(getString(R.string.dbname_tokens));
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(FirebaseAuth.getInstance().getCurrentUser().getUid(), R.mipmap.ic_launcher, username + ": "
                            + msg, "New Message", mSendingUser.getUser_id());
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                } else if (response.body().success == 1) {
                                    Log.d(TAG, "onResponse: Sended a notification");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {
                            Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages(String myID, String userID) {
        mChat = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_chats));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                try {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Chat chat = ds.getValue(Chat.class);
                        if (chat.getReceiver().equals(myID) && chat.getSender().equals(userID) ||
                                chat.getReceiver().equals(userID) && chat.getSender().equals(myID)) {
                            mChat.add(chat);
                        }
                        mAdapter = new ViewChatAdapter(ChatActivity.this, mChat, mProfileImageUrl);
                        recyclerView.setAdapter(mAdapter);

                    }
                } catch (NullPointerException e) {
                    Log.e(TAG, "onDataChange: NullPointerException" + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
