package com.ulasgokce.myapplication.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ulasgokce.myapplication.Login.LoginActivity;
import com.ulasgokce.myapplication.Models.Photo;
import com.ulasgokce.myapplication.R;
import com.ulasgokce.myapplication.Utils.BottomNavigationViewHelper;
import com.ulasgokce.myapplication.Utils.MainFeedListAdapter;
import com.ulasgokce.myapplication.Utils.SectionsPagerAdapter;
import com.ulasgokce.myapplication.Utils.UniversalImageLoader;
import com.ulasgokce.myapplication.Utils.ViewCommentsFragment;


public class HomeActivity extends AppCompatActivity implements MainFeedListAdapter.OnLoadMoreItemsListener {


    @Override
    public void onLoadMoreItems() {
        HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPager.getCurrentItem());
        if (fragment != null) {
            fragment.displayMorePhotos();
        }
    }

    private Context mContext = HomeActivity.this;
    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mViewPager = findViewById(R.id.viewpager_container);
        mFrameLayout = findViewById(R.id.container);
        mRelativeLayout = findViewById(R.id.relLayoutParent);

        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();
        setupFirebaseAuth();

    }

    public void hideLayout() {
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }

    public void showLayout() {
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mFrameLayout.getVisibility() == View.VISIBLE) {
            showLayout();
        }
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CameraFragment());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new MessagesFragment());

        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.trivy_logo);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_arrow);
    }

    public void onCommentThreadSelected(Photo photo) {
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    private void setupBottomNavigationView() {
        BottomNavigationView view = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.enableNavigation(mContext, this, view);
        Menu menu = view.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    /*************************************firebase**********************************/

    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: userlogged in");
        if (user == null) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
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
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mViewPager.setCurrentItem(HOME_FRAGMENT);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}