/*********************************************************************************
 * Copyright (c) 2025-2026 stringdotjar
 *
 * This file is part of the FlixelGDX framework, licensed under the MIT License.
 * See the LICENSE file in the repository root for full license information.
 *********************************************************************************/

package me.stringdotjar.flixelgdx.gradle.teavm;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

/**
 * Configuration extension exposed as the {@code flixelgdx} DSL block in a web module's
 * {@code build.gradle}.
 *
 * <p>All properties have sensible defaults and are optional. The only required configuration is
 * {@code teavm.all.mainClass} in the {@code org.teavm} plugin block (see
 * {@link FlixelTeaVMPlugin} for usage).
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * flixelgdx {
 *   // Optional: override the canvas element ID (default: "flixelgdx-canvas").
 *   canvasId = 'my-game-canvas'
 *
 *   // Optional: change where the web app is assembled (default: "$buildDir/dist/webapp").
 *   // Must match teavm.js.outputDir.
 *   outputDir = file("$buildDir/dist/webapp")
 * }
 * }</pre>
 */
public abstract class FlixelTeaVMExtension {

  /** Gradle extension name used to register this extension under. */
  public static final String NAME = "flixelgdx";

  /** Default HTML canvas element ID expected by {@code FlixelTeaVMLauncher}. */
  public static final String DEFAULT_CANVAS_ID = "flixelgdx-canvas";

  /**
   * ID of the HTML {@code <canvas>} element that the game renders into.
   *
   * <p>Must match the {@code canvasID} field of {@code WebApplicationConfiguration} passed to
   * {@code FlixelTeaVMLauncher.launch()}. Defaults to {@value #DEFAULT_CANVAS_ID}.
   *
   * @return the canvas element ID property.
   */
  public abstract Property<String> getCanvasId();

  /**
   * Directory into which the assembled web application is written.
   *
   * <p>This must match the value of {@code teavm.js.outputDir} in the {@code org.teavm} plugin
   * block so that copied assets, web resources, and the generated {@code index.html} are placed
   * alongside the compiled {@code teavm.js} file. Defaults to
   * {@code "$buildDir/dist/webapp"}.
   *
   * @return the output directory property.
   */
  public abstract DirectoryProperty getOutputDir();

  /**
   * Directory that contains user-provided web resources such as a custom {@code index.html},
   * favicon, or additional scripts.
   *
   * <p>All files found here are copied verbatim into {@link #getOutputDir()}. If this directory
   * contains an {@code index.html}, the plugin skips automatic index generation. Defaults to
   * {@code src/main/webapp} relative to the web module.
   *
   * @return the webapp source directory property.
   */
  public abstract DirectoryProperty getWebappDir();

  /**
   * Directory whose contents are copied to {@code <outputDir>/assets/} before each build.
   *
   * <p>Defaults to the {@code assets/} directory at the root of the Gradle project (i.e. the
   * sibling of the core, desktop, and teavm modules).
   *
   * @return the assets source directory property.
   */
  public abstract DirectoryProperty getAssetsDir();

  /**
   * Whether the plugin should generate a default {@code index.html} when none is found in
   * {@link #getWebappDir()}.
   *
   * <p>The generated page includes a {@code <canvas>} with the ID from {@link #getCanvasId()} and
   * a {@code <script>} tag that loads {@code js/teavm.js}. Set to {@code false} to suppress
   * generation entirely (you must then provide your own {@code index.html}). Defaults to
   * {@code true}.
   *
   * @return the generate-index-html property.
   */
  public abstract Property<Boolean> getGenerateDefaultIndexHtml();
}
