package me.stringdotjar.flixelgdx;

/**
 * The base class for all visual/spatial objects in Flixel. Extends {@link FlixelBasic} with
 * position ({@link #x}, {@link #y}), dimensions ({@link #width}, {@link #height}), and
 * rotation ({@link #angle}).
 *
 * <p>Most games interact with this through {@link me.stringdotjar.flixelgdx.FlixelSprite},
 * which adds graphical capabilities on top of this spatial foundation.
 *
 * @see <a href="https://api.haxeflixel.com/flixel/FlxObject.html">FlxObject (HaxeFlixel)</a>
 */
public class FlixelObject extends FlixelBasic {

  /** X position of the upper left corner of this object in world space. */
  protected float x = 0f;

  /** Y position of the upper left corner of this object in world space. */
  protected float y = 0f;

  /** The width of this object's hitbox. */
  protected float width = 0f;

  /** The height of this object's hitbox. */
  protected float height = 0f;

  /** The angle (in degrees) of this object. Used for visual rotation in sprites. */
  protected float angle = 0f;

  public FlixelObject() {
    super();
  }

  public FlixelObject(float x, float y) {
    super();
    this.x = x;
    this.y = y;
  }

  public FlixelObject(float x, float y, float width, float height) {
    super();
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public float getWidth() {
    return width;
  }

  public void setWidth(float width) {
    this.width = width;
  }

  public float getHeight() {
    return height;
  }

  public void setHeight(float height) {
    this.height = height;
  }

  public float getAngle() {
    return angle;
  }

  public void setAngle(float angle) {
    this.angle = angle;
  }

  /**
   * Helper function to set the coordinates of this object.
   *
   * @param x The new x position.
   * @param y The new y position.
   */
  public void setPosition(float x, float y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Shortcut for setting both {@link #width} and {@link #height}.
   *
   * @param width The new width.
   * @param height The new height.
   */
  public void setSize(float width, float height) {
    this.width = width;
    this.height = height;
  }

  /** Adds {@code dx} to the current X position. */
  public void changeX(float dx) {x += dx;}

  /** Adds {@code dy} to the current Y position. */
  public void changeY(float dy) {y += dy;}

  /** Adds {@code dr} degrees to the current rotation angle. */
  public void changeRotation(float dr) {angle += dr;}

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(ID=" + ID
      + ", x=" + x + ", y=" + y
      + ", w=" + width + ", h=" + height + ")";
  }
}
