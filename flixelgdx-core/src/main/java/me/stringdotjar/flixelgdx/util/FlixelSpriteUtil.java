/**********************************************************************************
 * Copyright (c) 2025-2026 stringdotjar
 *
 * This file is part of the FlixelGDX framework, licensed under the MIT License.
 * See the LICENSE file in the repository root for full license information.
 **********************************************************************************/

package me.stringdotjar.flixelgdx.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;

import me.stringdotjar.flixelgdx.asset.FlixelAssetManager;
import me.stringdotjar.flixelgdx.graphics.FlixelGraphic;

import org.jetbrains.annotations.NotNull;

/**
 * Helper class related to {@link me.stringdotjar.flixelgdx.FlixelSprite}.
 *
 * <p>These utilities are designed to work with FlixelGDX's normal Batch-based draw flow.
 * Avoid using ShapeRenderer in core game rendering unless you control the render pipeline.
 *
 * <p>Do not call {@link #createWhitePixelTexture()} every frame or per
 * sprite instance in hot paths. That allocates new {@link Pixmap} and {@link Texture} objects and
 * will spike heap usage on most JVMs. Prefer {@link #obtainWhitePixelTexture(FlixelAssetManager)},
 * which registers a single persistent texture with the asset manager (see
 * {@link me.stringdotjar.flixelgdx.asset.FlixelDefaultAssetManager}) and reuses it for the lifetime of the game.
 */
public final class FlixelSpriteUtil {

  private FlixelSpriteUtil() {}

  /**
   * Fixed asset key for the framework-owned 1x1 white {@link Texture} registered via
   * {@link #obtainWhitePixelTexture(FlixelAssetManager)}.
   */
  public static final String WHITE_PIXEL_TEXTURE_KEY = "__flixel_internal__/white_pixel_1x1";

  private static final Object WHITE_PIXEL_LOCK = new Object();

  /**
   * Returns the shared 1x1 white {@link Texture} registered with {@code assets}. The first call
   * creates the texture, wraps it in a {@link FlixelGraphic} with {@link FlixelGraphic#setPersist(boolean)}
   * {@code true}, and registers it with {@link FlixelAssetManager#registerWrapper}.
   * Callers must not {@link Texture#dispose()} this texture; lifecycle follows the asset manager.
   *
   * @param assets Non-null manager from {@link me.stringdotjar.flixelgdx.Flixel#ensureAssets()}.
   */
  @NotNull
  public static Texture obtainWhitePixelTexture(@NotNull FlixelAssetManager assets) {
    synchronized (WHITE_PIXEL_LOCK) {
      FlixelGraphic existing = assets.peekWrapper(WHITE_PIXEL_TEXTURE_KEY, FlixelGraphic.class);
      if (existing != null) {
        return existing.require();
      }
      Texture t = createWhitePixelTexture();
      FlixelGraphic g = new FlixelGraphic(assets, WHITE_PIXEL_TEXTURE_KEY, t);
      g.setPersist(true);
      assets.registerWrapper(g);
      return g.require();
    }
  }

  /**
   * Creates a 1x1 white pixel texture.
   *
   * <p><b>Ownership:</b> Prefer {@link #obtainWhitePixelTexture(FlixelAssetManager)} so only one
   * instance exists. If you call this directly, you own the returned texture and must dispose it.
   *
   * @return The created white pixel texture.
   */
  public static Texture createWhitePixelTexture() {
    Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    px.setColor(Color.WHITE);
    px.fill();
    Texture t = new Texture(px);
    px.dispose();
    return t;
  }

  /**
   * Draws a rectangle with the given color.
   *
   * @param batch The batch to draw the rectangle on.
   * @param whitePixel The white pixel texture to use for the rectangle.
   * @param x The x position of the rectangle.
   * @param y The y position of the rectangle.
   * @param w The width of the rectangle.
   * @param h The height of the rectangle.
   * @param color The color of the rectangle.
   */
  public static void drawRect(@NotNull Batch batch,
                              @NotNull Texture whitePixel,
                              float x,
                              float y,
                              float w,
                              float h,
                              @NotNull Color color) {
    batch.setColor(color);
    batch.draw(whitePixel, x, y, w, h);
    batch.setColor(Color.WHITE);
  }

  public static void drawBorder(@NotNull Batch batch,
                                @NotNull Texture whitePixel,
                                float x,
                                float y,
                                float w,
                                float h,
                                float thickness,
                                @NotNull Color color) {
    thickness = Math.max(0f, thickness);
    if (thickness <= 0f) {
      return;
    }
    batch.setColor(color);
    // Top.
    batch.draw(whitePixel, x, y + h - thickness, w, thickness);
    // Bottom.
    batch.draw(whitePixel, x, y, w, thickness);
    // Left.
    batch.draw(whitePixel, x, y, thickness, h);
    // Right.
    batch.draw(whitePixel, x + w - thickness, y, thickness, h);
    batch.setColor(Color.WHITE);
  }

  /**
   * Creates a 2-color linear gradient {@link Pixmap}.
   *
   * <p>Caller owns the returned Pixmap and must dispose of it through {@link Pixmap#dispose()} themselves.
   *
   * @param width The width of the gradient.
   * @param height The height of the gradient.
   * @param start The start color of the gradient.
   * @param end The end color of the gradient.
   * @param horizontal Whether the gradient is horizontal.
   * @return The created gradient Pixmap.
   */
  public static Pixmap createLinearGradientPixmap(int width,
                                                  int height,
                                                  @NotNull Color start,
                                                  @NotNull Color end,
                                                  boolean horizontal) {
    width = Math.max(1, width);
    height = Math.max(1, height);
    Pixmap pm = new Pixmap(width, height, Pixmap.Format.RGBA8888);

    float r0 = start.r;
    float g0 = start.g;
    float b0 = start.b;
    float a0 = start.a;
    float r1 = end.r;
    float g1 = end.g;
    float b1 = end.b;
    float a1 = end.a;

    for (int i = 0; i < (horizontal ? width : height); i++) {
      setInterpolatedColor(pm, i, horizontal ? width : height, r0, r1, g0, g1, b0, b1, a0, a1);
      for (int j = 0; j < (horizontal ? height : width); j++) {
        pm.drawPixel(horizontal ? i : j, horizontal ? j : i);
      }
    }

    return pm;
  }

  /**
   * Creates a 2-color linear gradient {@link Texture}.
   *
   * <p>Caller owns the returned Texture and must dispose of it through {@link Texture#dispose()} themselves.
   *
   * @param width The width of the gradient.
   * @param height The height of the gradient.
   * @param start The start color of the gradient.
   * @param end The end color of the gradient.
   * @param horizontal Whether the gradient is horizontal.
   * @return The created gradient Texture.
   */
  public static Texture createLinearGradientTexture(int width,
                                                    int height,
                                                    @NotNull Color start,
                                                    @NotNull Color end,
                                                    boolean horizontal) {
    Pixmap pm = createLinearGradientPixmap(width, height, start, end, horizontal);
    Texture t = new Texture(pm);
    pm.dispose();
    return t;
  }

  private static void setInterpolatedColor(Pixmap pm, int position, int dimension,
                                    float r0, float r1, float g0, float g1,
                                    float b0, float b1, float a0, float a1) {
    float t = dimension <= 1 ? 1f : (position / (float) (dimension - 1));
    pm.setColor(
      MathUtils.lerp(r0, r1, t),
      MathUtils.lerp(g0, g1, t),
      MathUtils.lerp(b0, b1, t),
      MathUtils.lerp(a0, a1, t)
    );
  }
}
