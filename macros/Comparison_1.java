package macro;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import star.base.neo.Body;
import star.base.neo.DoubleVector;
import star.base.neo.NamedObject;
import star.cadmodeler.CadModel;
import star.cadmodeler.SolidModelManager;
import star.cadmodeler.TessellationDensityMode;
import star.common.Simulation;
import star.common.SimulationPartManager;
import star.common.StarMacro;
import star.vis.PartColorMode;
import star.vis.PartDisplayer;
import star.vis.Scene;
import star.vis.SceneManager;

/**
 * Imports a series of CAD designs, converts them into solid model parts and creates comparison
 * sections for visual analysis.
 */
public class Comparison_1 extends StarMacro {

  private static final String CAD_MODEL_NAME = "3D-CAD Model 1";
  private static final String BODY_NAME = "Cylinder Sector: Sector Cylinder";
  private static final String GEOMETRY_SCENE_NAME = "Geometry Scene 1";
  private static final String CAD_SCENE_NAME = "3D-CAD View";
  private static final int DESIGN_COUNT = 8;
  private static final String DESIGN_PREFIX = "P";

  private static final DoubleVector SECTION_NORMAL = new DoubleVector(new double[] {0.0, 0.0, 1.0});
  private static final DoubleVector SECTION_ORIGIN = new DoubleVector(new double[] {0.0, 0.0, 0.0});

  @Override
  public void execute() {
    Simulation simulation = getActiveSimulation();
    SceneManager sceneManager = simulation.getSceneManager();
    Scene geometryScene = sceneManager.getScene(GEOMETRY_SCENE_NAME);
    CadModel cadModel =
        (CadModel) simulation.get(SolidModelManager.class).getObject(CAD_MODEL_NAME);

    for (int designIndex = 1; designIndex <= DESIGN_COUNT; designIndex++) {
      String designName = DESIGN_PREFIX + designIndex;
      importDesign(simulation, cadModel, designName);
      star.cadmodeler.SolidModelPart part = createSolidModelPart(simulation, cadModel, designName);
      createComparisonSection(simulation, geometryScene, part, designName);
    }
  }

  private void importDesign(Simulation simulation, CadModel cadModel, String designName) {
    Scene cadScene = simulation.getSceneManager().createScene(CAD_SCENE_NAME);
    simulation.get(SolidModelManager.class).editCadModel(cadModel, cadScene);
    cadScene.openInteractive();
    cadScene.setAdvancedRenderingEnabled(false);
    cadScene.resetCamera();

    File designFile =
        AutomationSupport.resolveFile(
            AutomationSupport.getDesignsDirectory(), designName + ".x_b");

    cadModel.importCadFile(
        designFile,
        true,
        true,
        true,
        true,
        false,
        TessellationDensityMode.COARSE,
        true,
        false,
        false,
        true,
        false,
        false,
        false,
        false,
        true,
        false,
        true,
        star.base.neo.NeoProperty.fromString(
            "{'NX': 1, 'STEP': 1, 'SE': 1, 'CGR': 1, 'SW': 1, 'RHINO': 1, 'IFC': 1, 'ACIS': 1, 'JT': 1, 'IGES': 1, 'CATIAV5': 1, 'CATIAV4': 1, '3DXML': 1, 'CREO': 1, 'INV': 1}"),
        false,
        null,
        0.0);

    cadScene.resetCamera();

    Body body = (Body) cadModel.getBody(BODY_NAME);
    body.setPresentationName(designName);

    cadModel.update();
    simulation.get(SolidModelManager.class).endEditCadModel(cadModel);
    simulation.getSceneManager().remove(cadScene);
  }

  private star.cadmodeler.SolidModelPart createSolidModelPart(
      Simulation simulation, CadModel cadModel, String designName) {
    cadModel.createParts(
        new ArrayList<>(Collections.singletonList(cadModel.getBody(designName))),
        new ArrayList<>(Collections.emptyList()),
        true,
        false,
        1,
        false,
        false,
        3,
        "SharpEdges",
        30.0,
        2,
        true,
        1.0E-5,
        false);

    return (star.cadmodeler.SolidModelPart)
        simulation.get(SimulationPartManager.class).getPart(designName);
  }

  private void createComparisonSection(
      Simulation simulation, Scene geometryScene, star.cadmodeler.SolidModelPart part, String designName) {
    star.vis.PlaneSection section =
        (star.vis.PlaneSection)
            simulation
                .getPartManager()
                .createImplicitPart(
                    new ArrayList<>(Collections.<NamedObject>emptyList()),
                    SECTION_NORMAL,
                    SECTION_ORIGIN,
                    0,
                    1,
                    new DoubleVector(new double[] {0.0}),
                    null);

    section.setCoordinateSystem(
        simulation.getCoordinateSystemManager().getLabCoordinateSystem());
    section.getInputParts().setObjects(part.getPartSurfaceManager().getPartSurfaces());
    section.setPresentationName(designName);

    PartDisplayer displayer =
        geometryScene.getDisplayerManager().createPartDisplayer("Section Surface", -1, 1);
    displayer.setPresentationName(designName);
    displayer.setColorMode(PartColorMode.CONSTANT);
    displayer.setDisplayerColor(new DoubleVector(new double[] {0.0, 1.0, 0.0}));
    displayer.getVisibleParts().addParts(section);
  }
}
