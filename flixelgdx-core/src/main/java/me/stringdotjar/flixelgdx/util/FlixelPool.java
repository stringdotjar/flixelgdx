package me.stringdotjar.flixelgdx.util;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.function.Supplier;

/**
 * A {@link Pool} that stores all freed objects in its own backing storage so it can support
 * type-based obtain: obtain a specific subtype from a single pool without maintaining a
 * separate pool per type.
 *
 * <p>Use {@link #obtain(Class, Supplier)} to get an instance of the requested type (reused from
 * the pool if available, otherwise from the factory). Use {@link #free(Object)} to return
 * objects; they are reset if they implement {@link Pool.Poolable}.
 *
 * <p>The no-arg {@link #obtain()} is disabled (throws {@link UnsupportedOperationException})
 * so that the libGDX Pool's original container is never used; use {@link #obtain(Class, Supplier)} instead.
 *
 * @param <T> The base type of objects in this pool (e.g. {@link me.stringdotjar.flixelgdx.tween.FlixelTween}).
 */
public abstract class FlixelPool<T> extends Pool<T> {

  private final Array<T> storage;

  /** Creates a pool with default initial capacity (16). */
  public FlixelPool() {
    this(16);
  }

  /**
   * Creates a pool with the given initial capacity for the free-object array.
   *
   * @param initialCapacity Initial capacity of the internal storage.
   */
  public FlixelPool(int initialCapacity) {
    this.storage = new Array<>(false, initialCapacity);
  }

  @Override
  public final void free(T object) {
    if (object == null) {
      return;
    }
    if (object instanceof Pool.Poolable) {
      ((Pool.Poolable) object).reset();
    }
    storage.add(object);
  }

  /**
   * Not supported; this pool uses its own storage, not the original libGDX Pool container.
   * Use {@link #obtain(Class, Supplier)} instead.
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  public final T obtain() {
    throw new UnsupportedOperationException("Use obtain(Class, Supplier) instead; this pool does not use the original Pool container.");
  }

  /**
   * Obtains an object of the given type from the pool, or creates one using the factory if none
   * of that type are available. Objects returned from the pool are already reset (from when
   * they were freed).
   *
   * @param type The requested type (e.g. {@code FlixelPropertyTween.class}).
   * @param factory Creates a new instance when the pool has no instance of {@code type}.
   * @return An instance of type {@code U}, either from the pool or from {@code factory}.
   */
  @SuppressWarnings("unchecked")
  public <U extends T> U obtain(Class<U> type, Supplier<U> factory) {
    for (int i = 0; i < storage.size; i++) {
      T t = storage.get(i);
      if (type.isInstance(t)) {
        storage.removeIndex(i);
        return (U) t;
      }
    }
    return factory.get();
  }

  @Override
  public final void clear() {
    storage.clear();
  }

  /**
   * Returns the number of objects currently in the pool (available for obtain).
   *
   * @return The number of free objects.
   */
  public int getFree() {
    return storage.size;
  }

  /**
   * Ensures the internal storage can hold at least {@code minCapacity} elements without
   * resizing. Useful when you know a burst of allocations is coming.
   *
   * @param minCapacity Minimum capacity for the free-object array.
   */
  public void ensureCapacity(int minCapacity) {
    storage.ensureCapacity(minCapacity);
  }
}
