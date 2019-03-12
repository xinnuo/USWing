package com.meida.exo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.meida.uswing.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.model.VideoInfoModel;
import tv.danmaku.ijk.media.utils.Debuger;
import tv.danmaku.ijk.media.utils.NetworkUtils;
import tv.danmaku.ijk.media.video.StandardVideoPlayer;
import tv.danmaku.ijk.media.video.base.BaseVideoPlayer;
import tv.danmaku.ijk.media.video.base.VideoPlayer;
import tv.danmaku.ijk.media.video.base.VideoViewBridge;

/**
 * 自定义View支持exo的list数据，实现无缝切换效果
 *
 * 这是一种思路，通过自定义后Exo2MediaPlayer内部，通过ConcatenatingMediaSource实现列表播放
 * 诸如此类，还可以实现AdsMediaSource等
 */

public class Exo2PlayerView extends StandardVideoPlayer {

    protected List<VideoInfoModel> mUriList = new ArrayList<>();
    protected int mPlayPosition;
    protected boolean mExoCache = false;

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public Exo2PlayerView(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public Exo2PlayerView(Context context) {
        super(context);
    }

    public Exo2PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param position      需要播放的位置
     * @return
     */
    public boolean setUp(List<VideoInfoModel> url, int position) {
        return setUp(url, position, null, new HashMap<String, String>());
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @return
     */
    public boolean setUp(List<VideoInfoModel> url, int position, File cachePath) {
        return setUp(url, position, cachePath, new HashMap<String, String>());
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @return
     */
    public boolean setUp(List<VideoInfoModel> url,  int position, File cachePath, Map<String, String> mapHeadData) {
        return setUp(url, position, cachePath, mapHeadData, true);
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @param changeState   切换的时候释放surface
     * @return
     */
    protected boolean setUp(List<VideoInfoModel> url, int position, File cachePath, Map<String, String> mapHeadData, boolean changeState) {
        mUriList = url;
        mPlayPosition = position;
        mMapHeadData = mapHeadData;
        VideoInfoModel videoModel = url.get(position);

        //不支持边播边缓存
        boolean set = setUp(videoModel.getUrl(), false, cachePath, videoModel.getTitle(), changeState);
        if (!TextUtils.isEmpty(videoModel.getTitle())) {
            mTitleTextView.setText(videoModel.getTitle());
        }
        return set;
    }

    @Override
    protected void cloneParams(BaseVideoPlayer from, BaseVideoPlayer to) {
        super.cloneParams(from, to);
        Exo2PlayerView sf = (Exo2PlayerView) from;
        Exo2PlayerView st = (Exo2PlayerView) to;
        st.mPlayPosition = sf.mPlayPosition;
        st.mUriList = sf.mUriList;
        st.mExoCache = sf.mExoCache;
    }

    @Override
    public BaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        BaseVideoPlayer baseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        if (baseVideoPlayer != null) {
            Exo2PlayerView exo2PlayerView = (Exo2PlayerView) baseVideoPlayer;
            VideoInfoModel videoModel = mUriList.get(mPlayPosition);
            if (!TextUtils.isEmpty(videoModel.getTitle())) {
                exo2PlayerView.mTitleTextView.setText(videoModel.getTitle());
            }
        }
        return baseVideoPlayer;
    }

    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, VideoPlayer videoPlayer) {
        if (videoPlayer != null) {
            VideoInfoModel videoModel = mUriList.get(mPlayPosition);
            if (!TextUtils.isEmpty(videoModel.getTitle())) {
                mTitleTextView.setText(videoModel.getTitle());
            }
        }
        super.resolveNormalVideoShow(oldF, vp, videoPlayer);
    }

    @Override
    protected void startPrepare() {
        if (getVideoManager().listener() != null) {
            getVideoManager().listener().onCompletion();
        }
        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onStartPrepared");
            mVideoAllCallBack.onStartPrepared(mOriginUrl, mTitle, this);
        }
        getVideoManager().setListener(this);
        getVideoManager().setPlayTag(mPlayTag);
        getVideoManager().setPlayPosition(mPlayPosition);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        ((Activity) getActivityContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBackUpPlayingBufferState = -1;

        //prepare通过list初始化
        List<String> urls = new ArrayList<>();
        for (VideoInfoModel videoModel : mUriList) {
            urls.add(videoModel.getUrl());
        }

        if (urls.size() == 0) {
            Debuger.printfError("********************** urls isEmpty . Do you know why ? **********************");
        }

        ((ExoVideoManager)getVideoManager()).prepare(urls, (mMapHeadData == null) ? new HashMap<String, String>() : mMapHeadData, mLooping, mSpeed, mExoCache, mCachePath);

        setStateAndUi(CURRENT_STATE_PREPAREING);
    }

    /**
     * 显示wifi确定框，如需要自定义继承重写即可
     */
    @Override
    protected void showWifiDialog() {
        if (!NetworkUtils.isAvailable(mContext)) {
            startPlayLogic();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivityContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), (dialog, which) -> {
            dialog.dismiss();
            startPlayLogic();
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public void setExoCache(boolean exoCache) {
        this.mExoCache = exoCache;
    }

    /**********以下重载VideoPlayer的VideoViewBridge相关实现***********/

    @Override
    public VideoViewBridge getVideoManager() {
        ExoVideoManager.instance().initContext(getContext().getApplicationContext());
        return ExoVideoManager.instance();
    }

    @Override
    protected boolean backFromFull(Context context) {
        return ExoVideoManager.backFromWindowFull(context);
    }

    @Override
    protected void releaseVideos() {
        ExoVideoManager.releaseAllVideos();
    }

    @Override
    protected int getFullId() {
        return ExoVideoManager.FULLSCREEN_ID;
    }

    @Override
    protected int getSmallId() {
        return ExoVideoManager.SMALL_ID;
    }

}
