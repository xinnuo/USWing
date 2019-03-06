package tv.danmaku.ijk.media.listener;

import java.io.File;

/**
 * Gif图创建的监听
 */
public interface IVideoGifSaveListener {

    void process(int curPosition, int total);

    void result(boolean success, File file);

}
