package com.ulasgokce.myapplication.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.ulasgokce.myapplication.R;
import com.ulasgokce.myapplication.Utils.BottomNavigationViewHelper;
import com.ulasgokce.myapplication.Utils.Permissions;
import com.ulasgokce.myapplication.Utils.SectionsPagerAdapter;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";
    private Context mContext = ShareActivity.this;
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        if (checkPermissionsArray(Permissions.PERMISSIONS)) {
            setupViewPager();
        } else {
            verifyPermissions(Permissions.PERMISSIONS);
        }

    }

    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }

    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
    adapter.addFragment(new GalleryFragment());
    adapter.addFragment(new PhotoFragment());
    mViewPager = findViewById(R.id.viewpager_container);
    mViewPager.setAdapter(adapter);
    TabLayout tabLayout =findViewById(R.id.tabsBottom);
    tabLayout.setupWithViewPager(mViewPager);
    tabLayout.getTabAt(0).setText(getString(R.string.gallery));
    tabLayout.getTabAt(1).setText(R.string.photo);
    }
    public int getTask(){
        Log.d(TAG, "getTask: Task " + getIntent().getFlags());
        return getIntent().getFlags();
    }

    private void verifyPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(ShareActivity.this, permissions, VERIFY_PERMISSIONS_REQUEST);
    }


    private boolean checkPermissionsArray(String[] permissions) {
        Log.d(TAG, "checkPermissinsArray: checking perpissions array");
        for (int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPermissions(String permission) {
        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }


    private void setupBottomNavigationView() {
        BottomNavigationView view = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.enableNavigation(mContext,this, view);
        Menu menu = view.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
