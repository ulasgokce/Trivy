package com.ulasgokce.myapplication.Share;

import android.content.Intent;
import android.graphics.Bitmap;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ulasgokce.myapplication.R;
import com.ulasgokce.myapplication.Utils.FirebaseMethods;
import com.ulasgokce.myapplication.Utils.UniversalImageLoader;

public class NextActivity extends AppCompatActivity {
    private static final String TAG = "NextActivity";
    private String mAppend = "file:/";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private int imageCount = 0;
    private EditText mCaption;
    private String imgUrl;
    private Bitmap bitmap;
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = findViewById(R.id.caption);
        setupFirebaseAuth();
        setImage();

        ImageView backArrow = findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });


        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NextActivity.this, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();
                String caption = mCaption.getText().toString();


                if (intent.hasExtra(getString(R.string.selected_image))) {
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl, null);
                }else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                        bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, null, bitmap);
                    }

                }
            });


        }

        private void setImage () {
            intent = getIntent();
            ImageView image = findViewById(R.id.imageShare);
            if (intent.hasExtra(getString(R.string.selected_image))) {
                imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                Log.d(TAG, "setImage: got new image url: " + imgUrl);
                UniversalImageLoader.setImage(imgUrl, image, null, mAppend);
            } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
                Log.d(TAG, "setImage: got new bitmap");
                image.setImageBitmap(bitmap);
            }
        }


        /*************************************firebase**********************************/


        private void setupFirebaseAuth () {
            mAuth = FirebaseAuth.getInstance();
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            myRef = mFirebaseDatabase.getReference();
            Log.d(TAG, "onDataChange: when initiated image count :" + imageCount);

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

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    imageCount = mFirebaseMethods.getImageCount(snapshot);
                    Log.d(TAG, "onDataChange: real image count :" + imageCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @Override
        public void onStart () {
            super.onStart();
            mAuth.addAuthStateListener(mAuthListener);
        }

        @Override
        public void onStop () {
            super.onStop();
            if (mAuthListener != null) {
                mAuth.removeAuthStateListener(mAuthListener);
            }
        }
    }
