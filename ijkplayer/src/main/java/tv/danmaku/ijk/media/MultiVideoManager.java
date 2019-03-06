package tv.danmaku.ijk.media;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.player.IPlayerManager;
import tv.danmaku.ijk.media.player.IjkPlayerManager;
import tv.danmaku.ijk.media.utils.CommonUtil;
import tv.danmaku.ijk.media.video.base.VideoPlayer;

import static tv.danmaku.ijk.media.utils.CommonUtil.hideNavKey;

/**
 * 多任务播放的管理器
 */
public class MultiVideoManager extends VideoBaseManager {

    public static final int SMALL_ID = R.id.multi_small_id;

    public static final int FULLSCREEN_ID = R.id.multi_full_id;

    public static String TAG = "MultiVideoManager";

    private static Map<String, MultiVideoManager> sMap = new HashMap<>();

    public MultiVideoManager() {
        init();
    }

    @Override
    protected IPlayerManager getPlayManager() {
        return new IjkPlayerManager();
    }

    /**
     * 退出全屏，主要用于返回键
     *
     * @return 返回是否全屏
     */
    @SuppressWarnings("ResourceType")
    public static boolean backFromWindowFull(Context context, String key) {
        boolean backFrom = false;
        ViewGroup vp = (CommonUtil.scanForActivity(context)).findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        if (oldF != null) {
            backFrom = true;
            hideNavKey(context);
            if (getMultiManager(key).lastListener() != null) {
                getMultiManager(key).lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos(String key) {
        if (getMultiManager(key).listener() != null) {
            getMultiManager(key).listener().onCompletion();
        }
        getMultiManager(key).releaseMediaPlayer();
    }


    /**
     * 暂停播放
     */
    public void onPause(String key) {
        if (getMultiManager(key).listener() != null) {
            getMultiManager(key).listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public void onResume(String key) {
        if (getMultiManager(key).listener() != null) {
            getMultiManager(key).listener().onVideoResume();
        }
    }

    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    public void onResume(String key, boolean seek) {
        if (getMultiManager(key).listener() != null) {
            getMultiManager(key).listener().onVideoResume(seek);
        }
    }

    /**
     * 单例管理器
     */
    public static synchronized Map<String, MultiVideoManager> instance() {
        return sMap;
    }

    /**
     * 单例管理器
     */
    public static synchronized MultiVideoManager getMultiManager(String key) {
        if (TextUtils.isEmpty(key)) {
            throw new IllegalStateException("key not be empty");
        }
        MultiVideoManager multiVideoManager = sMap.get(key);
        if (multiVideoManager == null) {
            multiVideoManager = new MultiVideoManager();
            sMap.put(key, multiVideoManager);
        }
        return multiVideoManager;
    }

    public static void onPauseAll() {
        if (sMap.size() > 0) {
            for (Map.Entry<String, MultiVideoManager> header : sMap.entrySet()) {
                header.getValue().onPause(header.getKey());
            }
        }
    }

    public static void onResumeAll() {
        if (sMap.size() > 0) {
            for (Map.Entry<String, MultiVideoManager> header : sMap.entrySet()) {
                header.getValue().onResume(header.getKey());
            }
        }
    }

    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作
     */
    public static void onResumeAll(boolean seek) {
        if (sMap.size() > 0) {
            for (Map.Entry<String, MultiVideoManager> header : sMap.entrySet()) {
                header.getValue().onResume(header.getKey(), seek);
            }
        }
    }

    public static void clearAllVideo() {
        if (sMap.size() > 0) {
            for (Map.Entry<String, MultiVideoManager> header : sMap.entrySet()) {
                MultiVideoManager.releaseAllVideos(header.getKey());
            }
        }
        sMap.clear();
    }

    public static void removeManager(String key) {
        sMap.remove(key);
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
