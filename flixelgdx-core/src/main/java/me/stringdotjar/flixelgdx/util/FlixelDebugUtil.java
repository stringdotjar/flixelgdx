package me.stringdotjar.flixelgdx.util;

import com.badlogic.gdx.utils.SnapshotArray;

import me.stringdotjar.flixelgdx.Flixel;
import me.stringdotjar.flixelgdx.FlixelBasic;
import me.stringdotjar.flixelgdx.FlixelObject;
import me.stringdotjar.flixelgdx.display.FlixelState;
import me.stringdotjar.flixelgdx.group.FlixelGroupable;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

/**
 * Utility methods used by the debug overlay for recursively traversing the state's object
 * tree (counting active members, iterating {@link FlixelObject} instances for bounding-box
 * drawing, etc.).
 *
 * <p>Recursion descends into any member that implements {@link FlixelGroupable}, which
 * covers both {@link me.stringdotjar.flixelgdx.group.FlixelGroup} and
 * {@link me.stringdotjar.flixelgdx.group.FlixelSpriteGroup}.
 *
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

  private static int countActiveMembersRecursive(SnapshotArray<? extends FlixelBasic> members) {
    int count = 0;
    Object[] items = members.begin();
    for (int i = 0, n = members.size; i < n; i++) {
      FlixelBasic member = (FlixelBasic) items[i];
      if (member == null) {
        continue;
      }
      if (member.exists) {
        count++;
      }
      if (member instanceof FlixelGroupable<?> group) {
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

  private static void forEachVisibleObjectRecursive(@NotNull SnapshotArray<? extends FlixelBasic> members, @NotNull Consumer<FlixelObject> callback) {
    Object[] items = members.begin();
    for (int i = 0, n = members.size; i < n; i++) {
      FlixelBasic member = (FlixelBasic) items[i];
      if (member == null) {
        continue;
      }
      if (member instanceof FlixelObject obj && obj.exists && obj.visible) {
        callback.accept(obj);
      }
      if (member instanceof FlixelGroupable<?> group) {
        forEachVisibleObjectRecursive(group.getMembers(), callback);
      }
    }
    members.end();
  }
}
