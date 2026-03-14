package me.stringdotjar.flixelgdx.tween;

import com.badlogic.gdx.utils.SnapshotArray;
import me.stringdotjar.flixelgdx.util.FlixelPool;

import java.util.function.Supplier;

/**
 * Manager class for handling a list of active {@link FlixelTween}s.
 *
 * <p>Mirrors <a href="https://api.haxeflixel.com/flixel/tweens/FlxTweenManager.html">FlxTweenManager</a>:
 * normally used via {@link FlixelTween#getGlobalManager()} rather than instantiating separately.
 * Adding a tween via {@link #addTween(FlixelTween)} automatically starts it.
 *
 * <p>Uses a single {@link FlixelPool} for all tween types. Obtain a tween via
 * {@link #obtainTween(Class, Supplier)} (reusing a freed instance of the requested type if
 * available, otherwise using the factory to create one).
 */
public class FlixelTweenManager {

  /** Array where all current active tweens are stored. */
  protected final SnapshotArray<FlixelTween> activeTweens = new SnapshotArray<>(FlixelTween[]::new);

  /** Pool of freed tweens (any subtype) available for reuse. */
  private final FlixelPool<FlixelTween> tweenPool = new FlixelPool<FlixelTween>() {
    @Override
    protected FlixelTween newObject() {
      return new FlixelTween();
    }
  };

  /**
   * Obtains a tween of the given type from the pool, or creates one using the factory if none
   * of that type are available. The returned tween is reset; the caller must set its settings
   * (and any type-specific state) before adding it via {@link #addTween(FlixelTween)}.
   *
   * @param type The tween class (e.g. {@link me.stringdotjar.flixelgdx.tween.type.FlixelPropertyTween}.class).
   * @param factory Creates a new tween when the pool has no instance of {@code type}.
   * @return A reset tween of type {@code T}, either from the pool or from {@code factory}.
   */
  public <T extends FlixelTween> T obtainTween(Class<T> type, Supplier<T> factory) {
    return tweenPool.obtain(type, factory);
  }

  /**
   * Adds the tween to this manager and starts it immediately.
   *
   * @param tween The tween to add and start.
   * @return The same tween for chaining.
   */
  public FlixelTween addTween(FlixelTween tween) {
    tween.setManager(this);
    tween.start();
    return tween;
  }

  /**
   * Updates all active tweens that are stored and updated in {@code this} manager.
   *
   * <p>Iterates in reverse so that finished ONESHOT tweens can be removed by index
   * without skipping elements or traversing null padding beyond the array's valid size.
   *
   * @param elapsed The amount of time that has passed since the last frame.
   */
  public void update(float elapsed) {
    FlixelTween[] items = activeTweens.begin();
    for (int i = 0; i < activeTweens.size; i++) {
      FlixelTween tween = items[i];
      if (tween == null || !tween.isActive()) {
        continue;
      }
      tween.update(elapsed);
    }

    for (int i = 0; i < activeTweens.size; i++) {
      FlixelTween tween = items[i];
      if (tween != null && tween.isFinished()) {
        if (tween.manager != this) {
          continue;
        }
        tween.finish();
      }
    }

    activeTweens.end();
  }

  /**
   * Remove an {@link FlixelTween} from {@code this} manager.
   * Note that when the FlixelTween is removed, it will call {@link FlixelTween#destroy} and it can no longer be used.
   *
   * @param tween The FlixelTween to remove.
   * @param destroy To determine if the FlixelTween will be destroyed upon calling the method.
   * @return  The removed FlixelTween object.
   */
  public FlixelTween removeTween(FlixelTween tween, boolean destroy) {
    if (tween == null) {
      return null;
    }

    tween.setActive(false);
    activeTweens.removeValue(tween, true);

    if (destroy) {
      tweenPool.free(tween);
    }

    return tween;
  }

  public SnapshotArray<FlixelTween> getActiveTweens() {
    return activeTweens;
  }

  public FlixelPool<FlixelTween> getTweenPool() {
    return tweenPool;
  }

  public void clearPool() {
    tweenPool.clear();
  }

  public int getPoolFree() {
    return tweenPool.getFree();
  }
}
