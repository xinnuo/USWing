package com.meida.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.meida.uswing.R;
import com.sunfusheng.GlideImageView;

import tv.danmaku.ijk.media.video.StandardVideoPlayer;

/**
 * 无任何控制ui的播放
 */
public class EmptyControlVideo extends StandardVideoPlayer {

    private GlideImageView mCoverImage;
    private View mFront;

    public EmptyControlVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public EmptyControlVideo(Context context) {
        super(context);
    }

    public EmptyControlVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mFront = findViewById(R.id.front);
        mCoverImage = findViewById(R.id.thumbImage);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_empty;
    }

    public View getFront() {
        return mFront;
    }

    public GlideImageView getCoverImage() {
        return mCoverImage;
    }

    public void loadCoverImage(String url) {
        mCoverImage.load(url);
    }

    @Override
    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {
        super.touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
        //不给触摸快进，如果需要，屏蔽下方代码即可
        mChangePosition = false;

        //不给触摸音量，如果需要，屏蔽下方代码即可
        mChangeVolume = false;

        //不给触摸亮度，如果需要，屏蔽下方代码即可
        mBrightness = false;
    }

    @Override
    protected void touchDoubleUp() {
        //super.touchDoubleUp();
        //不需要双击暂停
    }

}
