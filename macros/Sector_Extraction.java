package macro;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import star.base.neo.Body;
import star.cadmodeler.CadModel;
import star.cadmodeler.SolidModelManager;
import star.common.GlobalParameterManager;
import star.common.Simulation;
import star.common.Solution;
import star.common.SolutionHistoryManager;
import star.common.StarMacro;
import star.starice.StarIceEngine;
import star.vis.Scene;

/**
 * Automates saving the active simulation and exporting a sector CAD model for further processing.
 */
public class Sector_Extraction extends StarMacro {

  private static final String CAD_MODEL_NAME = "In-Cylinder 3D-CAD 1";
  private static final String CAD_BODY_NAME = "Cylinder Sector: Sector Cylinder";
  private static final String IN_CYLINDER_SCENE = "In-Cylinder";
  private static final String STAR_ICE_RESOURCE = "StarIce";

  @Override
  public void execute() {
    Simulation simulation = getActiveSimulation();
    String designPrefix = resolveDesignPrefix(simulation);

    saveSimulationState(simulation, designPrefix);
    resetSimulationState(simulation);
    exportSectorCad(simulation, designPrefix);
  }

  private static String resolveDesignPrefix(Simulation simulation) {
    String sessionPath = simulation.getSessionPath();
    if (sessionPath == null || sessionPath.isEmpty()) {
      return "sector";
    }

    String fileName = sessionPath.substring(sessionPath.lastIndexOf('/') + 1);
    int underscoreIndex = fileName.indexOf('_');
    String trimmed = underscoreIndex >= 0 ? fileName.substring(0, underscoreIndex) : fileName;
    return trimmed.replace(".sim", "");
  }

  private void saveSimulationState(Simulation simulation, String designPrefix) {
    simulation.saveState(
        AutomationSupport.resolvePath(
            AutomationSupport.getExportDirectory(), designPrefix + ".sim"));
  }

  private void resetSimulationState(Simulation simulation) {
    simulation.get(SolutionHistoryManager.class).clear();

    Solution solution = simulation.getSolution();
    solution.clearSolution();

    simulation.loadStarIce(STAR_ICE_RESOURCE);

    StarIceEngine engine = simulation.get(StarIceEngine.class);
    engine.startStarIce();

    Scene inCylinderScene = simulation.getSceneManager().getScene(IN_CYLINDER_SCENE);
    inCylinderScene.setAdvancedRenderingEnabled(false);
    inCylinderScene.openInteractive();

    simulation.loadStarIce(STAR_ICE_RESOURCE);

    star.starice.ScalarGlobalParameter startAngle =
        (star.starice.ScalarGlobalParameter) simulation.get(GlobalParameterManager.class)
            .getObject("Start Angle");

    star.starice.CyclicTimeUnits cyclicTimeUnits =
        (star.starice.CyclicTimeUnits) simulation.getUnitsManager().getObject("degCA");

    engine.getEngineOperatingParameters().setParameter(startAngle, 720.0, cyclicTimeUnits, false);
    engine.updateStarIce();
  }

  private void exportSectorCad(Simulation simulation, String designPrefix) {
    CadModel cadModel =
        (CadModel) simulation.get(SolidModelManager.class).getObject(CAD_MODEL_NAME);

    Body sectorBody = (Body) cadModel.getBody(CAD_BODY_NAME);
    List<Body> bodies = new ArrayList<>(Arrays.asList(sectorBody));

    File exportFile =
        AutomationSupport.resolveFile(
            AutomationSupport.getExportDirectory(), designPrefix + ".x_b");

    cadModel.exportModel(
        bodies,
        exportFile,
        true,
        false,
        false,
        false,
        "0");
  }
}
