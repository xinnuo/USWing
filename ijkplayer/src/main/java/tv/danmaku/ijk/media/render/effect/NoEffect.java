package tv.danmaku.ijk.media.render.effect;

import android.opengl.GLSurfaceView;

import tv.danmaku.ijk.media.render.view.VideoGLView;

/**
 * Displays the normal video without any effect.
 */
public class NoEffect implements VideoGLView.ShaderInterface {

    /**
     * Initialize
     */
    public NoEffect() { }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "varying vec2 vTextureCoord;\n"
                + "uniform samplerExternalOES sTexture;\n" + "void main() {\n"
                + "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n"
                + "}\n";

        return shader;

    }

}