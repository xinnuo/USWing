package tv.danmaku.ijk.media.render.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import tv.danmaku.ijk.media.listener.IVideoShotListener;
import tv.danmaku.ijk.media.listener.IVideoShotSaveListener;
import tv.danmaku.ijk.media.render.RenderView;
import tv.danmaku.ijk.media.render.glrender.VideoGLViewBaseRender;
import tv.danmaku.ijk.media.render.view.listener.ISurfaceListener;
import tv.danmaku.ijk.media.utils.Debuger;
import tv.danmaku.ijk.media.utils.FileUtils;
import tv.danmaku.ijk.media.utils.MeasureHelper;

/**
 * 用于显示video的，做了横屏与竖屏的匹配，还有需要rotation需求的
 */
public class TextureView extends android.view.TextureView implements android.view.TextureView.SurfaceTextureListener, IRenderView, MeasureHelper.MeasureFormVideoParamsListener {

    private ISurfaceListener mISurfaceListener;

    private MeasureHelper.MeasureFormVideoParamsListener mVideoParamsListener;

    private MeasureHelper measureHelper;

    private Surface mSurface;

    public TextureView(Context context) {
        super(context);
        init();
    }

    public TextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        measureHelper = new MeasureHelper(this, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureHelper.prepareMeasure(widthMeasureSpec, heightMeasureSpec, (int) getRotation());
        setMeasuredDimension(measureHelper.getMeasuredWidth(), measureHelper.getMeasuredHeight());
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        if (mISurfaceListener != null) {
            mISurfaceListener.onSurfaceAvailable(mSurface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mISurfaceListener != null) {
            mISurfaceListener.onSurfaceSizeChanged(mSurface, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        //清空释放
        if (mISurfaceListener != null) {
            mISurfaceListener.onSurfaceDestroyed(mSurface);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //如果播放的是暂停全屏了
        if (mISurfaceListener != null) {
            mISurfaceListener.onSurfaceUpdated(mSurface);
        }
    }

    @Override
    public ISurfaceListener getISurfaceListener() {
        return mISurfaceListener;
    }

    @Override
    public void setISurfaceListener(ISurfaceListener surfaceListener) {
        setSurfaceTextureListener(this);
        mISurfaceListener = surfaceListener;
    }

    @Override
    public int getSizeH() {
        return getHeight();
    }

    @Override
    public int getSizeW() {
        return getWidth();
    }

    /**
     * 暂停时初始化位图
     */
    @Override
    public Bitmap initCover() {
        Bitmap bitmap = Bitmap.createBitmap(
                getSizeW(), getSizeH(), Bitmap.Config.RGB_565);
        return getBitmap(bitmap);

    }

    /**
     * 暂停时初始化位图
     */
    @Override
    public Bitmap initCoverHigh() {
        Bitmap bitmap = Bitmap.createBitmap(
                getSizeW(), getSizeH(), Bitmap.Config.ARGB_8888);
        return getBitmap(bitmap);

    }

    /**
     * 获取截图
     *
     * @param shotHigh 是否需要高清的
     */
    @Override
    public void taskShotPic(IVideoShotListener videoShotListener, boolean shotHigh) {
        videoShotListener.getBitmap(shotHigh ? initCoverHigh() : initCover());
    }

    /**
     * 保存截图
     *
     * @param high 是否需要高清的
     */
    @Override
    public void saveFrame(final File file, final boolean high, final IVideoShotSaveListener videoShotSaveListener) {
        IVideoShotListener videoShotListener = new IVideoShotListener() {
            @Override
            public void getBitmap(Bitmap bitmap) {
                if (bitmap == null) {
                    videoShotSaveListener.result(false, file);
                } else {
                    FileUtils.saveBitmap(bitmap, file);
                    videoShotSaveListener.result(true, file);
                }
            }
        };

        videoShotListener.getBitmap(high ? initCoverHigh() : initCover());
    }


    @Override
    public View getRenderView() {
        return this;
    }

    @Override
    public void onRenderResume() {
        Debuger.printfLog(getClass().getSimpleName() + " not support onRenderResume now");
    }

    @Override
    public void onRenderPause() {
        Debuger.printfLog(getClass().getSimpleName() + " not support onRenderPause now");
    }

    @Override
    public void releaseRenderAll() {
        Debuger.printfLog(getClass().getSimpleName() + " not support releaseRenderAll now");
    }

    @Override
    public void setRenderMode(int mode) {
        Debuger.printfLog(getClass().getSimpleName() + " not support setRenderMode now");
    }

    @Override
    public void setRenderTransform(Matrix transform) {
        setTransform(transform);
    }

    @Override
    public void setGLRenderer(VideoGLViewBaseRender renderer) {
        Debuger.printfLog(getClass().getSimpleName() + " not support setGLRenderer now");
    }

    @Override
    public void setGLMVPMatrix(float[] MVPMatrix) {
        Debuger.printfLog(getClass().getSimpleName() + " not support setGLMVPMatrix now");
    }

    /**
     * 设置滤镜效果
     */
    @Override
    public void setGLEffectFilter(VideoGLView.ShaderInterface effectFilter) {
        Debuger.printfLog(getClass().getSimpleName() + " not support setGLEffectFilter now");
    }


    @Override
    public void setVideoParamsListener(MeasureHelper.MeasureFormVideoParamsListener listener) {
        mVideoParamsListener = listener;
    }

    @Override
    public int getCurrentVideoWidth() {
        if (mVideoParamsListener != null) {
            return mVideoParamsListener.getCurrentVideoWidth();
        }
        return 0;
    }

    @Override
    public int getCurrentVideoHeight() {
        if (mVideoParamsListener != null) {
            return mVideoParamsListener.getCurrentVideoHeight();
        }
        return 0;
    }

    @Override
    public int getVideoSarNum() {
        if (mVideoParamsListener != null) {
            return mVideoParamsListener.getVideoSarNum();
        }
        return 0;
    }

    @Override
    public int getVideoSarDen() {
        if (mVideoParamsListener != null) {
            return mVideoParamsListener.getVideoSarDen();
        }
        return 0;
    }


    /**
     * 添加播放的view
     */
    public static TextureView addTextureView(Context context, ViewGroup textureViewContainer, int rotate,
                                             final ISurfaceListener surfaceListener,
                                             final MeasureHelper.MeasureFormVideoParamsListener videoParamsListener) {

        if (textureViewContainer.getChildCount() > 0) textureViewContainer.removeAllViews();

        TextureView textureView = new TextureView(context);
        textureView.setISurfaceListener(surfaceListener);
        textureView.setVideoParamsListener(videoParamsListener);
        textureView.setRotation(rotate);
        RenderView.addToParent(textureViewContainer, textureView);

        return textureView;
    }
}