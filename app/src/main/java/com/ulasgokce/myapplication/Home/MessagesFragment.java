package com.ulasgokce.myapplication.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ulasgokce.myapplication.Models.Token;
import com.ulasgokce.myapplication.Models.User;
import com.ulasgokce.myapplication.R;
import com.ulasgokce.myapplication.Utils.UserListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {
    private static final String TAG = "MessagesFragment";
    private Context mContext;
    private UserListAdapter mAdapter;
    private ListView mListView;
    private ArrayList<String> mFollowing;
    private List<User> mUserList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        mContext = getActivity();
        mListView = view.findViewById(R.id.listView);
        mUserList = new ArrayList<>();
        mFollowing = new ArrayList<>();
        try {
            getFollowing();
            updateToken(FirebaseInstanceId.getInstance().getToken());
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException" + e.getMessage());
        }
        return view;
    }


    private void updateUsersList() {
        Log.d(TAG, "updateUsersList: updating list");
        mAdapter = new UserListAdapter(mContext, R.layout.layout_user_listitem, mUserList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user");
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(getString(R.string.intent_user),
                        mUserList.get(position));
                startActivity(intent);
            }
        });

    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following)).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }
                getUsers();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.dbname_tokens));
        Token token1 = new Token(token);
        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token1);

    }

    private void getUsers() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for (int i = 0; i < mFollowing.size(); i++) {
            Query query = reference
                    .child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        User user = new User();
                        user.setEmail(singleSnapshot.getValue(User.class).getEmail());
                        user.setUsername(singleSnapshot.getValue(User.class).getUsername());
                        user.setPhone_number(singleSnapshot.getValue(User.class).getPhone_number());
                        user.setUser_id(singleSnapshot.getValue(User.class).getUser_id());
                        mUserList.add(user);
                    }
                    updateUsersList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


}
