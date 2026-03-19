package me.stringdotjar.flixelgdx.functional;

/**
 * Represents a supplier of {@code float}-valued results.
 *
 * <p>This is the float counterpart to {@link java.util.function.DoubleSupplier},
 * used to avoid boxing when repeatedly polling values every frame.
 */
@FunctionalInterface
public interface FloatSupplier {

  /**
   * Gets a float result.
   *
   * @return a result.
   */
  float getAsFloat();
}

