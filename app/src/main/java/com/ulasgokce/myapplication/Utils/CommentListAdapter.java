package com.ulasgokce.myapplication.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ulasgokce.myapplication.Models.Comment;
import com.ulasgokce.myapplication.Models.UserAccountSettings;
import com.ulasgokce.myapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private static final String TAG = "CommentListAdapter";
    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;


    public CommentListAdapter(@NonNull Context context, int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    private static class ViewHolder {
        TextView comment, username, timestamp, reply, likes;
        CircleImageView profileImage;
        ImageView like;

    }

    //ViewHolder Build Pattern
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.comment = convertView.findViewById(R.id.comment);
            holder.username = convertView.findViewById(R.id.comment_username);
            holder.timestamp = convertView.findViewById(R.id.comment_time_posted);
            holder.reply = convertView.findViewById(R.id.comment_reply);
            holder.likes = convertView.findViewById(R.id.comment_likes);
            holder.profileImage = convertView.findViewById(R.id.comment_profile_image);
            holder.like = convertView.findViewById(R.id.comment_like);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.comment.setText(getItem(position).getComment());
        String timestampDifference = getTimeStampDifference(getItem(position));
        if (!timestampDifference.equals("0")) {
            holder.timestamp.setText(timestampDifference + "d");
        } else {
            holder.timestamp.setText("today");
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),holder.profileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (position==0){
            holder.like.setVisibility(View.GONE);
            holder.likes.setVisibility(View.GONE);
            holder.reply.setVisibility(View.GONE);
        }
        return convertView;
    }


    private String getTimeStampDifference(Comment comment) {
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Turkey"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDate_created();
        try {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
        } catch (ParseException e) {
            Log.e(TAG, "getTimeStampDifference: parse exeption " + e.getMessage());
            difference = "0";
        }
        return difference;
    }

}
