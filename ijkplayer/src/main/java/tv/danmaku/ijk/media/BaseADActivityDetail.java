package tv.danmaku.ijk.media;

import android.content.res.Configuration;
import android.view.View;

import tv.danmaku.ijk.media.builder.VideoOptionBuilder;
import tv.danmaku.ijk.media.listener.VideoAllCallBack;
import tv.danmaku.ijk.media.utils.OrientationUtils;
import tv.danmaku.ijk.media.video.ADVideoPlayer;
import tv.danmaku.ijk.media.video.base.BaseVideoPlayer;
import tv.danmaku.ijk.media.video.base.VideoPlayer;
import tv.danmaku.ijk.media.video.base.VideoView;

/**
 * 详情AD模式播放页面基础类
 */
public abstract class BaseADActivityDetail<T extends BaseVideoPlayer, R extends ADVideoPlayer> extends BaseActivityDetail<T> {

    protected OrientationUtils mADOrientationUtils;

    @Override
    public void initVideo() {
        super.initVideo();
        //外部辅助的旋转，帮助全屏
        mADOrientationUtils = new OrientationUtils(this, getADVideoPlayer());
        //初始化不打开外部的旋转
        mADOrientationUtils.setEnable(false);
        if (getADVideoPlayer().getFullscreenButton() != null) {
            getADVideoPlayer().getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //直接横屏
                    showADFull();
                    clickForFullScreen();
                }
            });
        }
    }

    /**
     * 选择builder模式
     */
    @Override
    public void initVideoBuilderMode() {
        super.initVideoBuilderMode();
        getADVideoOptionBuilder()
                .setVideoAllCallBack(new VideoAllCallBack() {

                    @Override
                    public void onStartPrepared(String url, Object... objects) {
                        super.onStartPrepared(url, objects);
                        //开始播放了才能旋转和全屏
                        mADOrientationUtils.setEnable(getDetailOrientationRotateAuto());
                    }

                    @Override
                    public void onAutoComplete(String url, Object... objects) {
                        //广告结束，释放
                        getADVideoPlayer().getCurrentPlayer().release();
                        getADVideoPlayer().onVideoReset();
                        getADVideoPlayer().setVisibility(View.GONE);
                        //开始播放原视频，根据是否处于全屏状态判断
                        getVideoPlayer().getCurrentPlayer().startAfterPrepared();
                        if (getADVideoPlayer().getCurrentPlayer().isIfCurrentIsFullscreen()) {
                            getADVideoPlayer().removeFullWindowViewOnly();
                            if (!getVideoPlayer().getCurrentPlayer().isIfCurrentIsFullscreen()) {
                                showFull();
                                getVideoPlayer().setSaveBeforeFullSystemUiVisibility(getADVideoPlayer().getSaveBeforeFullSystemUiVisibility());
                            }
                        }
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        //退出全屏逻辑
                        if (mADOrientationUtils != null) {
                            mADOrientationUtils.backToProtVideo();
                        }
                        if (getVideoPlayer().getCurrentPlayer().isIfCurrentIsFullscreen()) {
                            getVideoPlayer().onBackFullscreen();
                        }
                    }

                })
                .build(getADVideoPlayer());
    }

    /**
     * 正常视频内容的全屏显示
     */
    @Override
    public void showFull() {
        if (orientationUtils.getIsLand() != 1) {
            //直接横屏
            orientationUtils.resolveByClick();
        }
        getVideoPlayer().startWindowFullscreen(this, hideActionBarWhenFull(), hideStatusBarWhenFull());
    }

    @Override
    public void onBackPressed() {
        if (mADOrientationUtils != null) {
            mADOrientationUtils.backToProtVideo();
        }
        if (VideoADManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VideoADManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoADManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoADManager.releaseAllVideos();
        if (mADOrientationUtils != null)
            mADOrientationUtils.releaseListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //如果旋转了就全屏
        boolean backUpIsPlay = isPlay;
        if (!isPause && getADVideoPlayer().getVisibility() == View.VISIBLE) {
            if (isADStarted()) {
                isPlay = false;
                getADVideoPlayer().getCurrentPlayer().onConfigurationChanged(this, newConfig, mADOrientationUtils, hideActionBarWhenFull(), hideStatusBarWhenFull());
            }
        }
        super.onConfigurationChanged(newConfig);
        isPlay = backUpIsPlay;
    }

    @Override
    public void onStartPrepared(String url, Object... objects) {
        super.onStartPrepared(url, objects);
    }

    @Override
    public void onPrepared(String url, Object... objects) {
        super.onPrepared(url, objects);
        if (isNeedAdOnStart()) {
            startAdPlay();
        }
    }

    @Override
    public void onEnterFullscreen(String url, Object... objects) {
        super.onEnterFullscreen(url, objects);
        //隐藏调全屏对象的返回按键
        VideoPlayer videoPlayer = (VideoPlayer) objects[1];
        videoPlayer.getBackButton().setVisibility(View.GONE);
    }

    @Override
    public void clickForFullScreen() { }

    protected boolean isADStarted() {
        return getADVideoPlayer().getCurrentPlayer().getCurrentState() >= 0 &&
                getADVideoPlayer().getCurrentPlayer().getCurrentState() != VideoView.CURRENT_STATE_NORMAL
                && getADVideoPlayer().getCurrentPlayer().getCurrentState() != VideoView.CURRENT_STATE_AUTO_COMPLETE;
    }

    /**
     * 显示播放广告
     */
    public void startAdPlay() {
        getADVideoPlayer().setVisibility(View.VISIBLE);
        getADVideoPlayer().startPlayLogic();
        if (getVideoPlayer().getCurrentPlayer().isIfCurrentIsFullscreen()) {
            showADFull();
            getADVideoPlayer().setSaveBeforeFullSystemUiVisibility(getVideoPlayer().getSaveBeforeFullSystemUiVisibility());
        }
    }

    /**
     * 广告视频的全屏显示
     */
    public void showADFull() {
        if (mADOrientationUtils.getIsLand() != 1) {
            mADOrientationUtils.resolveByClick();
        }
        getADVideoPlayer().startWindowFullscreen(BaseADActivityDetail.this, hideActionBarWhenFull(), hideStatusBarWhenFull());
    }

    public abstract R getADVideoPlayer();

    /**
     * 配置AD播放器
     */
    public abstract VideoOptionBuilder getADVideoOptionBuilder();

    /**
     * 是否播放开始广告
     */
    public abstract boolean isNeedAdOnStart();

}
