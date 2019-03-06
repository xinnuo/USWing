package tv.danmaku.ijk.media.render.effect;

import android.opengl.GLSurfaceView;

import tv.danmaku.ijk.media.render.view.VideoGLView;

/**
 * 简单模糊
 */
public class SampleBlurEffect implements VideoGLView.ShaderInterface {

    private float blur = 1f;

    public SampleBlurEffect() {
        super();
    }

    public SampleBlurEffect(float blur) {
        this.blur = blur;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {

        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "varying vec2 vTextureCoord;\n" +
                "const float blurSize = " + blur + "/800.0;\n" +
                "const float weightSum = 70.0 + 2.0 * (1.0 + 8.0 + 28.0 + 56.0);\n" +
                "\n" +
                "void main(void)\n" +
                "{\n" +
                "   vec4 sum = vec4(0.0);\n" +
                "\n" +
                "   sum += texture2D(sTexture, vec2(vTextureCoord.x - 4.0*blurSize, vTextureCoord.y)) * 1.0 / weightSum;\n" +
                "   sum += texture2D(sTexture, vec2(vTextureCoord.x - 3.0*blurSize, vTextureCoord.y)) * 8.0 / weightSum;\n" +
                "   sum += texture2D(sTexture, vec2(vTextureCoord.x - 2.0*blurSize, vTextureCoord.y)) * 28.0 / weightSum;\n" +
                "   sum += texture2D(sTexture, vec2(vTextureCoord.x - blurSize, vTextureCoord.y)) * 56.0 / weightSum;\n" +
                "   sum += texture2D(sTexture, vec2(vTextureCoord.x, vTextureCoord.y)) * 70.0 / weightSum;\n" +
                "   sum += texture2D(sTexture, vec2(vTextureCoord.x + blurSize, vTextureCoord.y)) * 56.0 / weightSum;\n" +
                "   sum += texture2D(sTexture, vec2(vTextureCoord.x + 2.0*blurSize, vTextureCoord.y)) * 28.0 / weightSum;\n" +
                "   sum += texture2D(sTexture, vec2(vTextureCoord.x + 3.0*blurSize, vTextureCoord.y)) * 8.0 / weightSum;\n" +
                "   sum += texture2D(sTexture, vec2(vTextureCoord.x + 4.0*blurSize, vTextureCoord.y)) * 1.0 / weightSum;\n" +
                "\n" +
                "   gl_FragColor = sum;\n" +
                "}";
    }

}
