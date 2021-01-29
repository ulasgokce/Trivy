package com.ulasgokce.myapplication.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ulasgokce.myapplication.Models.Notification;
import com.ulasgokce.myapplication.Models.User;
import com.ulasgokce.myapplication.R;

import java.util.List;

public class ViewNotificationsAdapter extends ArrayAdapter<Notification> {
    private static final String TAG = "ViewNotificationsAdapte";

    private LayoutInflater inflater;
    private Context context;
    private List<Notification> dataList;
    private int mResource;
    private String message;

    private String senderUsername;

    public ViewNotificationsAdapter(@NonNull Context context, int resource, @NonNull List<Notification> objects) {
        super(context, resource, objects);
        this.context = context;
        this.dataList = objects;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(mResource, parent, false);
        TextView textView = convertView.findViewById(R.id.notification_message);

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(context.getString(R.string.dbname_users))
                .orderByChild(context.getString(R.string.field_user_id))
                .equalTo(getItem(position).getSender());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    String username= "";

                    username = singleSnapshot.getValue(User.class).getUsername();
                    if (getItem(position).getEvent().equals("Commented")) {
                        Log.d(TAG, "onDataChange: "+ senderUsername + " Commented Your Post.");
                        message = username + " Commented Your Post.";
                        textView.setText(message);
                    }
                    if (getItem(position).getEvent().equals("Liked")) {
                        Log.d(TAG, "onDataChange: "+ senderUsername + " Liked Your Post.");
                        message = username + " Liked Your Post.";
                        textView.setText(message);
                    }
                    if (getItem(position).getEvent().equals("Followed")) {
                        Log.d(TAG, "onDataChange: "+ " is following you.");
                        message = "Now " + username + " is following you.";
                        textView.setText(message);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return convertView;

    }
}
