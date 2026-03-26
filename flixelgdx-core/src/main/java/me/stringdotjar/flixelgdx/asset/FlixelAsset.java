/**********************************************************************************
 * Copyright (c) 2025-2026 stringdotjar
 *
 * This file is part of the FlixelGDX framework, licensed under the MIT License.
 * See the LICENSE file in the repository root for full license information.
 **********************************************************************************/

package me.stringdotjar.flixelgdx.asset;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Typed handle for one asset path + class, with optional {@code persist} and refcount.
 *
 * <p>Obtain handles with {@link #get(String, Class)} (wrappers are cached in the active
 * {@link FlixelAssetManager} instance on {@link me.stringdotjar.flixelgdx.Flixel#assets}).
 * Prefer {@link #queueLoad()} in a loading state and {@code Flixel.assets.update()} each frame.
 */
public final class FlixelAsset<T> {

  @NotNull
  private final String assetKey;

  @NotNull
  private final Class<T> type;

  @NotNull
  private final FlixelAssetManager assetManager;

  private boolean persist;
  private int refCount;

  FlixelAsset(@NotNull FlixelAssetManager assetManager, @NotNull String assetKey, @NotNull Class<T> type) {
    if (assetManager == null) {
      throw new IllegalArgumentException("Asset manager cannot be null.");
    }
    if (assetKey == null || assetKey.isEmpty()) {
      throw new IllegalArgumentException("Asset key cannot be null/empty.");
    }
    if (type == null) {
      throw new IllegalArgumentException("Type cannot be null.");
    }
    this.assetManager = assetManager;
    this.assetKey = assetKey;
    this.type = type;
    this.persist = false;
    this.refCount = 0;
  }

  @NotNull
  public String getAssetKey() {
    return assetKey;
  }

  @NotNull
  public Class<T> getType() {
    return type;
  }

  public boolean isPersist() {
    return persist;
  }

  public FlixelAsset<T> setPersist(boolean persist) {
    this.persist = persist;
    return this;
  }

  public int getRefCount() {
    return refCount;
  }

  public FlixelAsset<T> retain() {
    refCount++;
    return this;
  }

  public FlixelAsset<T> release() {
    refCount--;
    if (refCount < 0) {
      refCount = 0;
    }
    return this;
  }

  /**
   * Enqueues this asset into {@link FlixelAssets}. Safe to call multiple times.
   *
   * <p>Use this method to preload assets in your game's loading state.
   */
  public void queueLoad() {
    if (!assetManager.isLoaded(assetKey, type)) {
      assetManager.load(assetKey, type);
    }
  }

  /** Strict: requires the asset to already be loaded. */
  @NotNull
  public T require() {
    if (!assetManager.isLoaded(assetKey, type)) {
      throw new IllegalStateException(
        "Asset not loaded: \"" + assetKey + "\" (" + type.getSimpleName() + "). "
          + "Preload it in a loading state (FlixelAssets.load + FlixelAssets.update), or call loadNow() explicitly."
      );
    }
    return assetManager.get(assetKey, type);
  }

  /**
   * Explicit synchronous load for one-off cases. Avoid using implicitly during gameplay.
   */
  @NotNull
  public T loadNow() {
    if (!assetManager.isLoaded(assetKey, type)) {
      assetManager.load(assetKey, type);
      assetManager.finishLoadingAsset(assetKey);
    }
    return assetManager.get(assetKey, type);
  }

  @NotNull
  public FlixelAsset<T> get(@NotNull String assetKey, @NotNull Class<T> type) {
    return assetManager.obtainTypedAsset(assetKey, type);
  }

  @Nullable
  public FlixelAsset<?> peek(@NotNull String assetKey, @NotNull Class<?> type) {
    return assetManager.peekTypedAsset(assetKey, type);
  }
}
