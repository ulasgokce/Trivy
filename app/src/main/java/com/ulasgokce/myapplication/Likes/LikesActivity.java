package com.ulasgokce.myapplication.Likes;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ulasgokce.myapplication.Models.Notification;
import com.ulasgokce.myapplication.R;
import com.ulasgokce.myapplication.Utils.BottomNavigationViewHelper;
import com.ulasgokce.myapplication.Utils.ViewNotificationsAdapter;

import java.util.ArrayList;
import java.util.List;

public class LikesActivity extends AppCompatActivity {
    private static final String TAG = "LikesActivity";
    private Context mContext = LikesActivity.this;
    private static final int ACTIVITY_NUM = 3;
    private FirebaseAuth mAuth;
    private ListView listView;
    private List<Notification> myNotifications;
    private ViewNotificationsAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        myNotifications=new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        listView=findViewById(R.id.listView);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child(getString(R.string.dbname_notification)).orderByChild(getString(R.string.field_receiver)).equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    myNotifications.add(notification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mAdapter =new ViewNotificationsAdapter(this,R.layout.list_item_notification,myNotifications);
        listView.setAdapter(mAdapter);
        setupBottomNavigationView();



    }

    private void setupBottomNavigationView() {
        BottomNavigationView view = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.enableNavigation(mContext, this, view);
        Menu menu = view.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}