package tv.danmaku.ijk.media.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.io.File;

import tv.danmaku.ijk.media.listener.IVideoShotListener;
import tv.danmaku.ijk.media.listener.IVideoShotSaveListener;
import tv.danmaku.ijk.media.render.glrender.VideoGLViewBaseRender;
import tv.danmaku.ijk.media.render.view.IRenderView;
import tv.danmaku.ijk.media.render.view.SurfaceView;
import tv.danmaku.ijk.media.render.view.TextureView;
import tv.danmaku.ijk.media.render.view.VideoGLView;
import tv.danmaku.ijk.media.render.view.listener.ISurfaceListener;
import tv.danmaku.ijk.media.utils.MeasureHelper;
import tv.danmaku.ijk.media.utils.VideoType;

/**
 * render绘制中间控件
 */
public class RenderView {

    private IRenderView mShowView;

    /*************************RenderView function start *************************/
    public void requestLayout() {
        if (mShowView != null) mShowView.getRenderView().requestLayout();
    }

    public float getRotation() {
        return mShowView.getRenderView().getRotation();
    }

    public void setRotation(float rotation) {
        if (mShowView != null) mShowView.getRenderView().setRotation(rotation);
    }

    public void invalidate() {
        if (mShowView != null) mShowView.getRenderView().invalidate();
    }

    public int getWidth() {
        return (mShowView != null) ? mShowView.getRenderView().getWidth() : 0;
    }

    public int getHeight() {
        return (mShowView != null) ? mShowView.getRenderView().getHeight() : 0;
    }

    public View getShowView() {
        return mShowView != null ? mShowView.getRenderView() : null;
    }

    public ViewGroup.LayoutParams getLayoutParams() {
        return mShowView.getRenderView().getLayoutParams();
    }

    public void setLayoutParams(ViewGroup.LayoutParams layoutParams) {
        if (mShowView != null) mShowView.getRenderView().setLayoutParams(layoutParams);
    }

    /**
     * 添加播放的view
     */
    public void addView(final Context context, final ViewGroup textureViewContainer, final int rotate,
                        final ISurfaceListener surfaceListener,
                        final MeasureHelper.MeasureFormVideoParamsListener videoParamsListener,
                        final VideoGLView.ShaderInterface effect, final float[] transform,
                        final VideoGLViewBaseRender customRender, int mode) {
        if (VideoType.getRenderType() == VideoType.SUFRACE) {
            mShowView = SurfaceView.addSurfaceView(context, textureViewContainer, rotate, surfaceListener, videoParamsListener);
        } else if (VideoType.getRenderType() == VideoType.GLSURFACE) {
            mShowView = VideoGLView.addGLView(context, textureViewContainer, rotate, surfaceListener, videoParamsListener, effect, transform, customRender, mode);
        } else {
            mShowView = TextureView.addTextureView(context, textureViewContainer, rotate, surfaceListener, videoParamsListener);
        }
    }

    /*************************RenderView function end *************************/

    /*************************ShowView function start *************************/

    /**
     * 主要针对TextureView，设置旋转
     */
    public void setTransform(Matrix transform) {
        if (mShowView != null) mShowView.setRenderTransform(transform);
    }

    /**
     * 暂停时初始化位图
     */
    public Bitmap initCover() {
        return mShowView != null ? mShowView.initCover() : null;
    }

    /**
     * 暂停时初始化位图
     */
    public Bitmap initCoverHigh() {
        return mShowView != null ? mShowView.initCoverHigh() : null;
    }

    /**
     * 获取截图
     */
    public void taskShotPic(IVideoShotListener videoShotListener) {
        this.taskShotPic(videoShotListener, false);
    }


    /**
     * 获取截图
     *
     * @param shotHigh 是否需要高清的
     */
    public void taskShotPic(IVideoShotListener videoShotListener, boolean shotHigh) {
        if (mShowView != null) mShowView.taskShotPic(videoShotListener, shotHigh);
    }

    /**
     * 保存截图
     */
    public void saveFrame(final File file, IVideoShotSaveListener videoShotSaveListener) {
        saveFrame(file, false, videoShotSaveListener);
    }

    /**
     * 保存截图
     *
     * @param high 是否需要高清的
     */
    public void saveFrame(final File file, final boolean high, final IVideoShotSaveListener videoShotSaveListener) {
        if (mShowView != null) mShowView.saveFrame(file, high, videoShotSaveListener);
    }

    /**
     * 主要针对GL
     */
    public void onResume() {
        if (mShowView != null) mShowView.onRenderResume();
    }

    /**
     * 主要针对GL
     */
    public void onPause() {
        if (mShowView != null) mShowView.onRenderPause();
    }

    /**
     * 主要针对GL
     */
    public void releaseAll() {
        if (mShowView != null) mShowView.releaseRenderAll();
    }

    /**
     * 主要针对GL
     */
    public void setGLRenderMode(int mode) {
        if (mShowView != null) mShowView.setRenderMode(mode);
    }

    /**
     * 自定义GL的渲染render
     */
    public void setGLRenderer(VideoGLViewBaseRender renderer) {
        if (mShowView != null) mShowView.setGLRenderer(renderer);
    }

    /**
     * GL模式下的画面matrix效果
     *
     * @param matrixGL 16位长度
     */
    public void setMatrixGL(float[] matrixGL) {
        if (mShowView != null) mShowView.setGLMVPMatrix(matrixGL);
    }

    /**
     * 设置滤镜效果
     */
    public void setEffectFilter(VideoGLView.ShaderInterface effectFilter) {
        if (mShowView != null) mShowView.setGLEffectFilter(effectFilter);
    }

    /*************************ShowView function end *************************/


    /*************************common function *************************/

    public static void addToParent(ViewGroup textureViewContainer, View render) {
        int params = getTextureParams();
        if (textureViewContainer instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(params, params);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            textureViewContainer.addView(render, layoutParams);
        } else if (textureViewContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(params, params);
            layoutParams.gravity = Gravity.CENTER;
            textureViewContainer.addView(render, layoutParams);
        }
    }

    /**
     * 获取布局参数
     */
    public static int getTextureParams() {
        boolean typeChanged = (VideoType.getShowType() != VideoType.SCREEN_TYPE_DEFAULT);
        return (typeChanged) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
    }

}
