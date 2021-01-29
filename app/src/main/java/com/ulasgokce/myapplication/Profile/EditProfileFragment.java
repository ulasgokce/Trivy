package com.ulasgokce.myapplication.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ulasgokce.myapplication.Dialogs.ConfirmPasswordDialog;
import com.ulasgokce.myapplication.Models.User;
import com.ulasgokce.myapplication.Models.UserAccountSettings;
import com.ulasgokce.myapplication.Models.UserSettings;
import com.ulasgokce.myapplication.R;
import com.ulasgokce.myapplication.Share.ShareActivity;
import com.ulasgokce.myapplication.Utils.FirebaseMethods;
import com.ulasgokce.myapplication.Utils.UniversalImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener {

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password : " + password);
        AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(), password);
        mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: user reauthenticated");
                    mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if (task.isSuccessful()) {
                                try {
                                    if (task.getResult().getSignInMethods().size() == 1) {
                                        Log.d(TAG, "onComplete: That email is already in use");
                                        Toast.makeText(getActivity(), " That email is already in use", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Log.d(TAG, "onComplete: That email is available");
                                        mAuth.getCurrentUser().updateEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "onComplete: User email adress updated");
                                                    Toast.makeText(getActivity(), "Email Updated", Toast.LENGTH_SHORT).show();
                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                }
                                            }
                                        });
                                    }
                                } catch (NullPointerException e) {
                                    Log.e(TAG, "onComplete: NullPointerException " + e.getMessage());
                                }
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "onComplete: reauthenditcation failed");
                }
            }
        });

    }

    private static final String TAG = "EditProfileFragment";
    private EditText mDisplayName, mWebsite, mUsernmame, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;
    private UserSettings mUserSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mDisplayName = view.findViewById(R.id.display_name);
        mWebsite = view.findViewById(R.id.website);
        mUsernmame = view.findViewById(R.id.username);
        mDescription = view.findViewById(R.id.description);
        mEmail = view.findViewById(R.id.email);
        mPhoneNumber = view.findViewById(R.id.phonenumber);
        mChangeProfilePhoto = view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods = new FirebaseMethods(getActivity());


        /*  setProfileImage();*/
        setupFirebaseAuth();
        ImageView backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        ImageView checkmark = view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileSettings();
            }
        });
        return view;
    }

    private void saveProfileSettings() {
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsernmame.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());


        if (!mUserSettings.getUser().getUsername().equals(username)) {
            checkIfUsernameExist(username);
        }
        if (!mUserSettings.getUser().getEmail().equals(email)) {
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this, 1);
        }
        if (!mUserSettings.getUserAccountSettings().getDisplay_name().equals(displayName)) {
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null, 0);
        }
        if (!mUserSettings.getUserAccountSettings().getWebsite().equals(website)) {
            mFirebaseMethods.updateUserAccountSettings(null, website, null, 0);
        }
        if (!mUserSettings.getUserAccountSettings().getDescription().equals(description)) {
            mFirebaseMethods.updateUserAccountSettings(null, null, description, 0);
        }
        if (!String.valueOf(mUserSettings.getUser().getPhone_number()).equals(phoneNumber)) {
            mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber);
        }


    }

    private void checkIfUsernameExist(String username) {
        Log.d(TAG, "checkIfUsernameExist: check if " + username + "exists");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Succesfully Saved", Toast.LENGTH_SHORT).show();

                }
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: Found a match");
                        Toast.makeText(getActivity(), "That username already exits.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {
      /*Log.d(TAG, "setProfileWidgets: setting widgets with data retriecing from firebase database:" + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retriecing from firebase database:" + userSettings.getUserAccountSettings().getUsername());*/
        User user = userSettings.getUser();
        mUserSettings = userSettings;
        UserAccountSettings settings = userSettings.getUserAccountSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsernmame.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPhoneNumber.setText(String.valueOf(user.getPhone_number()));
        mEmail.setText(user.getEmail());
        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });


    }

    /*************************************firebase**********************************/


    private void setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();
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
                setProfileWidgets(mFirebaseMethods.getUserAccountSettings(snapshot));
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
