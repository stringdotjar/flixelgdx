/**********************************************************************************
 * Copyright (c) 2025-2026 stringdotjar
 *
 * This file is part of the FlixelGDX framework, licensed under the MIT License.
 * See the LICENSE file in the repository root for full license information.
 **********************************************************************************/

package me.stringdotjar.flixelgdx.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.loader.MASoundLoader;
import me.stringdotjar.flixelgdx.Flixel;
import me.stringdotjar.flixelgdx.audio.FlixelSoundSource;
import me.stringdotjar.flixelgdx.audio.FlixelSoundSourceLoader;
import me.stringdotjar.flixelgdx.graphics.FlixelGraphic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Asset manager for FlixelGDX.
 *
 * <p>This class centralizes asset loading and lifecycle policy in one place. It wraps a single libGDX
 * {@link AssetManager}, plus pooled wrappers ({@link FlixelGraphic}, {@link FlixelAsset})
 * that track {@code persist} and reference counts and can be cleared on state switches.
 *
 * <p><b>Recommended usage:</b> Access this via {@link me.stringdotjar.flixelgdx.Flixel#assets}.
 *
 * <p><b>Experts:</b> {@link #getManager()} exposes the underlying {@link AssetManager} for custom
 * loaders, {@link AssetDescriptor} batches, or APIs not wrapped here.
 */
public class FlixelAssetManager {

  private AssetManager manager;

  private final ConcurrentHashMap<String, String> audioPathCache = new ConcurrentHashMap<>();

  private final ObjectMap<String, FlixelGraphic> graphicCache = new ObjectMap<>();
  private int ownedGraphicId;

  private final ObjectMap<AssetId, FlixelAsset<?>> typedAssetCache = new ObjectMap<>();

  private record AssetId(@NotNull String key, @NotNull Class<?> type) {
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof AssetId other)) return false;
      return key.equals(other.key) && type.equals(other.type);
    }

    @Override
    public int hashCode() {
      return 31 * key.hashCode() + type.hashCode();
    }
  }

  /** Constructs a new asset manager with the default loaders for audio, strings, and sound sources. */
  public FlixelAssetManager() {
    manager = new AssetManager();
    manager.setLoader(MASound.class, new MASoundLoader(Flixel.sound.getEngine(), manager.getFileHandleResolver()));
    manager.setLoader(String.class, new FlixelStringAssetLoader(manager.getFileHandleResolver()));
    manager.setLoader(FlixelSoundSource.class, new FlixelSoundSourceLoader(manager.getFileHandleResolver()));
  }

  /** Returns the underlying libGDX {@link AssetManager}. */
  @NotNull
  public AssetManager getManager() {
    return manager;
  }

  /**
   * Delegates to {@link AssetManager#load(String, Class)}.
   *
   * @param fileName The name of the file to load.
   * @param type The type of the asset to load.
   */
  public <T> void load(@NotNull String fileName, @NotNull Class<T> type) {
    manager.load(fileName, type);
  }

  /**
   * Enqueues the given source using its key and type.
   *
   * @param source The source to load.
   */
  public void load(@NotNull FlixelSource<?> source) {
    if (source == null) {
      throw new IllegalArgumentException("Source cannot be null.");
    }
    load(source.getAssetKey(), source.getType());
  }

  /**
   * Enqueues {@code fileName} as a {@link Texture}. No-op if already loaded as a texture.
   *
   * @param fileName The name of the file to load.
   */
  public void load(@NotNull String fileName) {
    if (!manager.isLoaded(fileName, Texture.class)) {
      manager.load(fileName, Texture.class);
    }
  }

  /**
   * Enqueues each path as a {@link Texture}. Skips entries already loaded as textures.
   *
   * @param fileNames The names of the files to load.
   */
  public void loadImages(@NotNull String... fileNames) {
    for (String fileName : fileNames) {
      if (!manager.isLoaded(fileName, Texture.class)) {
        manager.load(fileName, Texture.class);
      }
    }
  }

  /**
   * Delegates to {@link AssetManager#load(AssetDescriptor)}.
   *
   * @param assetDescriptor The asset descriptor to load.
   */
  public void load(@NotNull AssetDescriptor<?> assetDescriptor) {
    manager.load(assetDescriptor);
  }

  /**
   * Delegates to {@link AssetManager#update()}.
   *
   * @return Whether the asset manager has finished loading all assets.
   */
  public boolean update() {
    return manager.update();
  }

  /**
   * Delegates to {@link AssetManager#update(int)}.
   *
   * @param millis The number of milliseconds to update the asset manager.
   * @return Whether the asset manager has finished loading all assets.
   */
  public boolean update(int millis) {
    return manager.update(millis);
  }

  /** Delegates to {@link AssetManager#getProgress()}. */
  public float getProgress() {
    return manager.getProgress();
  }

  /**
   * Delegates to {@link AssetManager#isLoaded(String)}.
   *
   * @param fileName The name of the file to check.
   * @return Whether the file is loaded.
   */
  public boolean isLoaded(@NotNull String fileName) {
    return manager.isLoaded(fileName);
  }

  /**
   * Delegates to {@link AssetManager#isLoaded(String, Class)}.
   *
   * @param fileName The name of the file to check.
   * @param type The type of the asset to check.
   * @return Whether the file is loaded.
   */
  public boolean isLoaded(@NotNull String fileName, @NotNull Class<?> type) {
    return manager.isLoaded(fileName, type);
  }

  /**
   * Whether the given source is loaded.
   *
   * @param source The source to check.
   * @return Whether the source is loaded.
   */
  public boolean isLoaded(@NotNull FlixelSource<?> source) {
    if (source == null) {
      throw new IllegalArgumentException("Source cannot be null.");
    }
    return isLoaded(source.getAssetKey(), source.getType());
  }

  /**
   * Delegates to {@link AssetManager#get(String, Class)}.
   *
   * @param fileName The name of the file to get.
   * @param type The type of the asset to get.
   * @return The asset.
   */
  @NotNull
  public <T> T get(@NotNull String fileName, @NotNull Class<T> type) {
    return manager.get(fileName, type);
  }

  /**
   * Delegates to {@link #get(String, Class)} using the source's key and type.
   *
   * @param source The source to get.
   * @return The asset.
   */
  @NotNull
  public <T> T get(@NotNull FlixelSource<T> source) {
    if (source == null) {
      throw new IllegalArgumentException("Source cannot be null.");
    }
    return get(source.getAssetKey(), source.getType());
  }

  /**
   * Requires the source to already be loaded, then returns it.
   *
   * @param source The source to require.
   * @return The asset.
   */
  @NotNull
  public <T> T require(@NotNull FlixelSource<T> source) {
    if (source == null) {
      throw new IllegalArgumentException("Source cannot be null.");
    }
    return source.require(this);
  }

  /**
   * Shorthand for {@link #get(String, Class) get(fileName, Texture.class)}.
   *
   * @param fileName The name of the file to get.
   * @return The texture.
   */
  @NotNull
  public Texture getTexture(@NotNull String fileName) {
    return manager.get(fileName, Texture.class);
  }

  /**
   * Returns the loaded texture for {@code assetKey}.
   *
   * @param assetKey The key of the asset to get.
   * @return The texture.
   * @throws IllegalStateException if the texture is not loaded.
   */
  @NotNull
  public Texture requireTexture(@NotNull String assetKey) {
    if (!manager.isLoaded(assetKey, Texture.class)) {
      throw new IllegalStateException(
        "Texture not loaded: \"" + assetKey + "\". Preload it in a loading state (Flixel.assets.load + Flixel.assets.update()), "
          + "or call loadTextureNow(String) explicitly."
      );
    }
    return manager.get(assetKey, Texture.class);
  }

  /**
   * Loads {@code assetKey} as a texture, then returns it. Note that this will block the main thread.
   *
   * @param assetKey The key of the asset to load.
   * @return The texture.
   */
  @NotNull
  public Texture loadTextureNow(@NotNull String assetKey) {
    if (!manager.isLoaded(assetKey, Texture.class)) {
      manager.load(assetKey, Texture.class);
      manager.finishLoadingAsset(assetKey);
    }
    return manager.get(assetKey, Texture.class);
  }

  /**
   * Resolves an internal asset path to an absolute filesystem path that MiniAudio can open, with caching.
   *
   * @param path The path to resolve.
   * @return The resolved path.
   */
  @NotNull
  public String resolveAudioPath(@NotNull String path) {
    return audioPathCache.computeIfAbsent(path, this::extractAssetPath);
  }

  /**
   * Converts an internal path to an absolute filesystem path (temp-file extraction when assets live in a JAR).
   *
   * @param path The path to convert.
   * @return The converted path.
   * @throws RuntimeException if the path cannot be converted.
   */
  @NotNull
  public String extractAssetPath(@NotNull String path) {
    FileHandle handle = Gdx.files.internal(path);
    try {
      File file = handle.file();
      if (file.exists()) {
        return file.getAbsolutePath();
      }
    } catch (Exception ignored) {
      // When running from a packaged JAR, internal/classpath handles may not expose a real filesystem File.
    }
    String ext = path.contains(".") ? path.substring(path.lastIndexOf('.')) : "";
    try {
      if (ext.isEmpty()) {
        ext = ".tmp";
      }
      File temp = File.createTempFile("flixel_asset_", ext);
      temp.deleteOnExit();
      handle.copyTo(new FileHandle(temp));
      return temp.getAbsolutePath();
    } catch (IOException e) {
      throw new RuntimeException("Failed to extract asset from JAR: " + path, e);
    }
  }

  /**
   * Delegates to {@link AssetManager#unload(String)}.
   *
   * @param fileName The name of the file to unload.
   */
  public void unload(@NotNull String fileName) {
    manager.unload(fileName);
  }

  /** Delegates to {@link AssetManager#finishLoading()}. */
  public void finishLoading() {
    manager.finishLoading();
  }

  /** Delegates to {@link AssetManager#finishLoadingAsset(String)}.
   *
   * @param fileName The name of the file to finish loading.
   */
  public void finishLoadingAsset(@NotNull String fileName) {
    manager.finishLoadingAsset(fileName);
  }

  /** Delegates to {@link AssetManager#getDiagnostics()}.
   *
   * @return The diagnostics, which contains ref counts and dependency info for all assets.
   */
  @NotNull
  public String getDiagnostics() {
    return manager.getDiagnostics();
  }

  /** Unload non-persisting wrapper entries with zero references. Call this when a state is destroyed. */
  public void clearNonPersist() {
    clearNonPersistGraphics();
    clearNonPersistTypedAssets();
  }

  /** Disposes the internal {@link AssetManager} and clears wrapper pools. */
  public void dispose() {
    if (manager != null) {
      manager.dispose();
      manager = null;
    }
    ownedGraphicId = 0;
    graphicCache.clear();
    typedAssetCache.clear();
    audioPathCache.clear();
  }

  /**
   * Obtains a pooled graphic for the given asset key.
   *
   * @param assetKey The key of the asset to obtain.
   * @return The pooled graphic.
   */
  @NotNull
  public FlixelGraphic obtainGraphic(@NotNull String assetKey) {
    FlixelGraphic g = graphicCache.get(assetKey);
    if (g == null) {
      g = FlixelGraphic.createPooledKey(assetKey);
      graphicCache.put(assetKey, g);
    }
    return g;
  }

  /**
   * Obtains a pooled graphic for the given texture.
   *
   * @param texture The texture to obtain.
   * @return The pooled graphic.
   */
  @NotNull
  public FlixelGraphic obtainOwnedGraphic(@NotNull Texture texture) {
    String key = "__owned_texture__/" + (ownedGraphicId++);
    FlixelGraphic g = FlixelGraphic.createPooledOwned(key, texture);
    graphicCache.put(key, g);
    return g;
  }

  /**
   * Peeks a graphic for the given asset key.
   *
   * @param assetKey The key of the asset to peek.
   * @return The graphic.
   */
  @Nullable
  public FlixelGraphic peekGraphic(@NotNull String assetKey) {
    return graphicCache.get(assetKey);
  }

  /** Unload non-persisting graphics with zero references. Call this when a state is destroyed. */
  public void clearNonPersistGraphics() {
    AssetManager assets = manager;

    Array<String> toRemove = null;
    for (ObjectMap.Entry<String, FlixelGraphic> e : graphicCache) {
      FlixelGraphic g = e.value;
      if (g == null) continue;
      if (g.persist) continue;
      if (g.getRefCount() > 0) continue;

      if (g.isOwned()) {
        Texture t = g.getOwnedTexture();
        if (t != null) {
          t.dispose();
        }
      } else if (assets != null) {
        if (assets.isLoaded(g.getAssetKey(), Texture.class)) {
          assets.unload(g.getAssetKey());
        }
      }

      if (toRemove == null) {
        toRemove = new Array<>();
      }
      toRemove.add(g.getAssetKey());
    }

    if (toRemove != null) {
      for (int i = 0; i < toRemove.size; i++) {
        graphicCache.remove(toRemove.get(i));
      }
    }
  }

  /**
   * Obtains a typed asset for the given asset key and type.
   *
   * @param assetKey The key of the asset to obtain.
   * @param type The type of the asset to obtain.
   * @return The typed asset.
   */
  @NotNull
  @SuppressWarnings("unchecked")
  public <T> FlixelAsset<T> obtainTypedAsset(@NotNull String assetKey, @NotNull Class<T> type) {
    AssetId id = new AssetId(assetKey, type);
    FlixelAsset<?> existing = typedAssetCache.get(id);
    if (existing != null) {
      return (FlixelAsset<T>) existing;
    }
    FlixelAsset<T> created = new FlixelAsset<>(this, assetKey, type);
    typedAssetCache.put(id, created);
    return created;
  }

  /**
   * Peeks a typed asset for the given asset key and type.
   *
   * @param assetKey The key of the asset to peek.
   * @param type The type of the asset to peek.
   * @return The typed asset.
   */
  @Nullable
  public FlixelAsset<?> peekTypedAsset(@NotNull String assetKey, @NotNull Class<?> type) {
    return typedAssetCache.get(new AssetId(assetKey, type));
  }

  /** Unload non-persisting typed assets with zero references. Call this when a state is destroyed. */
  public void clearNonPersistTypedAssets() {

    Array<AssetId> toRemove = null;
    for (ObjectMap.Entry<AssetId, FlixelAsset<?>> e : typedAssetCache) {
      FlixelAsset<?> a = e.value;
      if (a == null) continue;
      if (a.isPersist()) continue;
      if (a.getRefCount() > 0) continue;

      if (manager.isLoaded(a.getAssetKey(), a.getType())) {
        manager.unload(a.getAssetKey());
      }

      if (toRemove == null) {
        toRemove = new Array<>();
      }
      toRemove.add(e.key);
    }

    if (toRemove != null) {
      for (int i = 0; i < toRemove.size; i++) {
        typedAssetCache.remove(toRemove.get(i));
      }
    }
  }
}

