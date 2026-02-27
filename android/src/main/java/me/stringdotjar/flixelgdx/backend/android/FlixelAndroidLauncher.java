package me.stringdotjar.flixelgdx.backend.android;

import android.app.Activity;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import me.stringdotjar.flixelgdx.Flixel;
import me.stringdotjar.flixelgdx.FlixelGame;
import me.stringdotjar.flixelgdx.backend.android.alert.FlixelAndroidAlerter;

/**
 * Launches the Android version of the FlixelGDX game.
 */
public class FlixelAndroidLauncher {

  /**
   * Launches the Android version of the game with the given game instance.
   *
   * <p>This should be called from the onCreate method of the Android launcher class, and the
   * game instance should be created in the same general area.
   *
   * @param game The game instance to launch. This should already be initialized with the desired configuration values.
   */
  public static void launch(FlixelGame game, Activity activity) {
    Flixel.initialize(game, new FlixelAndroidAlerter(activity));

    AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
    configuration.useImmersiveMode = true;
  }
}
