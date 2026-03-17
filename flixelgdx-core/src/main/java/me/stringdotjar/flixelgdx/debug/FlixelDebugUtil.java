package me.stringdotjar.flixelgdx.debug;

import com.badlogic.gdx.utils.SnapshotArray;

import me.stringdotjar.flixelgdx.Flixel;
import me.stringdotjar.flixelgdx.FlixelBasic;
import me.stringdotjar.flixelgdx.FlixelObject;
import me.stringdotjar.flixelgdx.display.FlixelState;
import me.stringdotjar.flixelgdx.group.FlixelGroup;

import java.util.function.Consumer;

/**
 * Utility methods used by the debug overlay for recursively traversing the state's object
 * tree (counting active members, iterating {@link FlixelObject} instances for bounding-box
 * drawing, etc.).
 *
 * <p>All methods are designed to avoid per-frame heap allocation.
 */
public final class FlixelDebugUtil {

  private FlixelDebugUtil() {}

  /**
   * Recursively counts all active members in the current state's object tree. A member is
   * counted when {@code exists == true}.
   *
   * @return The number of active members, or {@code 0} if no state is loaded.
   */
  public static int countActiveMembers() {
    FlixelState state = Flixel.getState();
    if (state == null) {
      return 0;
    }
    return countActiveMembersRecursive(state.getMembers());
  }

  private static int countActiveMembersRecursive(SnapshotArray<FlixelBasic> members) {
    int count = 0;
    FlixelBasic[] items = members.begin();
    for (int i = 0, n = members.size; i < n; i++) {
      FlixelBasic member = items[i];
      if (member == null) {
        continue;
      }
      if (member.exists) {
        count++;
      }
      if (member instanceof FlixelGroup<?> group) {
        count += countActiveMembersRecursive(group.getMembers());
      }
    }
    members.end();
    return count;
  }

  /**
   * Iterates all visible {@link FlixelObject} instances in the current state's object tree
   * (where {@code exists} and {@code visible} are both {@code true}), invoking the callback
   * for each one. No intermediate collection is created.
   *
   * @param callback Invoked once per visible {@link FlixelObject}.
   */
  public static void forEachVisibleObject(Consumer<FlixelObject> callback) {
    FlixelState state = Flixel.getState();
    if (state == null) {
      return;
    }
    forEachVisibleObjectRecursive(state.getMembers(), callback);
  }

  private static void forEachVisibleObjectRecursive(SnapshotArray<FlixelBasic> members, Consumer<FlixelObject> callback) {
    FlixelBasic[] items = members.begin();
    for (int i = 0, n = members.size; i < n; i++) {
      FlixelBasic member = items[i];
      if (member == null) {
        continue;
      }
      if (member instanceof FlixelObject obj && obj.exists && obj.visible) {
        callback.accept(obj);
      }
      if (member instanceof FlixelGroup<?> group) {
        forEachVisibleObjectRecursive(group.getMembers(), callback);
      }
    }
    members.end();
  }
}
