package tv.danmaku.ijk.media.render.glrender;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import tv.danmaku.ijk.media.listener.IVideoShotListener;
import tv.danmaku.ijk.media.render.effect.NoEffect;
import tv.danmaku.ijk.media.render.view.VideoGLView;

/**
 * 在videffects的基础上调整的
 * <p>
 */
@SuppressLint("ViewConstructor")
public class VideoGLViewSimpleRender extends VideoGLViewBaseRender {

    private static final int FLOAT_SIZE_BYTES = 4;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;

    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    protected static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            -1.0f, 0.0f, 1.0f,
            0.0f, -1.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, 1.0f,};

    private final String mVertexShader = "uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uSTMatrix;\n"
            + "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  gl_Position = uMVPMatrix * aPosition;\n"
            + "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n"
            + "}\n";

    private int mProgram;

    private int mTextureID[] = new int[2];

    private int muMVPMatrixHandle;

    private int muSTMatrixHandle;

    private int maPositionHandle;

    private int maTextureHandle;

    private boolean mUpdateSurface = false;

    private boolean mTakeShotPic = false;

    private FloatBuffer mTriangleVertices;

    private SurfaceTexture mSurface;

    private IVideoShotListener mVideoShotListener;

    private VideoGLView.ShaderInterface mEffect = new NoEffect();

    public VideoGLViewSimpleRender() {
        mTriangleVertices = ByteBuffer
                .allocateDirect(mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

        Matrix.setIdentityM(mSTMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        synchronized (this) {
            if (mUpdateSurface) {
                mSurface.updateTexImage();
                mSurface.getTransformMatrix(mSTMatrix);
                mUpdateSurface = false;
            }
        }
        initDrawFrame();

        bindDrawFrameTexture();

        initPointerAndDraw();

        takeBitmap(glUnused);

        GLES20.glFinish();
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

        mProgram = createProgram(getVertexShader(), getFragmentShader());
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES20
                .glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram,
                "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram,
                "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram,
                "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for uSTMatrix");
        }

        GLES20.glGenTextures(2, mTextureID, 0);

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID[0]);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        mSurface = new SurfaceTexture(mTextureID[0]);
        mSurface.setOnFrameAvailableListener(this);

        Surface surface = new Surface(mSurface);

        sendSurfaceForPlayer(surface);
    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        mUpdateSurface = true;
    }

    @Override
    public void releaseAll() {

    }

    /**
     * 设置滤镜效果
     *
     * @param shaderEffect
     */
    @Override
    public void setEffect(VideoGLView.ShaderInterface shaderEffect) {
        if (shaderEffect != null) {
            mEffect = shaderEffect;
        }
        mChangeProgram = true;
        mChangeProgramSupportError = true;
    }

    @Override
    public VideoGLView.ShaderInterface getEffect() {
        return mEffect;
    }

    protected void initDrawFrame() {
        if (mChangeProgram) {
            mProgram = createProgram(getVertexShader(), getFragmentShader());
            mChangeProgram = false;
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");
    }


    protected void bindDrawFrameTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID[0]);
    }


    protected void takeBitmap(GL10 glUnused) {
        if (mTakeShotPic) {
            mTakeShotPic = false;
            if (mVideoShotListener != null) {
                Bitmap bitmap = createBitmapFromGLSurface(0, 0, mSurfaceView.getWidth(), mSurfaceView.getHeight(), glUnused);
                mVideoShotListener.getBitmap(bitmap);
            }
        }
    }


    protected void initPointerAndDraw() {
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix,
                0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

    }

    public int getProgram() {
        return mProgram;
    }

    public int getMuMVPMatrixHandle() {
        return muMVPMatrixHandle;
    }

    public int getMuSTMatrixHandle() {
        return muSTMatrixHandle;
    }

    public int getMaPositionHandle() {
        return maPositionHandle;
    }

    public int getMaTextureHandle() {
        return maTextureHandle;
    }

    public float[] getSTMatrix() {
        return mSTMatrix;
    }

    public int[] getTextureID() {
        return mTextureID;
    }

    protected String getVertexShader() {
        return mVertexShader;
    }

    protected String getFragmentShader() {
        return mEffect.getShader(mSurfaceView);
    }

    /**
     * 打开截图
     */
    public void takeShotPic() {
        mTakeShotPic = true;
    }

    /**
     * 截图监听
     */
    public void setVideoShotListener(IVideoShotListener listener, boolean high) {
        this.mVideoShotListener = listener;
        this.mHighShot = high;
    }
}


