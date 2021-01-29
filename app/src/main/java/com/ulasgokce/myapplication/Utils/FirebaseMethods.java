package com.ulasgokce.myapplication.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ulasgokce.myapplication.Home.HomeActivity;
import com.ulasgokce.myapplication.Models.Photo;
import com.ulasgokce.myapplication.Models.User;
import com.ulasgokce.myapplication.Models.UserAccountSettings;
import com.ulasgokce.myapplication.Models.UserSettings;
import com.ulasgokce.myapplication.Profile.AccountSettingsActivity;
import com.ulasgokce.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context mContext;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;
    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;
        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void uploadNewPhoto(String photoType, String caption, int count, String imgUrl, Bitmap bm) {
        Log.d(TAG, "uploadNewPhoto: attemptin to upload new photo");
        FilePaths filePaths = new FilePaths();
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (photoType.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new photo");
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));


            if (bm == null) {
                bm = ImageManager.getBitmap(imgUrl);
            }
            UploadTask uploadTask = null;
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uri.isComplete()) ;
                    Uri url = uri.getResult();
                    Toast.makeText(mContext, "photo upload success ", Toast.LENGTH_SHORT).show();

                    addPhotoToDatabase(caption, url);
                    Intent i = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(i);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onProgress: upload failed: ");
                    Toast.makeText(mContext, "photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "photo upload progress " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: " + progress + " % done");
                }
            });

        } else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new photo");

            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");


            if (bm == null) {
                bm = ImageManager.getBitmap(imgUrl);
            }
            UploadTask uploadTask = null;
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uri.isComplete()) ;
                    Uri url = uri.getResult();
                    Toast.makeText(mContext, "profile photo upload success ", Toast.LENGTH_SHORT).show();

                    setProfilePhoto(url);
                    ((AccountSettingsActivity) mContext).setViewPager(((AccountSettingsActivity) mContext).pagerAdapter.getFragmentNumber(mContext.getString(R.string.edit_profile_fragment)));

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onProgress: upload failed: ");
                    Toast.makeText(mContext, "photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "photo upload progress " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: " + progress + " % done");
                }
            });

        }
    }

    private void setProfilePhoto(Uri firebaseUrl) {
        Log.d(TAG, "setProfilePhoto: setting new profile image :link :" + firebaseUrl.toString());
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(firebaseUrl.toString());
    }

    private void addPhotoToDatabase(String caption, Uri firebaseUrl) {
        Log.d(TAG, "addPhotoToDatabase: adding the photo to database");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(firebaseUrl.toString());
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        myRef.child(mContext.getString(R.string.dbname_user_photos)).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Turkey"));
        return sdf.format(new Date());
    }

    public int getImageCount(DataSnapshot snapshot) {
        int count = 0;
        for (DataSnapshot ds : snapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()) {
            count++;
        }
        return count;
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                    } else {
                        Toast.makeText(mContext, "couldn't send verification email.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void registerNewEmail(final String email, final String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    sendVerificationEmail();
                    userID = mAuth.getCurrentUser().getUid();
                    Log.d(TAG, "signInWithCustomToken:success");
                } else {
                    Toast.makeText(mContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void addNewUser(String email, String username, String description, String website, String profile_photo) {
        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username));
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);
        UserAccountSettings settings = new UserAccountSettings(
                description,
                StringManipulation.condenseUsername(username), 0, 0, 0,
                "", username, website,userID
        );
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID).setValue(settings);
    }


    public UserSettings getUserAccountSettings(DataSnapshot dataSnapshot) {
        User user = new User();
        UserAccountSettings settings = new UserAccountSettings();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            //usersettings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot" + ds);
                try {
                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );
                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );
                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers()
                    );
                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()
                    );
                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: nullpointerexeption" + e.getMessage());
                }


            }
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserAcountSettings: datasnapshot" + ds);
                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername()
                );
                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail()
                );
                user.setPhone_number(
                        ds.child(userID)
                                .getValue(User.class)
                                .getPhone_number()
                );
                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()
                );
            }
        }
        return new UserSettings(user, settings);
    }

    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: update username to" + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    public void updateEmail(String email) {
        Log.d(TAG, "updateEmail: update email to " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }

    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber) {
        if (displayName != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }
        if (description != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }
        if (website != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }
        if (phoneNumber != 0) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }
    }
}
