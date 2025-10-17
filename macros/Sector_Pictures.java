package macro;

import java.util.Arrays;
import java.util.List;

import star.base.neo.DoubleVector;
import star.common.Simulation;
import star.common.StarMacro;
import star.vis.CurrentView;
import star.vis.Displayer;
import star.vis.DisplayerManager;
import star.vis.DisplayerVisibilityOverride;
import star.vis.PartDisplayer;
import star.vis.Scene;

/**
 * Captures consistent renders of each sector design by applying a standardised camera position and
 * exporting PNG snapshots.
 */
public class Sector_Pictures extends StarMacro {

  private static final String GEOMETRY_SCENE_NAME = "Geometry Scene 1";
  private static final List<String> PART_NAMES =
      Arrays.asList("P0", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "P8", "P9");

  private static final DoubleVector EYE =
      new DoubleVector(new double[] {0.02787872615929598, 9.212431862836573E-4, -0.008763404275169969});
  private static final DoubleVector TARGET =
      new DoubleVector(new double[] {0.02787872615929598, -0.12411595435289759, -0.008763404275169931});
  private static final DoubleVector UP =
      new DoubleVector(new double[] {0.0, 3.074859849441974E-16, 1.0});
  private static final double MAGNIFICATION = 0.016963164754689935;
  private static final int IMAGE_WIDTH = 1920;
  private static final int IMAGE_HEIGHT = 1080;

  @Override
  public void execute() {
    Simulation simulation = getActiveSimulation();
    Scene geometryScene = simulation.getSceneManager().getScene(GEOMETRY_SCENE_NAME);
    CurrentView currentView = geometryScene.getCurrentView();

    for (String partName : PART_NAMES) {
      resetPartVisibility(geometryScene);
      showPart(geometryScene, partName);
      currentView.setInput(EYE, TARGET, UP, MAGNIFICATION, 1, 30.0);
      geometryScene.printAndWait(
          AutomationSupport.resolvePath(
              AutomationSupport.getImagesDirectory(), partName + ".png"),
          1,
          IMAGE_WIDTH,
          IMAGE_HEIGHT,
          true,
          false);
    }
  }

  private void resetPartVisibility(Scene scene) {
    DisplayerManager displayerManager = scene.getDisplayerManager();
    for (Displayer displayer : displayerManager.getObjects()) {
      if (displayer instanceof PartDisplayer) {
        ((PartDisplayer) displayer)
            .setVisibilityOverrideMode(DisplayerVisibilityOverride.HIDE_ALL_PARTS);
      }
    }
  }

  private void showPart(Scene scene, String partName) {
    PartDisplayer displayer = (PartDisplayer) scene.getDisplayerManager().getObject(partName);
    displayer.setVisibilityOverrideMode(DisplayerVisibilityOverride.USE_PART_PROPERTY);
  }
}
