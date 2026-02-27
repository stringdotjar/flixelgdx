package me.stringdotjar.flixelgdx.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Utility class for simplifying asset paths and libGDX {@link FileHandle}s. */
public final class FlixelPathsUtil {

  private static final Map<String, String> audioPathCache = new ConcurrentHashMap<>();

  public static FileHandle asset(String path) {
    return Gdx.files.internal(path);
  }

  public static FileHandle shared(String path) {
    return asset(String.format("shared/%s", path));
  }

  public static FileHandle fontAsset(String path) {
    return asset(String.format("fonts/%s.ttf", path));
  }

  public static FileHandle xmlAsset(String path) {
    return asset(String.format("%s.xml", path));
  }

  public static FileHandle sharedImageAsset(String path) {
    return shared(String.format("images/%s.png", path));
  }

  public static FileHandle external(String path) {
    return Gdx.files.external(path);
  }

  /**
   * Resolves an internal asset path to an absolute filesystem path that MiniAudio's native engine
   * can open directly.
   *
   * <p>When running from the IDE the working directory is the {@code assets/} folder, so the raw
   * relative path works as-is. When running from a packaged JAR the assets are embedded as
   * classpath resources and MiniAudio cannot open them by name. In that case the resource is
   * extracted to a temp file on first call, and the temp file's absolute path is returned. Results
   * are cached so repeated calls for the same path do not produce extra temp files.
   *
   * @param path The internal asset path, e.g. {@code "shared/sounds/foo.ogg"}.
   * @return An absolute filesystem path that MiniAudio can open.
   */
  public static String resolveAudioPath(String path) {
    return audioPathCache.computeIfAbsent(path, FlixelPathsUtil::extractAudioPath);
  }

  private static String extractAudioPath(String path) {
    FileHandle handle = asset(path);
    if (handle.file().exists()) {
      return handle.file().getAbsolutePath();
    }
    // Asset is inside a JAR, copy it out to a temp file so MiniAudio can open it.
    String ext = path.contains(".") ? path.substring(path.lastIndexOf('.')) : "";
    try {
      File temp = File.createTempFile("flixelaudio_", ext);
      temp.deleteOnExit();
      handle.copyTo(new FileHandle(temp));
      return temp.getAbsolutePath();
    } catch (IOException e) {
      throw new RuntimeException("Failed to extract audio asset from JAR: " + path, e);
    }
  }

  private FlixelPathsUtil() {}
}
