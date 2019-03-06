package com.meida.view;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.widget.SeekBar;

import com.meida.uswing.R;
import com.sunfusheng.GlideImageView;

import tv.danmaku.ijk.media.MultiVideoManager;
import tv.danmaku.ijk.media.utils.Debuger;
import tv.danmaku.ijk.media.video.StandardVideoPlayer;
import tv.danmaku.ijk.media.video.base.VideoViewBridge;

public class CustomVideoPlayer extends StandardVideoPlayer {

    private final String TAG = CustomVideoPlayer.this.getClass().getSimpleName();

    private CustomVideoPlayer linkedPlayer;
    private GlideImageView mCoverImage;
    private GlideImageView mAdd;

    public CustomVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public CustomVideoPlayer(Context context) {
        super(context);
    }

    public CustomVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mCoverImage = findViewById(R.id.thumbImage);
        mAdd = findViewById(R.id.add);

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

        mStartButton.setOnClickListener(v -> {
            clickStartIcon();
            if (linkedPlayer != null) {
                linkedPlayer.clickStartIcon();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_control;
    }

    /**
     * 获取添加按键
     */
    public GlideImageView getAddButton() {
        return mAdd;
    }

    public void loadCoverImage(String url) {
        mCoverImage.load(url, R.mipmap.default_compare);
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
        GlideImageView imageView = (GlideImageView) mStartButton;
        imageView.setImageResource(mCurrentState == CURRENT_STATE_PLAYING ? R.mipmap.mes_icon17 : R.mipmap.mes_icon16);
        mAdd.setVisibility(mCurrentState == CURRENT_STATE_PLAYING ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void touchDoubleUp() {
        super.touchDoubleUp();
        if (linkedPlayer != null) {
            linkedPlayer.clickStartIcon();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        super.onStopTrackingTouch(seekBar);

        if (linkedPlayer != null) {
            boolean isPlaying = linkedPlayer.getCurrentPlayer().isInPlayingState();
            if (isPlaying) {
                int time = seekBar.getProgress() * linkedPlayer.getDuration() / 100;
                linkedPlayer.getVideoManager().seekTo(time);
            }
        }
    }

    public void setLinkedPlayer(CustomVideoPlayer player) {
        this.linkedPlayer = player;
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

}
