package com.example.leiyu.myshortvideo.videogride.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.leiyu.myshortvideo.R;
import com.example.leiyu.myshortvideo.videogride.dialog.PegasusAlertDialog;
import com.example.leiyu.myshortvideo.videogride.entity.FunctionConfig;
import com.example.leiyu.myshortvideo.videogride.entity.LocalMedia;
import com.example.leiyu.myshortvideo.videogride.observable.ImagesObservable;
import com.example.leiyu.myshortvideo.entity.LocalMediaLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leiyu on 2018/3/2.
 */

public class PictureImageGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public static final int TYPE_CAMERA = 1;
    public static final int TYPE_PICTURE = 2;

    private Context context;
    private boolean showCamera = true;
    private OnPhotoSelectChangedListener imageSelectChangedListener;
    private int maxSelectNum;
    private List<LocalMedia> images = new ArrayList<>();
    private List<LocalMedia> selectImages = new ArrayList<>();
    private boolean enablePreview;
    private int selectMode = FunctionConfig.MODE_MULTIPLE;
    private boolean enablePreviewVideo = false;
    private int cb_drawable;
    private boolean is_checked_num;
    private DisplayImageOptions options;
    private boolean isVideoDisEnable;

    public PictureImageGridAdapter(Context context, boolean showCamera, int maxSelectNum, int mode, boolean enablePreview, boolean enablePreviewVideo, int cb_drawable, boolean is_checked_num) {
        this.context = context;
        this.selectMode = mode;
        this.showCamera = showCamera;
        this.maxSelectNum = maxSelectNum;
        this.enablePreview = enablePreview;
        this.enablePreviewVideo = enablePreviewVideo;
        this.cb_drawable = cb_drawable;
        this.is_checked_num = is_checked_num;
        this.options = new DisplayImageOptions.Builder().
                cacheInMemory(true).cacheOnDisk(true)
                .showImageOnLoading(R.mipmap.lf_module_background)
                .showImageForEmptyUri(R.mipmap.lf_module_background)
                .showImageOnFail(R.mipmap.lf_module_background)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    public void bindImagesData(List<LocalMedia> images) {
        this.images = images;
        notifyDataSetChanged();
    }


    public void bindSelectImages(List<LocalMedia> images) {
        this.selectImages = images;
        notifyDataSetChanged();
        subSelectPosition();
    }

    public List<LocalMedia> getImages() {
        return images;
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera && position == 0) {
            return TYPE_CAMERA;
        } else {
            return TYPE_PICTURE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CAMERA) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lf_ugc_publish_picture_item_camera, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lf_ugc_publish_picture_image_grid_item, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == TYPE_CAMERA) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.headerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (imageSelectChangedListener != null) {
                        imageSelectChangedListener.onTakePhoto();
                    }
                }
            });
        } else {
            final ViewHolder contentHolder = (ViewHolder) holder;
            final LocalMedia image = images.get(showCamera ? position - 1 : position);
            image.position = contentHolder.getAdapterPosition();
            final String path = image.getSourcePath();
            final int type = image.getType();

            if (type== LocalMediaLoader.TYPE_VIDEO){
                contentHolder.viewBottomBg.setVisibility(View.VISIBLE);
            }else{
                contentHolder.viewBottomBg.setVisibility(View.GONE);
            }

            if (is_checked_num) {
                notifyCheckChanged(contentHolder, image);
            }

            selectImage(contentHolder, isSelected(image), false);

            if (selectMode == FunctionConfig.MODE_SINGLE) {
                if(type == LocalMediaLoader.TYPE_IMAGE) {
                    contentHolder.check.setVisibility(View.GONE);
                    contentHolder.icon_duration.setVisibility(View.GONE);
                    contentHolder.tv_duration.setVisibility(View.GONE);
                }else if(type == LocalMediaLoader.TYPE_VIDEO){
                    contentHolder.check.setVisibility(View.GONE);
                    contentHolder.icon_duration.setVisibility(View.VISIBLE);
                    contentHolder.tv_duration.setVisibility(View.VISIBLE);
                    long duration = image.getDuration();
                    contentHolder.tv_duration.setText(timeParse(duration));
                    if(duration < FunctionConfig.DEFAULT_SELECT_VIDEO_MIN_DURATION) {
                        contentHolder.picture.setColorFilter(ContextCompat.getColor(context, R.color.image_overlay2), PorterDuff.Mode.SRC_ATOP);
                    }else{
                        contentHolder.picture.setColorFilter(ContextCompat.getColor(context, R.color.transparent), PorterDuff.Mode.SRC_ATOP);
                    }
                }
            } else {
                contentHolder.check.setVisibility(View.VISIBLE);
                contentHolder.icon_duration.setVisibility(View.VISIBLE);
                contentHolder.tv_duration.setVisibility(View.VISIBLE);
                if (type == LocalMediaLoader.TYPE_VIDEO) {
                    contentHolder.check.setVisibility(View.GONE);
                    contentHolder.icon_duration.setVisibility(View.VISIBLE);
                    long duration = image.getDuration();
                    contentHolder.tv_duration.setText(timeParse(duration));
                    //置灰操作
                    if (isVideoDisEnable){
                        contentHolder.picture.setColorFilter(ContextCompat.getColor(context, R.color.image_overlay2), PorterDuff.Mode.SRC_ATOP);
                    }else{
                        if (selectImages.size() > 0 ) {
                            contentHolder.picture.setColorFilter(ContextCompat.getColor(context, R.color.image_overlay2), PorterDuff.Mode.SRC_ATOP);
                        } else {
                            contentHolder.picture.setColorFilter(ContextCompat.getColor(context, R.color.transparent), PorterDuff.Mode.SRC_ATOP);
                        }
                    }
                } else {
                    contentHolder.check.setVisibility(View.VISIBLE);
                    contentHolder.icon_duration.setVisibility(View.GONE);
                    contentHolder.tv_duration.setText("");
                }
            }
            ImageLoader.getInstance().displayImage("file://" + path, contentHolder.picture, options);
            if (enablePreview || enablePreviewVideo) {
                contentHolder.check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeCheckboxState(contentHolder, image, position);
                    }
                });
            }
            contentHolder.contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (type == LocalMediaLoader.TYPE_VIDEO && imageSelectChangedListener != null && selectImages.size() <= 0 && !isVideoDisEnable) {
                        int index = showCamera ? position - 1 : position;
                        imageSelectChangedListener.onPictureClick(image, index);
                    } else if (type == LocalMediaLoader.TYPE_IMAGE && imageSelectChangedListener != null) {
                        int index = showCamera ? position - 1 : position;
                        imageSelectChangedListener.onPictureClick(image, index);
                    } else {
                        changeCheckboxState(contentHolder, image, position);
                    }
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return showCamera ? images.size() + 1 : images.size();
    }


    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        View headerView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerView = itemView;
        }
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {
        ImageView picture;
        TextView check;
        TextView tv_duration;
        View contentView;
        ImageView icon_duration;
        View viewBottomBg;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            picture = (ImageView) itemView.findViewById(R.id.picture);
            check = (TextView) itemView.findViewById(R.id.check);
            tv_duration = (TextView) itemView.findViewById(R.id.tv_duration);
            icon_duration = (ImageView) itemView.findViewById(R.id.lf_ugc_publish_video_rl_icon);
            viewBottomBg=itemView.findViewById(R.id.id_video_bg);
        }
    }

    /****
     * 是否将视频置灰
     * @param enable enable
     */
    public  void setVideoDisEnable(boolean enable){
        this.isVideoDisEnable=enable;
        notifyDataSetChanged();
    }

    public boolean isSelected(LocalMedia image) {
        for (LocalMedia media : selectImages) {
            if (media.getSourcePath().equals(image.getSourcePath())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 选择按钮更新
     */
    private void notifyCheckChanged(ViewHolder viewHolder, LocalMedia imageBean) {
        viewHolder.check.setText("");
        for (LocalMedia media : selectImages) {
            if (media.getSourcePath().equals(imageBean.getSourcePath())) {
                imageBean.setNum(media.getNum());
                viewHolder.check.setText(String.valueOf(imageBean.getNum()));
            }
        }
    }

    /**
     * 改变图片选中状态
     *
     * @param contentHolder
     * @param image
     */

    private void changeCheckboxState(ViewHolder contentHolder, LocalMedia image, int position) {
        if (image.getType() == LocalMediaLoader.TYPE_VIDEO) return;
        boolean isChecked = contentHolder.check.isSelected();

        if (selectImages.size() >= maxSelectNum && !isChecked) {
            PegasusAlertDialog.ShowAlertDialog(context, context.getString(R.string.lf_message_max_num, maxSelectNum)
                    , "我知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
            );
            return;
        }
        if (isChecked) {
            for (LocalMedia media : selectImages) {
                if (media.getSourcePath().equals(image.getSourcePath())) {
                    selectImages.remove(media);
                    ImagesObservable.getInstance().removeASelectedImage(media);
                    subSelectPosition();
                    break;
                }
            }
        } else {
            selectImages.add(image);
            ImagesObservable.getInstance().addASelectedImage(image);
            image.setNum(selectImages.size());
        }
        if (imageSelectChangedListener != null) {
            imageSelectChangedListener.onChange(selectImages);
        }
        selectImage(contentHolder, !isChecked, true);
        notifyItemChanged(position);
        if (selectImages.size() == 1 || selectImages.size() == 0) {
            notifyDataSetChanged();
        }
    }

    /**
     * 更新选择的顺序
     */
    private void subSelectPosition() {
        if (is_checked_num) {
            for (int index = 0, len = selectImages.size(); index < len; index++) {
                LocalMedia media = selectImages.get(index);
                media.setNum(index + 1);
                notifyItemChanged(media.position);
            }
        }
    }

    private void selectImage(ViewHolder holder, boolean isChecked, boolean isAnim) {
        holder.check.setSelected(isChecked);
        if (isChecked) {
            if (isAnim) {
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.lf_ugc_publish_modal_in);
                holder.check.startAnimation(animation);
            }
            holder.picture.setColorFilter(ContextCompat.getColor(context, R.color.image_overlay2), PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.picture.setColorFilter(ContextCompat.getColor(context, R.color.transparent), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public interface OnPhotoSelectChangedListener {
        void onTakePhoto();

        void onChange(List<LocalMedia> selectImages);


        void onPictureClick(LocalMedia media, int position);
    }

    public void setOnPhotoSelectChangedListener(OnPhotoSelectChangedListener imageSelectChangedListener) {
        this.imageSelectChangedListener = imageSelectChangedListener;
    }

    private static String timeParse(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = /*Math.round((float) seconds / 1000) */seconds/1000;
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }
}
