package com.ulasgokce.myapplication.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.ulasgokce.myapplication.R;

import java.util.ArrayList;

public class GridImageAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private String mAppend;
    private ArrayList<String> imgURLs;

    public GridImageAdapter(Context mContext, int layoutResource, String mAppend, ArrayList<String> imgURLs) {
        super(mContext, layoutResource, imgURLs);
        this.mContext = mContext;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutResource = layoutResource;
        this.mAppend = mAppend;
        this.imgURLs = imgURLs;
    }

    private static class VievHolder {
        SquareImageView image;
        ProgressBar mProgressBar;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final VievHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new VievHolder();
            holder.mProgressBar = convertView.findViewById(R.id.gridImageProgressBar);
            holder.image = convertView.findViewById(R.id.gridImageView);
            convertView.setTag(holder);
        }
        else {
            holder= (VievHolder) convertView.getTag();
        }
        String imgURL = getItem(position);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mAppend + imgURL, holder.image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if (holder.mProgressBar !=null){
                    holder.mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (holder.mProgressBar !=null){
                    holder.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (holder.mProgressBar !=null){
                    holder.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (holder.mProgressBar !=null){
                    holder.mProgressBar.setVisibility(View.GONE);
                }
            }
        });
        return convertView;
    }
}
