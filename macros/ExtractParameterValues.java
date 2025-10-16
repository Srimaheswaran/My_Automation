package macro;

import star.common.GlobalParameter;
import star.common.GlobalParameterManager;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.Units;

/**
 * Prints all scalar global parameters, including their values and associated units, to the console.
 */
public class ExtractParameterValues extends StarMacro {

  private static final String OUTPUT_TEMPLATE = "Parameter: %s | Value: %.6f | Units: %s";

  @Override
  public void execute() {
    Simulation simulation = getActiveSimulation();
    GlobalParameterManager parameterManager = simulation.get(GlobalParameterManager.class);

    for (GlobalParameter parameter : parameterManager.getGlobalParameters()) {
      if (parameter instanceof ScalarGlobalParameter) {
        printParameter((ScalarGlobalParameter) parameter);
      }
    }
  }

  private void printParameter(ScalarGlobalParameter parameter) {
    String name = parameter.getPresentationName();
    double value = parameter.getQuantity().getRawValue();
    Units units = parameter.getQuantity().getUnits();
    String unitsText = units != null ? units.getPresentationName() : "No Units";

    System.out.println(String.format(OUTPUT_TEMPLATE, name, value, unitsText));
  }
}
