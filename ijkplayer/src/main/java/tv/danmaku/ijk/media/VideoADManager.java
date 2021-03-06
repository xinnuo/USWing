package tv.danmaku.ijk.media;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import tv.danmaku.ijk.media.utils.CommonUtil;
import tv.danmaku.ijk.media.video.base.VideoPlayer;

import static tv.danmaku.ijk.media.utils.CommonUtil.hideNavKey;

/**
 * 视频管理，单例
 */
public class VideoADManager extends VideoBaseManager {

    public static final int SMALL_ID = R.id.ad_small_id;

    public static final int FULLSCREEN_ID = R.id.ad_full_id;

    public static String TAG = "VideoADManager";

    @SuppressLint("StaticFieldLeak")
    private static VideoADManager videoManager;

    private VideoADManager() {
        init();
    }

    /**
     * 单例管理器
     */
    public static synchronized VideoADManager instance() {
        if (videoManager == null) {
            videoManager = new VideoADManager();
        }
        return videoManager;
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
            if (VideoADManager.instance().lastListener() != null) {
                VideoADManager.instance().lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos() {
        if (VideoADManager.instance().listener() != null) {
            VideoADManager.instance().listener().onCompletion();
        }
        VideoADManager.instance().releaseMediaPlayer();
    }

    /**
     * 暂停播放
     */
    public static void onPause() {
        if (VideoADManager.instance().listener() != null) {
            VideoADManager.instance().listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public static void onResume() {
        if (VideoADManager.instance().listener() != null) {
            VideoADManager.instance().listener().onVideoResume();
        }
    }

    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    public static void onResume(boolean seek) {
        if (VideoADManager.instance().listener() != null) {
            VideoADManager.instance().listener().onVideoResume(seek);
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