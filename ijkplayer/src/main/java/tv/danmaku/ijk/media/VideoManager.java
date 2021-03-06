package tv.danmaku.ijk.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import tv.danmaku.ijk.media.listener.IMediaPlayerListener;
import tv.danmaku.ijk.media.utils.CommonUtil;
import tv.danmaku.ijk.media.video.base.VideoPlayer;

import static tv.danmaku.ijk.media.utils.CommonUtil.hideNavKey;

/**
 * 视频管理，单例
 */
public class VideoManager extends VideoBaseManager {

    public static final int SMALL_ID = R.id.small_id;

    public static final int FULLSCREEN_ID = R.id.full_id;

    public static String TAG = "VideoManager";

    @SuppressLint("StaticFieldLeak")
    private static VideoManager mVideoManager;

    private VideoManager() {
        init();
    }

    /**
     * 单例管理器
     */
    public static synchronized VideoManager instance() {
        if (mVideoManager == null) {
            mVideoManager = new VideoManager();
        }
        return mVideoManager;
    }

    /**
     * 同步创建一个临时管理器
     */
    public static synchronized VideoManager tmpInstance(IMediaPlayerListener listener) {
        VideoManager tmpVideoManager = new VideoManager();
        tmpVideoManager.bufferPoint = mVideoManager.bufferPoint;
        tmpVideoManager.optionModelList = mVideoManager.optionModelList;
        tmpVideoManager.playTag = mVideoManager.playTag;
        tmpVideoManager.currentVideoWidth = mVideoManager.currentVideoWidth;
        tmpVideoManager.currentVideoHeight = mVideoManager.currentVideoHeight;
        tmpVideoManager.context = mVideoManager.context;
        tmpVideoManager.lastState = mVideoManager.lastState;
        tmpVideoManager.playPosition = mVideoManager.playPosition;
        tmpVideoManager.timeOut = mVideoManager.timeOut;
        tmpVideoManager.needMute = mVideoManager.needMute;
        tmpVideoManager.needTimeOutOther = mVideoManager.needTimeOutOther;
        tmpVideoManager.setListener(listener);
        return tmpVideoManager;
    }

    /**
     * 替换管理器
     */
    public static synchronized void changeManager(VideoManager videoManager) {
        mVideoManager = videoManager;
    }

    /**
     * 退出全屏，主要用于返回键
     *
     * @return 返回是否全屏
     */
    @SuppressWarnings("ResourceType")
    public static boolean backFromWindowFull(Context context) {
        boolean backFrom = false;
        ViewGroup vp = (CommonUtil.scanForActivity(context)).findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        if (oldF != null) {
            backFrom = true;
            hideNavKey(context);
            if (VideoManager.instance().lastListener() != null) {
                VideoManager.instance().lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos() {
        if (VideoManager.instance().listener() != null) {
            VideoManager.instance().listener().onCompletion();
        }
        VideoManager.instance().releaseMediaPlayer();
    }

    /**
     * 暂停播放
     */
    public static void onPause() {
        if (VideoManager.instance().listener() != null) {
            VideoManager.instance().listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public static void onResume() {
        if (VideoManager.instance().listener() != null) {
            VideoManager.instance().listener().onVideoResume();
        }
    }

    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    public static void onResume(boolean seek) {
        if (VideoManager.instance().listener() != null) {
            VideoManager.instance().listener().onVideoResume(seek);
        }
    }

    /**
     * 当前是否全屏状态
     *
     * @return 当前是否全屏状态， true代表是。
     */
    @SuppressWarnings("ResourceType")
    public static boolean isFullState(Activity activity) {
        ViewGroup vp = (CommonUtil.scanForActivity(activity)).findViewById(Window.ID_ANDROID_CONTENT);
        final View full = vp.findViewById(FULLSCREEN_ID);
        VideoPlayer videoPlayer = null;
        if (full != null) {
            videoPlayer = (VideoPlayer) full;
        }
        return videoPlayer != null;
    }

}