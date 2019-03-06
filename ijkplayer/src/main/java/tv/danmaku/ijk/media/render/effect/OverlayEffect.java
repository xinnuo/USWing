package tv.danmaku.ijk.media.render.effect;

import android.opengl.GLSurfaceView;

import tv.danmaku.ijk.media.render.view.VideoGLView;

/**
 * 重叠
 */
public class OverlayEffect implements VideoGLView.ShaderInterface {

    private float fstep = 0.0015f;

    public OverlayEffect() {
        super();
    }

    public OverlayEffect(float fstep) {
        this.fstep = fstep;
    }

    @Override
    public String getShader(GLSurfaceView mGlSurfaceView) {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "             uniform samplerExternalOES sTexture; \n" +
                "             varying vec2 vTextureCoord; \n" +
                "  \n" +
                "             void main() \n" +
                "             { \n" +
                "              vec4 sample0,sample1,sample2,sample3; \n" +
                "              float fstep=" + fstep + "; \n" +
                "              sample0=texture2D(sTexture,vec2(vTextureCoord.x-fstep,vTextureCoord.y-fstep)); \n" +
                "              sample1=texture2D(sTexture,vec2(vTextureCoord.x+fstep,vTextureCoord.y-fstep)); \n" +
                "              sample2=texture2D(sTexture,vec2(vTextureCoord.x+fstep,vTextureCoord.y+fstep)); \n" +
                "              sample3=texture2D(sTexture,vec2(vTextureCoord.x-fstep,vTextureCoord.y+fstep)); \n" +
                "              vec4 color=(sample0+sample1+sample2+sample3) / 4.0; \n" +
                "              gl_FragColor=color; \n" +
                "             } ";
    }

}
