package com.ulasgokce.myapplication.Login;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ulasgokce.myapplication.R;
import com.ulasgokce.myapplication.Utils.FirebaseMethods;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context mContext;
    private ProgressBar mProgressBar;
    private String email, password, username;
    private EditText mEmail, mPassword, mUserName;
    private Button btnRegister;
    private TextView mPleaseWait;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private String append = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = RegisterActivity.this;
        firebaseMethods = new FirebaseMethods(mContext);
        initWidgets();
        setupFirebaseAuth();
        init();
    }

    private void initWidgets() {
        mProgressBar = findViewById(R.id.progressBar);
        mPleaseWait = findViewById(R.id.pleaseWait);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mUserName = findViewById(R.id.input_username);

        btnRegister = findViewById(R.id.btn_register);
        mProgressBar.setVisibility(View.GONE);
        mPleaseWait.setVisibility(View.GONE);
    }

    private void init() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString();
                username = mUserName.getText().toString();
                password = mPassword.getText().toString();
                if (checkInputs(email, username, password)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);
                    firebaseMethods.registerNewEmail(email, username, password);
                }
            }
        });
    }

    private boolean checkInputs(String email, String username, String password) {
        if (email.equals("") || username.equals("") || password.equals("")) {
            Toast.makeText(mContext, "You must Fill all the fields", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /*************************************firebase**********************************/


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


                }
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: Found a match");
                        append = myRef.push().getKey().substring(3, 10);
                        Log.d(TAG, "onDataChange: username already exist.Appendingrandom string to the name:" + append);
                    }
                }
                String mUsername = "";
                mUsername = username + append;
                firebaseMethods.addNewUser(email, username, "", "", "");
                Toast.makeText(mContext, "Signup succesfull", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

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
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                           checkIfUsernameExist(username);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    finish();
                } else {
                    Log.d(TAG, "onAuthStateChanged: Signed Out");
                }
            }
        };
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
