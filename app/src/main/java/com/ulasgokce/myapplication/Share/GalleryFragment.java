package com.ulasgokce.myapplication.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.ulasgokce.myapplication.Profile.AccountSettingsActivity;
import com.ulasgokce.myapplication.R;
import com.ulasgokce.myapplication.Utils.FilePaths;
import com.ulasgokce.myapplication.Utils.FileSearch;
import com.ulasgokce.myapplication.Utils.GridImageAdapter;

import java.util.ArrayList;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private static final int NUM_GRID_COLUMN = 3;
    private String mAppend = "file:/";
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;
    private ArrayList<String> directories;
    private String mSelectedImage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        galleryImage = view.findViewById(R.id.galleryImageView);
        gridView = view.findViewById(R.id.gridView);
        mProgressBar = view.findViewById(R.id.progressBar);
        directorySpinner = view.findViewById(R.id.spinnerDirectory);
        mProgressBar.setVisibility(View.GONE);
        directories = new ArrayList<>();
        init();
        ImageView shareClose = view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing galleryfragment");
                getActivity().finish();
            }
        });


        TextView nextScreen = view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRootTask()) {
                    Intent i = new Intent(getActivity(), NextActivity.class);
                    i.putExtra(getString(R.string.selected_image), mSelectedImage);
                    startActivity(i);
                } else {
                    Intent i = new Intent(getActivity(), AccountSettingsActivity.class);
                    Log.d(TAG, "onClick: selected Image : " + mSelectedImage);
                    i.putExtra(getString(R.string.selected_image), mSelectedImage);
                    i.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(i);
                    getActivity().finish();
                }
            }
        });
        return view;
    }

    private boolean isRootTask() {
        if (((ShareActivity) getActivity()).getTask() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void init() {
        FilePaths filePaths = new FilePaths();
        if (FileSearch.getDirectoryPaths(filePaths.PICTURES) != null) {
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }
        directories.add(filePaths.CAMERA);
        ArrayList<String> directoryNames = new ArrayList<>();
        for (int i = 0; i < directories.size(); i++) {
            int index = directories.get(i).lastIndexOf("/");
            String s = directories.get(i).substring(index).replace("/", "");
            directoryNames.add(s);
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);
        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected: " + directories.get(position));
                setupGridView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setupGridView(String selectedDirectory) {
        Log.d(TAG, "setupGridView: directory chosen: " + selectedDirectory);
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectedDirectory);
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMN;
        gridView.setColumnWidth(imageWidth);
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, mAppend, imgURLs);
        gridView.setAdapter(adapter);

        try {
            setImage(imgURLs.get(0), galleryImage, mAppend);
            mSelectedImage = imgURLs.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "setupGridView: ArrayIndexOutOfBoundsException" + e.getMessage());
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setImage(imgURLs.get(position), galleryImage, mAppend);
                mSelectedImage = imgURLs.get(position);
            }
        });

    }

    private void setImage(String imgURL, ImageView image, String append) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
