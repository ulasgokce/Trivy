package com.ulasgokce.myapplication.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ulasgokce.myapplication.Models.Chat;
import com.ulasgokce.myapplication.R;

import java.util.List;

public class ViewChatAdapter extends RecyclerView.Adapter<ViewChatAdapter.ViewHolder> {
    private static final String TAG = "ViewChatAdapter";

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private String imageUrl;
    private List<Chat> mChat;

    public ViewChatAdapter(Context context, List<Chat> chat, String imageUrl) {
        mContext = context;
        mChat = chat;
        this.imageUrl = imageUrl;
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    @NonNull
    @Override
    public ViewChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewChatAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        holder.show_message.setText(chat.getMessage());
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(imageUrl, holder.profile_image);

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public ImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (mChat.get(position).getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
