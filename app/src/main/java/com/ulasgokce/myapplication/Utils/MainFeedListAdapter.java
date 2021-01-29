package com.ulasgokce.myapplication.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ulasgokce.myapplication.Home.HomeActivity;
import com.ulasgokce.myapplication.Models.Comment;
import com.ulasgokce.myapplication.Models.Like;
import com.ulasgokce.myapplication.Models.Notification;
import com.ulasgokce.myapplication.Models.Photo;
import com.ulasgokce.myapplication.Models.User;
import com.ulasgokce.myapplication.Models.UserAccountSettings;
import com.ulasgokce.myapplication.Profile.ProfileActivity;
import com.ulasgokce.myapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainFeedListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemsListener {
        void onLoadMoreItems();
    }

    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    private static final String TAG = "MainFeedListAdapter";
    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";

    public MainFeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        mReference = FirebaseDatabase.getInstance().getReference();
        this.mContext = context;
    }

    static class ViewHolder {
        CircleImageView mProfileImage;
        String likesString;
        TextView username, timeDelta, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;
        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.username = convertView.findViewById(R.id.username);
            holder.image = convertView.findViewById(R.id.post_image);
            holder.heartRed = convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = convertView.findViewById(R.id.image_heart_white);
            holder.comment = convertView.findViewById(R.id.speech_bubble);
            holder.likes = convertView.findViewById(R.id.image_likes);
            holder.comments = convertView.findViewById(R.id.image_comments_link);
            holder.caption = convertView.findViewById(R.id.image_caption);
            holder.timeDelta = convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImage = convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        getCurrentUsername();
        getLikesString(holder);
        holder.caption.setText(getItem(position).getCaption());

        List<Comment> comments = getItem(position).getComments();
        holder.comments.setText("View all " + comments.size() + " comments");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity) mContext).onCommentThreadSelected(getItem(position));
            }
        });

        String timestampDifference = getTimeStampDifference(getItem(position));
        if (!timestampDifference.equals("0")) {
            holder.timeDelta.setText(timestampDifference + " DAYS AGO");
        } else {
            holder.timeDelta.setText("TODAY");
        }


        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    //  currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    Log.d(TAG, "onDataChange: found user : " + singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });

                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(), holder.mProfileImage);
                    holder.mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });
                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity) mContext).onCommentThreadSelected(getItem(position));

                            ((HomeActivity) mContext).hideLayout();
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Query userQuery = mReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    holder.user = singleSnapshot.getValue(User.class);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (reachedEndOfList(position)){
            loadMoreData();
        }


        return convertView;
    }

    private boolean reachedEndOfList(int position) {
        return position == getCount() - 1;
    }

    private void loadMoreData() {
        try {
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();
        } catch (ClassCastException e) {
            Log.e(TAG, "loadMoreData: ClassCastException " + e.getMessage());
        }
        try{
            mOnLoadMoreItemsListener.onLoadMoreItems();
        }catch (NullPointerException e) {
            Log.e(TAG, "loadMoreData: NullPointerException " + e.getMessage());

        }
    }

    private void getCurrentUsername() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        ViewHolder mHolder;

        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                        String keyID = singleSnapshot.getKey();
                        if (mHolder.likeByCurrentUser &&
                                singleSnapshot.getValue(Like.class)
                                        .getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        } else if (!mHolder.likeByCurrentUser) {
                            addNewLike(mHolder);
                            break;
                        }

                    }
                    if (!snapshot.exists()) {
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return true;
        }
    }

    private void addNewLike(final ViewHolder holder) {
        Log.d(TAG, "addNewLike: adding new like");
        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        holder.heart.toggleLike();
        getLikesString(holder);


        Notification notification = new Notification();
        notification.setSender(FirebaseAuth.getInstance().getCurrentUser().getUid());
        notification.setReceiver(holder.photo.getUser_id());
        notification.setEvent("Liked");

        mReference.child(mContext.getString(R.string.dbname_notification)).push().setValue(notification);
    }


    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: getting likes string");

        try {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: found like : " +
                                            singleSnapshot.getValue(User.class).getUsername());
                                    holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                    holder.users.append(",");
                                }
                                Log.d(TAG, "onDataChange: liked users " + holder.users.toString());
                                String[] splitUsers = holder.users.toString().split(",");
                                if (holder.users.toString().contains(currentUsername)) {
                                    holder.likeByCurrentUser = true;
                                } else {
                                    holder.likeByCurrentUser = false;
                                }
                                int length = splitUsers.length;
                                if (length == 1) {
                                    holder.likesString = "Liked by " + splitUsers[0];
                                }
                                if (length == 2) {
                                    holder.likesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];

                                }
                                if (length == 3) {
                                    holder.likesString = "Liked by " + splitUsers[0] + " , " + splitUsers[1] + " and " + splitUsers[2];

                                }
                                if (length == 4) {
                                    holder.likesString = "Liked by " + splitUsers[0] + " , " + splitUsers[1] + " , " + splitUsers[2]
                                            + " and " + splitUsers[3];
                                }
                                if (length > 4) {
                                    holder.likesString = "Liked by " + splitUsers[0] + " , " + splitUsers[1] + " , " + splitUsers[2] +
                                            " and " + (splitUsers.length - 3) + " others";
                                }
                                setUpLikesString(holder, holder.likesString);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    if (!dataSnapshot.exists()) {
                        holder.likesString = "";
                        holder.likeByCurrentUser = false;
                        setUpLikesString(holder, holder.likesString);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "getLikesString: NullPointerException" + e.getMessage());
            holder.likesString = "";
            holder.likeByCurrentUser = false;
            setUpLikesString(holder, holder.likesString);

        }
    }

    private void setUpLikesString(final ViewHolder holder, String likesString) {
        if (holder.likeByCurrentUser) {
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        } else {
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.likes.setText(likesString);
    }

    private String getTimeStampDifference(Photo photo) {
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Turkey"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_created();
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
