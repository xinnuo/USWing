package com.meida.view;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;

import com.meida.uswing.R;
import com.sunfusheng.GlideImageView;

import tv.danmaku.ijk.media.MultiVideoManager;
import tv.danmaku.ijk.media.utils.Debuger;
import tv.danmaku.ijk.media.video.StandardVideoPlayer;
import tv.danmaku.ijk.media.video.base.VideoViewBridge;

public class EmptyNoControlVideo extends StandardVideoPlayer {

    private final String TAG = EmptyNoControlVideo.this.getClass().getSimpleName();

    private GlideImageView mCoverImage;
    private OnPlayListener listener;

    public EmptyNoControlVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public EmptyNoControlVideo(Context context) {
        super(context);
    }

    public EmptyNoControlVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mCoverImage = findViewById(R.id.thumbImage);

        if (mThumbImageViewLayout != null &&
                (mCurrentState == -1
                        || mCurrentState == CURRENT_STATE_NORMAL
                        || mCurrentState == CURRENT_STATE_ERROR)) {
            mThumbImageViewLayout.setVisibility(VISIBLE);
        }

        onAudioFocusChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        };
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_no_control;
    }

    public void loadCoverImage(String url) {
        mCoverImage.load(url);
    }

    @Override
    protected void releaseVideos() {
        MultiVideoManager.releaseAllVideos(getKey());
    }

    @Override
    protected int getFullId() {
        return MultiVideoManager.FULLSCREEN_ID;
    }

    @Override
    protected int getSmallId() {
        return MultiVideoManager.SMALL_ID;
    }

    @Override
    public VideoViewBridge getVideoManager() {
        MultiVideoManager.getMultiManager(getKey()).initContext(getContext().getApplicationContext());
        return MultiVideoManager.getMultiManager(getKey());
    }

    public String getKey() {
        if (mPlayPosition == -22) {
            Debuger.printfError(TAG + " used getKey() " + "******* PlayPosition never set. ********");
        }
        if (TextUtils.isEmpty(mPlayTag)) {
            Debuger.printfError(TAG + " used getKey() " + "******* PlayTag never set. ********");
        }
        return TAG + mPlayPosition + mPlayTag;
    }

    @Override
    protected void updateStartImage() {
        if (listener != null) {
            listener.doWork(mCurrentState == CURRENT_STATE_PLAYING);
        }
    }

    @Override
    protected void touchDoubleUp() {
        // super.touchDoubleUp();
    }

    public void setOnPlayListener(OnPlayListener listener) {
        this.listener = listener;
    }

    public void updataProgress(int progress) {
        boolean isPlaying = getCurrentPlayer().isInPlayingState();
        if (isPlaying) {
            int time = progress * getDuration() / 100;
            getVideoManager().seekTo(time);
        }
    }

    public boolean isPlaying() {
        return getCurrentPlayer().isInPlayingState();
    }

    public void startToClick() {
        clickStartIcon();
    }

    /******************* 下方两个重载方法，在播放开始前不屏蔽封面，不需要可屏蔽 ********************/
    @Override
    public void onSurfaceUpdated(Surface surface) {
        super.onSurfaceUpdated(surface);
        if (mThumbImageViewLayout != null && mThumbImageViewLayout.getVisibility() == VISIBLE) {
            mThumbImageViewLayout.setVisibility(INVISIBLE);
        }
    }

    @Override
    protected void setViewShowState(View view, int visibility) {
        if (view == mThumbImageViewLayout && visibility != VISIBLE) {
            return;
        }
        super.setViewShowState(view, visibility);
    }

    public interface OnPlayListener {
        void doWork(boolean isPlaying);
    }

}
