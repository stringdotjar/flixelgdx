package me.stringdotjar.flixelgdx.debug;

import com.badlogic.gdx.Gdx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * Manages the variable watch list for the debug overlay. Watch entries are getter-based
 * (no reflection). Each entry is a display name mapped to a {@link Supplier} whose value
 * is polled every frame.
 *
 * <p>Access via {@code Flixel.watch}. The API mirrors HaxeFlixel's
 * {@code FlxG.watch} front-end as closely as possible while staying idiomatic in Java.
 *
 * <ul>
 *   <li>{@link #add(String, Supplier)}: watch a supplier (equivalent to HaxeFlixel's {@code addFunction})</li>
 *   <li>{@link #remove(String)}: remove a watched entry by name</li>
 *   <li>{@link #addQuick(String, Object)}: set/update a value directly (call in {@code update()})</li>
 *   <li>{@link #removeQuick(String)}: remove a quick-watch entry</li>
 *   <li>{@link #addMouse()} / {@link #removeMouse()}: convenience for mouse coordinates</li>
 * </ul>
 */
public class FlixelDebugWatchManager {

  private static final String MOUSE_WATCH_NAME = "Mouse";

  private final Map<String, Supplier<?>> watches = new ConcurrentHashMap<>();
  private final Map<String, Object> quickWatches = new ConcurrentHashMap<>();

  /**
   * Registers a watch entry. If an entry with the same name already exists it is replaced.
   * Equivalent to HaxeFlixel's {@code FlxG.watch.addFunction}.
   *
   * @param displayName The label shown in the watch panel.
   * @param valueGetter A supplier that returns the current value each frame.
   */
  public void add(@NotNull String displayName, @NotNull Supplier<?> valueGetter) {
    if (displayName == null || valueGetter == null) {
      return;
    }
    watches.put(displayName, valueGetter);
  }

  /**
   * Removes a previously registered watch entry by name.
   *
   * @param displayName The label of the entry to remove.
   */
  public void remove(@NotNull String displayName) {
    if (displayName != null) {
      watches.remove(displayName);
    }
  }

  /**
   * Adds or updates a quick-watch entry. Call this every frame from {@code update()} when you
   * want to display a value that isn't backed by a supplier. Equivalent to HaxeFlixel's
   * {@code FlxG.watch.addQuick}.
   *
   * @param displayName The label shown in the watch panel.
   * @param value The current value to display.
   */
  public void addQuick(@NotNull String displayName, @NotNull Object value) {
    if (displayName == null) {
      return;
    }
    quickWatches.put(displayName, value != null ? value : "null");
  }

  /**
   * Removes a quick-watch entry by name.
   *
   * @param displayName The label of the quick-watch entry to remove.
   */
  public void removeQuick(@NotNull String displayName) {
    if (displayName != null) {
      quickWatches.remove(displayName);
    }
  }

  /** Adds a convenience watch entry that shows the current mouse screen position. */
  public void addMouse() {
    watches.put(MOUSE_WATCH_NAME, () -> Gdx.input.getX() + ", " + Gdx.input.getY());
  }

  /** Removes the convenience mouse position watch entry. */
  public void removeMouse() {
    watches.remove(MOUSE_WATCH_NAME);
  }

  /**
   * Iterates every watch entry (suppliers first, then quick-watches), invoking the callback
   * with each display name and its current resolved value. No intermediate collections are
   * created.
   *
   * @param callback Receives (displayName, currentValue) for every entry.
   */
  public void forEach(@NotNull BiConsumer<String, Object> callback) {
    for (Map.Entry<String, Supplier<?>> entry : watches.entrySet()) {
      Object val;
      try {
        val = entry.getValue().get();
      } catch (Exception e) {
        val = "<error>";
      }
      callback.accept(entry.getKey(), val);
    }
    for (Map.Entry<String, Object> entry : quickWatches.entrySet()) {
      callback.accept(entry.getKey(), entry.getValue());
    }
  }

  /** Returns {@code true} when there are no watch or quick-watch entries registered. */
  public boolean isEmpty() {
    return watches.isEmpty() && quickWatches.isEmpty();
  }

  /** Clears all watch and quick-watch entries. */
  public void clear() {
    watches.clear();
    quickWatches.clear();
  }
}
