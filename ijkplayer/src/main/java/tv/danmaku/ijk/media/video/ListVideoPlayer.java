package tv.danmaku.ijk.media.video;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.codeest.enviews.ENDownloadView;
import tv.danmaku.ijk.media.model.VideoInfoModel;
import tv.danmaku.ijk.media.video.base.BaseVideoPlayer;
import tv.danmaku.ijk.media.video.base.VideoPlayer;

/**
 * 列表播放支持
 */
public class ListVideoPlayer extends StandardVideoPlayer {

    protected List<VideoInfoModel> mUriList = new ArrayList<>();
    protected int mPlayPosition;

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public ListVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public ListVideoPlayer(Context context) {
        super(context);
    }

    public ListVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param position      需要播放的位置
     * @param cacheWithPlay 是否边播边缓存
     * @return
     */
    public boolean setUp(List<VideoInfoModel> url, boolean cacheWithPlay, int position) {
        return setUp(url, cacheWithPlay, position, null, new HashMap<String, String>());
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @return
     */
    public boolean setUp(List<VideoInfoModel> url, boolean cacheWithPlay, int position, File cachePath) {
        return setUp(url, cacheWithPlay, position, cachePath, new HashMap<String, String>());
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @return
     */
    public boolean setUp(List<VideoInfoModel> url, boolean cacheWithPlay, int position, File cachePath, Map<String, String> mapHeadData) {
        return setUp(url, cacheWithPlay, position, cachePath, mapHeadData, true);
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @param changeState   切换的时候释放surface
     * @return
     */
    protected boolean setUp(List<VideoInfoModel> url, boolean cacheWithPlay, int position, File cachePath, Map<String, String> mapHeadData, boolean changeState) {
        mUriList = url;
        mPlayPosition = position;
        mMapHeadData = mapHeadData;
        VideoInfoModel videoModel = url.get(position);
        boolean set = setUp(videoModel.getUrl(), cacheWithPlay, cachePath, videoModel.getTitle(), changeState);
        if (!TextUtils.isEmpty(videoModel.getTitle())) {
            mTitleTextView.setText(videoModel.getTitle());
        }
        return set;
    }


    @Override
    protected void cloneParams(BaseVideoPlayer from, BaseVideoPlayer to) {
        super.cloneParams(from, to);
        ListVideoPlayer sf = (ListVideoPlayer) from;
        ListVideoPlayer st = (ListVideoPlayer) to;
        st.mPlayPosition = sf.mPlayPosition;
        st.mUriList = sf.mUriList;
    }

    @Override
    public BaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        BaseVideoPlayer baseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        if (baseVideoPlayer != null) {
            ListVideoPlayer listVideoPlayer = (ListVideoPlayer) baseVideoPlayer;
            VideoInfoModel videoModel = mUriList.get(mPlayPosition);
            if (!TextUtils.isEmpty(videoModel.getTitle())) {
                listVideoPlayer.mTitleTextView.setText(videoModel.getTitle());
            }
        }
        return baseVideoPlayer;
    }

    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, VideoPlayer videoPlayer) {
        if (videoPlayer != null) {
            ListVideoPlayer listVideoPlayer = (ListVideoPlayer) videoPlayer;
            VideoInfoModel videoModel = mUriList.get(mPlayPosition);
            if (!TextUtils.isEmpty(videoModel.getTitle())) {
                mTitleTextView.setText(videoModel.getTitle());
            }
        }
        super.resolveNormalVideoShow(oldF, vp, videoPlayer);
    }

    @Override
    public void onCompletion() {
        releaseNetWorkState();
        if (mPlayPosition < (mUriList.size())) {
            return;
        }
        super.onCompletion();
    }

    @Override
    public void onAutoCompletion() {
        if (playNext()) {
            return;
        }
        super.onAutoCompletion();
    }


    /**
     * 开始状态视频播放，prepare时不执行  addTextureView();
     */
    @Override
    protected void prepareVideo() {
        super.prepareVideo();
        if (mHadPlay && mPlayPosition < (mUriList.size())) {
            setViewShowState(mLoadingProgressBar, VISIBLE);
            if (mLoadingProgressBar instanceof ENDownloadView) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
    }

    @Override
    public void onPrepared() {
        super.onPrepared();
    }

    @Override
    protected void changeUiToNormal() {
        super.changeUiToNormal();
        if (mHadPlay && mPlayPosition < (mUriList.size())) {
            setViewShowState(mThumbImageViewLayout, GONE);
            setViewShowState(mTopContainer, INVISIBLE);
            setViewShowState(mBottomContainer, INVISIBLE);
            setViewShowState(mStartButton, GONE);
            setViewShowState(mLoadingProgressBar, VISIBLE);
            setViewShowState(mBottomProgressBar, INVISIBLE);
            setViewShowState(mLockScreen, GONE);
            if (mLoadingProgressBar instanceof ENDownloadView) {
                ((ENDownloadView) mLoadingProgressBar).start();
            }
        }
    }

    /**
     * 播放下一集
     *
     * @return true表示还有下一集
     */
    public boolean playNext() {
        if (mPlayPosition < (mUriList.size() - 1)) {
            mPlayPosition += 1;
            VideoInfoModel videoModel = mUriList.get(mPlayPosition);
            mSaveChangeViewTIme = 0;
            setUp(mUriList, mCache, mPlayPosition, null, mMapHeadData, false);
            if (!TextUtils.isEmpty(videoModel.getTitle())) {
                mTitleTextView.setText(videoModel.getTitle());
            }
            startPlayLogic();
            return true;
        }
        return false;
    }

}
