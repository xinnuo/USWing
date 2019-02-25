package com.example.wechatsmallvideoview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public abstract class SurfaceVideoViewDownloadCreator
        implements
        SurfaceVideoView.OnPlayStateListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, View.OnClickListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnInfoListener {

    private String TAG = this.getClass().getSimpleName();

    private SurfaceVideoView surfaceVideoView;
    private LoadingCircleView progressBar;
    private Button statusButton;
    private ImageView surface_video_screenshot;

    private File videoFile = null;
    private boolean mNeedResume;

    protected abstract Activity getActivity();

    protected abstract boolean setAutoPlay();

    protected abstract int getSurfaceWidth();

    protected abstract int getSurfaceHeight(); //px值

    protected abstract void setThumbImage(ImageView thumbImageView);

    protected abstract String getSecondVideoCachePath();

    protected abstract void downloadVideo(String videoPath, String fileDir, String fileName);

    public SurfaceVideoViewDownloadCreator(Activity activity, ViewGroup container, final String videoPath) {
        View view = LayoutInflater
                .from(activity)
                .inflate(R.layout.surface_video_view_layout, container, false);

        container.addView(view);

        surfaceVideoView = view.findViewById(R.id.surface_video_view);
        progressBar = view.findViewById(R.id.surface_video_progress);
        statusButton = view.findViewById(R.id.surface_video_button);
        surface_video_screenshot = view.findViewById(R.id.surface_video_screenshot);
        setThumbImage(surface_video_screenshot);


        int width = getSurfaceWidth();
        if (width != 0) {
            /* 默认就是手机宽度 */
            surfaceVideoView.getLayoutParams().width = width;
        }
        view.findViewById(R.id.surface_video_container).getLayoutParams().height = getSurfaceHeight();
        view.findViewById(R.id.surface_video_container).requestLayout();

        surfaceVideoView.setOnPreparedListener(this);
        surfaceVideoView.setOnPlayStateListener(this);
        surfaceVideoView.setOnErrorListener(this);
        surfaceVideoView.setOnInfoListener(this);
        surfaceVideoView.setOnCompletionListener(this);

        surfaceVideoView.setOnClickListener(this);

        if (setAutoPlay()) {
            prepareStart(videoPath);
        } else {
            statusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* 点击即加载，这里进行本地是否存在判断 */
                    prepareStart(videoPath);
                }
            });
        }
    }

    private void prepareStart(String path) {
        try {
            String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/USWing/Video/";
            File file = new File(rootPath);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new NullPointerException("创建 rootPath 失败，注意 6.0+ 的动态申请权限");
                }
            }

            String[] temp = path.split("/");
            videoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/USWing/Video/" + temp[temp.length - 1]);

            /* 是否存在缓存 */
            if (videoFile.exists()) {
                play(videoFile.getAbsolutePath());
            } else {
                /* 第二缓存目录，应对此种情况，例如，本地上传是一个目录，那么就可能要到这个目录找一下 */
                String secondCacheFilePath = getSecondVideoCachePath();
                if (secondCacheFilePath != null) {
                    play(secondCacheFilePath);
                    return;
                }

                videoFile.createNewFile();
                /* 下载再播放 */
                downloadVideo(path, rootPath, temp[temp.length - 1]);
            }

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    public void onKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {// 跟随系统音量走
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (!getActivity().isFinishing())
                    surfaceVideoView.dispatchKeyEvent(getActivity(), event);
                break;
        }
    }

    public void onDestroy() {
        progressBar = null;
        statusButton = null;
        if (surfaceVideoView != null) {
            surfaceVideoView.release();
            surfaceVideoView = null;
        }
    }

    public void onResume() {
        if (surfaceVideoView != null && mNeedResume) {
            mNeedResume = false;
            if (surfaceVideoView.isRelease())
                surfaceVideoView.reOpen();
            else
                surfaceVideoView.start();
        }
    }

    public void onPause() {
        if (surfaceVideoView != null) {
            if (surfaceVideoView.isPlaying()) {
                mNeedResume = true;
                surfaceVideoView.pause();
            }
        }
    }

    public void play(String path) {
        if (!surfaceVideoView.isPlaying()) {
            progressBar.setVisibility(View.GONE);
            statusButton.setVisibility(View.GONE);
            surfaceVideoView.setVideoPath(path);
        }
    }

    public void downloadBefore() {
        progressBar.setVisibility(View.VISIBLE);
        statusButton.setVisibility(View.GONE);
    }

    public void updateProgress(int progress) {
        progressBar.setProgerss(progress, true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!getActivity().isFinishing())
            surfaceVideoView.reOpen();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "播放失败 onError " + what);
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            /* 音频和视频数据不正确 */
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                Log.d(TAG, "音频和视频数据不正确 ");
                break;
            /* 缓冲开始 */
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (!getActivity().isFinishing()) {
                    surfaceVideoView.pause();
                }
                break;
            /* 缓冲结束 */
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (!getActivity().isFinishing())
                    surfaceVideoView.start();
                break;
            /* 渲染开始 rendering */
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    surfaceVideoView.setBackground(null);
                } else {
                    surfaceVideoView.setBackgroundDrawable(null);
                }
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "播放开始 onPrepared ");
        surfaceVideoView.setVolume(SurfaceVideoView.getSystemVolumn(getActivity()));
        surfaceVideoView.start();
        //progressBar.setVisibility(View.GONE);
        surface_video_screenshot.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        getActivity().finish();
    }

    @Override
    public void onStateChanged(boolean isPlaying) {
        if (!setAutoPlay()) statusButton.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
    }
}
