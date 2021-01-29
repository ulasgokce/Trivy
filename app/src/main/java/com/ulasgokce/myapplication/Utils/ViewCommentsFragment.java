package com.ulasgokce.myapplication.Utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ulasgokce.myapplication.Home.HomeActivity;
import com.ulasgokce.myapplication.Models.Comment;
import com.ulasgokce.myapplication.Models.Notification;
import com.ulasgokce.myapplication.Models.Photo;
import com.ulasgokce.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ViewCommentsFragment extends Fragment {
    private static final String TAG = "ViewCommentsFragment";

    public ViewCommentsFragment() {
        super();
        setArguments(new Bundle());
    }

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;

    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    private ListView mListView;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        mBackArrow = view.findViewById(R.id.backArrow);
        mCheckMark = view.findViewById(R.id.ivPostComment);
        mComment = view.findViewById(R.id.comment);
        mListView = view.findViewById(R.id.listView);
        mComments = new ArrayList<>();
        mContext = getActivity();

        try {

            mPhoto = getPhotoFromBundle();
            setupFirebaseAuth();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException " + e.getMessage());
        }


        return view;
    }

    private void setupWidgets() {

        CommentListAdapter adapter = new CommentListAdapter(mContext, R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mComment.getText().toString().equals("")) {
                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyBoard();
                } else {
                    Toast.makeText(getActivity(), "You cant post a blank Comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                    getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void closeKeyBoard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment) {
        Log.d(TAG, "addNewComment: adding  new comment " + newComment);
        String commentID = myRef.push().getKey();
        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimeStamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Notification notification = new Notification();
        notification.setSender(FirebaseAuth.getInstance().getCurrentUser().getUid());
        notification.setReceiver(mPhoto.getUser_id());
        notification.setEvent("Commented");

        myRef.child(getString(R.string.dbname_notification)).push().setValue(notification);

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);


    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Turkey"));
        return sdf.format(new Date());
    }

    private Photo getPhotoFromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }



    /*************************************firebase**********************************/


    private void setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: signed in");
                } else {
                    Log.d(TAG, "onAuthStateChanged: Signed Out");
                }
            }
        };

        if (mPhoto.getComments().size()==0){
            mComments.clear();
            Comment firstComment = new Comment();
            firstComment.setComment(mPhoto.getCaption());
            firstComment.setUser_id(mPhoto.getUser_id());
            firstComment.setDate_created(mPhoto.getDate_created());
            mComments.add(firstComment);
            mPhoto.setComments(mComments);
            setupWidgets();
        }

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Query query = myRef
                        .child(mContext.getString(R.string.dbname_photos))
                        .orderByChild(mContext.getString(R.string.field_photo_id))
                        .equalTo(mPhoto.getPhoto_id());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                            Photo photo = new Photo();
                            Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                            photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                            photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                            photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                            photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                            photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                            photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());


                            mComments.clear();
                            Comment firstComment = new Comment();
                            firstComment.setComment(mPhoto.getCaption());
                            firstComment.setUser_id(mPhoto.getUser_id());
                            firstComment.setDate_created(mPhoto.getDate_created());

                            mComments.add(firstComment);
                            for (DataSnapshot dSnapshot : singleSnapshot.child(mContext.getString(R.string.field_comments)).getChildren()) {
                                Comment comment = new Comment();
                                comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                mComments.add(comment);
                            }
                            photo.setComments(mComments);
                            mPhoto = photo;

                            setupWidgets();
//                    List<Like> likesList = new ArrayList<Like>();
//                    for (DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_likes)).getChildren()) {
//                        Like like = new Like();
//                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//                        likesList.add(like);
//                    }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
