package me.stringdotjar.flixelgdx;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * Interface for visible objects that can be drawn.
 *
 * @see FlixelBasic
 */
public interface FlixelDrawable {

  /**
   * Draws the visible object.
   *
   * @param batch The batch used for rendering.
   */
  void draw(Batch batch);
}
