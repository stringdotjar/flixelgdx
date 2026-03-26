/**
 * Asset loading and lifecycle for FlixelGDX.
 *
 * <p><b>{@link FlixelAssetManager}</b> - Centralized asset system used by FlixelGDX.
 * It wraps a libGDX {@link com.badlogic.gdx.assets.AssetManager} and provides consistent helpers
 * for loading ({@code load}, {@code update}), strict access ({@code require}), and lifecycle policy
 * (wrapper pools, reference counts, and clearing non persistent assets on state switches).
 *
 * <p>Game code typically uses {@link me.stringdotjar.flixelgdx.Flixel#assets} rather than constructing
 * a manager directly, but the manager is instantiable and extendable for advanced use cases.
 *
 * <p><b>{@link FlixelAsset}</b> - Typed handle for one {@code (path, Class)} pair with optional
 * {@code persist} and refcount. Prefer handles when you want Flixel style unload on state switch.
 *
 * <p><b>{@link FlixelSource}</b> - Small interface that lets built in and user defined source objects
 * expose a consistent {@code (assetKey, type)} contract for loading and requiring through the manager.
 *
 * <p><b>Experts:</b> Use {@link FlixelAssetManager#getManager()} only when you need raw libGDX behavior.
 */
package me.stringdotjar.flixelgdx.asset;
