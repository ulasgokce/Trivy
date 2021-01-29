package com.ulasgokce.myapplication.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ulasgokce.myapplication.Login.LoginActivity;
import com.ulasgokce.myapplication.R;

public class SignOutFragment extends Fragment {
    private static final String TAG = "SignOutFragment";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressBar mProgressBar;
    private TextView tvSignOut, tvSigningout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signout, container, false);
        tvSignOut = view.findViewById(R.id.tvConfirmSignout);
        tvSigningout = view.findViewById(R.id.tvSingingout);
        mProgressBar = view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        tvSigningout.setVisibility(View.GONE);
        setupFirebaseAuth();
        Button btnSignout = view.findViewById(R.id.btnConfirmSignout);
        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                tvSigningout.setVisibility(View.VISIBLE);
                mAuth.signOut();
                getActivity().finish();
            }
        });
        return view;
    }


    private void setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: signed in");
                } else {
                    Log.d(TAG, "onAuthStateChanged: Signed Out");
                    Intent i =new Intent(getActivity(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
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
