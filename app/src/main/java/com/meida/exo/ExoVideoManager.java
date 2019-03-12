package com.meida.exo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.meida.uswing.R;

import java.io.File;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.VideoBaseManager;
import tv.danmaku.ijk.media.player.IPlayerManager;
import tv.danmaku.ijk.media.utils.CommonUtil;
import tv.danmaku.ijk.media.video.base.VideoPlayer;

import static tv.danmaku.ijk.media.utils.CommonUtil.hideNavKey;

/**
 * 自定义管理器，连接自定义exo view 和 exo player，实现无缝切换效果
 */
public class ExoVideoManager extends VideoBaseManager {

    public static final int SMALL_ID = R.id.small_id;

    public static final int FULLSCREEN_ID = R.id.full_id;

    public static String TAG = "ExoVideoManager";

    @SuppressLint("StaticFieldLeak")
    private static ExoVideoManager videoManager;

    private ExoVideoManager() {
        init();
    }

    /**
     * 单例管理器
     */
    public static synchronized ExoVideoManager instance() {
        if (videoManager == null) {
            videoManager = new ExoVideoManager();
        }
        return videoManager;
    }

    @Override
    protected IPlayerManager getPlayManager() {
        return new ExoPlayerManager();
    }

    public void prepare(List<String> urls, Map<String, String> mapHeadData, boolean loop, float speed, boolean cache, File cachePath) {
        if (urls.size() == 0) return;
        Message msg = new Message();
        msg.what = HANDLER_PREPARE;
        msg.obj = new ExoModel(urls, mapHeadData, loop, speed, cache, cachePath);
        sendMessage(msg);
        if (needTimeOutOther) {
            startTimeOutBuffer();
        }
    }


    /**
     * 上一集
     */
    public void previous() {
        if (playerManager == null) {
            return;
        }
        ((ExoPlayerManager)playerManager).previous();
    }

    /**
     * 下一集
     */
    public void next() {
        if (playerManager == null) {
            return;
        }
        ((ExoPlayerManager)playerManager).next();
    }

    /**
     * 退出全屏，主要用于返回键
     *
     * @return 返回是否全屏
     */
    @SuppressWarnings("ResourceType")
    public static boolean backFromWindowFull(Context context) {
        boolean backFrom = false;
        ViewGroup vp = (ViewGroup) (CommonUtil.scanForActivity(context)).findViewById(Window.ID_ANDROID_CONTENT);
        View oldF = vp.findViewById(FULLSCREEN_ID);
        if (oldF != null) {
            backFrom = true;
            hideNavKey(context);
            if (ExoVideoManager.instance().lastListener() != null) {
                ExoVideoManager.instance().lastListener().onBackFullscreen();
            }
        }
        return backFrom;
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    public static void releaseAllVideos() {
        if (ExoVideoManager.instance().listener() != null) {
            ExoVideoManager.instance().listener().onCompletion();
        }
        ExoVideoManager.instance().releaseMediaPlayer();
    }


    /**
     * 暂停播放
     */
    public static void onPause() {
        if (ExoVideoManager.instance().listener() != null) {
            ExoVideoManager.instance().listener().onVideoPause();
        }
    }

    /**
     * 恢复播放
     */
    public static void onResume() {
        if (ExoVideoManager.instance().listener() != null) {
            ExoVideoManager.instance().listener().onVideoResume();
        }
    }


    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    public static void onResume(boolean seek) {
        if (ExoVideoManager.instance().listener() != null) {
            ExoVideoManager.instance().listener().onVideoResume(seek);
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
