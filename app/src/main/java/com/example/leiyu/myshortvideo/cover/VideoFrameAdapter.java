package com.example.leiyu.myshortvideo.cover;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.leiyu.myshortvideo.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leiyu on 2018/3/1.
 */

public class VideoFrameAdapter extends RecyclerView.Adapter{

    private List<VideoFrameInfo> mFrameLists = new ArrayList<>();
    private LayoutInflater mInflater;
    private int itemWidth;
    private int itemHeight;
    private DisplayImageOptions mOptions;

    public VideoFrameAdapter(Context context, int itemWidth, int itemHeight) {
        this.mInflater = LayoutInflater.from(context);
        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        this.mOptions = new DisplayImageOptions.Builder().
                cacheInMemory(true).cacheOnDisk(true)
                .showImageOnLoading(R.mipmap.lf_module_background)
                .showImageForEmptyUri(R.mipmap.lf_module_background)
                .showImageOnFail(R.mipmap.lf_module_background)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EditViewHolder(mInflater.inflate(R.layout.lf_ugc_publish_video_cover_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        EditViewHolder viewHolder = (EditViewHolder) holder;
        ImageLoader.getInstance().displayImage("file://" + mFrameLists.get(position).path, viewHolder.img);
    }

    @Override
    public int getItemCount() {
        return mFrameLists.size();
    }

    private final class EditViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        EditViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.coverImage);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img.getLayoutParams();
            layoutParams.width = itemWidth;
            layoutParams.height = itemHeight;
            img.setLayoutParams(layoutParams);
        }
    }

    public void addItemVideoInfo(VideoFrameInfo info) {
        mFrameLists.add(info);
        notifyItemInserted(mFrameLists.size());
    }
}
