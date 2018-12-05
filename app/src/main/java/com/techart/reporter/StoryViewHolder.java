package com.techart.reporter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.techart.reporter.constants.FireBaseUtils;

/**
 * Created by kelvin on 2/12/18.
 */

public final class StoryViewHolder extends RecyclerView.ViewHolder {
    public TextView tvTitle;
    public TextView tvDescription;
    public TextView tvInfor;
    public TextView tvState;

    public TextView tvNumComments;
    public TextView tvNumViews;
    public TextView tvTime;

    public ImageView ivStory;
    public View mView;


    public ImageButton btnComment;
    public ImageButton btnViews;

    public StoryViewHolder(View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tv_title);

        ivStory = itemView.findViewById(R.id.iv_news);
        tvDescription = itemView.findViewById(R.id.tv_description);
        tvInfor = itemView.findViewById(R.id.tv_infor);

        btnComment = itemView.findViewById(R.id.commentBtn);
        btnViews = itemView.findViewById(R.id.bt_views);
        tvTime = itemView.findViewById(R.id.tv_time);
        tvNumComments = itemView.findViewById(R.id.tv_numcomments);
        tvNumViews = itemView.findViewById(R.id.tv_numviews);
        this.mView = itemView;
    }

    public void setTint(Context context){
        ivStory.setColorFilter(ContextCompat.getColor(context, R.color.colorTint));
    }

    public void setIvImage(Context context, String imageUrl) {
        Glide.with(context)
        .load(imageUrl)
        .into(ivStory);
    }
    public void setPostViewed(String post_key) {
        FireBaseUtils.setPostViewed(post_key,btnViews);
    }
}
