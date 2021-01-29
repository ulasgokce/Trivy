package com.ulasgokce.myapplication.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ulasgokce.myapplication.R;
import com.ulasgokce.myapplication.Utils.BottomNavigationViewHelper;
import com.ulasgokce.myapplication.Utils.FirebaseMethods;
import com.ulasgokce.myapplication.Utils.SectionsStatePagerAdapter;

import java.util.ArrayList;

public class AccountSettingsActivity extends AppCompatActivity {
    private static final String TAG = "AccountSettingsActivity";
    private Context mContext;
    private static final int ACTIVITY_NUM = 4;
    public SectionsStatePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = AccountSettingsActivity.this;
        setContentView(R.layout.activity_accountsettings);
        mViewPager = findViewById(R.id.viewpager_container);
        mRelativeLayout = findViewById(R.id.relLayout1);
        setupSettingsList();
        setupFragments();
        ImageView backArrow = findViewById(R.id.backArrow);
        getIncomingIntent();
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getIncomingIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.selected_image)) || intent.hasExtra(getString(R.string.selected_bitmap))) {
            Log.d(TAG, "getIncomingIntent: selected image or bitmap");

            Log.d(TAG, "getIncomingIntent: new incoming imgUrl" + intent.getStringExtra(getString(R.string.selected_image)));
            if (intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment))) {
                if (intent.hasExtra(getString(R.string.selected_image))) {
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0,
                            intent.getStringExtra(getString(R.string.selected_image)), null);
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0,
                            null, (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap)));
                }
            }
        }
            if (intent.hasExtra(getString(R.string.calling_activity))) {
                Log.d(TAG, "getIncomingIntent: received incoming intent from" + getString(R.string.profile_activity));
                setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
            }

    }

    private void setupFragments() {
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile_fragment));
        pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out_fragment));
    }

    public void setViewPager(int fragmentNumber) {
        mRelativeLayout.setVisibility(View.GONE);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);
    }


    private void setupSettingsList() {
        ListView listView = findViewById(R.id.lvAccountSettings);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.sign_out_fragment));

        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setViewPager(position);
            }
        });

    }

    private void setupBottomNavigationView() {
        BottomNavigationView view = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.enableNavigation(mContext, this, view);
        Menu menu = view.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
