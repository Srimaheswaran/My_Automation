package macro;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Shared helper utilities for STAR-CCM+ automation macros.
 *
 * <p>The helper centralises common path handling logic and offers a light-weight configuration layer
 * via environment variables. Each directory can be overridden at runtime without modifying the
 * source files:</p>
 *
 * <ul>
 *   <li>{@code STAR_AUTOMATION_DESIGNS_DIR} – location of imported/exported design CAD files.</li>
 *   <li>{@code STAR_AUTOMATION_EXPORT_DIR} – location where simulations and exported CAD are saved.</li>
 *   <li>{@code STAR_AUTOMATION_IMAGES_DIR} – directory that receives rendered imagery.</li>
 * </ul>
 */
public final class AutomationSupport {

  private static final String DEFAULT_DESIGNS_DIR =
      "/nfs/nnwork011/106300/03_Simulations/Sector/V2410/Designs/";
  private static final String DEFAULT_EXPORT_DIR = DEFAULT_DESIGNS_DIR;
  private static final String DEFAULT_IMAGES_DIR = DEFAULT_DESIGNS_DIR;

  private static final String DESIGNS_ENV = "STAR_AUTOMATION_DESIGNS_DIR";
  private static final String EXPORT_ENV = "STAR_AUTOMATION_EXPORT_DIR";
  private static final String IMAGES_ENV = "STAR_AUTOMATION_IMAGES_DIR";

  private AutomationSupport() {}

  public static String getDesignsDirectory() {
    return ensureTrailingSeparator(resolveEnv(DESIGNS_ENV, DEFAULT_DESIGNS_DIR));
  }

  public static String getExportDirectory() {
    return ensureTrailingSeparator(resolveEnv(EXPORT_ENV, DEFAULT_EXPORT_DIR));
  }

  public static String getImagesDirectory() {
    return ensureTrailingSeparator(resolveEnv(IMAGES_ENV, DEFAULT_IMAGES_DIR));
  }

  public static String resolvePath(String first, String... more) {
    Path path = resolve(first, more);
    return path.toString().replace('\\', '/');
  }

  public static File resolveFile(String first, String... more) {
    return resolve(first, more).toFile();
  }

  private static Path resolve(String first, String... more) {
    return Paths.get(first, more).toAbsolutePath().normalize();
  }

  private static String resolveEnv(String envName, String defaultValue) {
    String value = System.getenv(envName);
    if (value == null) {
      return defaultValue;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? defaultValue : trimmed;
  }

  private static String ensureTrailingSeparator(String value) {
    if (value.endsWith("/") || value.endsWith("\\")) {
      return value;
    }
    return value + System.getProperty("file.separator");
  }
}
