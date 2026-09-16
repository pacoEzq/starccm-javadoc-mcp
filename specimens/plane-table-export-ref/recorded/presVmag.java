// Simcenter STAR-CCM+ macro: presVmag.java
// Written by Simcenter STAR-CCM+ 21.04.007
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.vis.*;

public class presVmag extends StarMacro {

  @Override
  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    PlaneSection planeSection_0 = 
      (PlaneSection) simulation_0.getPartManager().createImplicitPart(new ArrayList<>(Collections.<NamedObject>emptyList()), new DoubleVector(new double[] {0.0, 0.0, 1.0}), new DoubleVector(new double[] {0.0, 0.0, 0.0}), 0, 1, new DoubleVector(new double[] {0.0}), null);

    planeSection_0.getInputParts().setQuery(null);

    Region region_0 = 
      simulation_0.getRegionManager().getRegion("Fluid");

    Boundary boundary_0 = 
      region_0.getBoundaryManager().getBoundary("Inlet");

    Boundary boundary_1 = 
      region_0.getBoundaryManager().getBoundary("Outlet");

    Boundary boundary_2 = 
      region_0.getBoundaryManager().getBoundary("Wall");

    planeSection_0.getInputParts().setObjects(region_0, boundary_0, boundary_1, boundary_2);

    Units units_0 = 
      ((Units) simulation_0.getUnitsManager().getObject("m"));

    planeSection_0.getOriginCoordinate().setCoordinate(units_0, units_0, units_0, new DoubleVector(new double[] {0.5, 0.0, 0.0}));

    planeSection_0.getOrientationCoordinate().setCoordinate(units_0, units_0, units_0, new DoubleVector(new double[] {1.0, 0.0, 0.0}));

    XyzInternalTable xyzInternalTable_0 = 
      simulation_0.getTableManager().create("star.common.XyzInternalTable");

    xyzInternalTable_0.getParts().setQuery(null);

    xyzInternalTable_0.getParts().setObjects(planeSection_0);

    PrimitiveFieldFunction primitiveFieldFunction_0 = 
      ((PrimitiveFieldFunction) simulation_0.getFieldFunctionManager().getFunction("Velocity"));

    VectorMagnitudeFieldFunction vectorMagnitudeFieldFunction_0 = 
      ((VectorMagnitudeFieldFunction) primitiveFieldFunction_0.getMagnitudeFunction());

    PrimitiveFieldFunction primitiveFieldFunction_1 = 
      ((PrimitiveFieldFunction) simulation_0.getFieldFunctionManager().getFunction("Pressure"));

    xyzInternalTable_0.setFieldFunctions(new ArrayList<>(Arrays.<FieldFunction>asList(vectorMagnitudeFieldFunction_0, primitiveFieldFunction_1)));

    xyzInternalTable_0.extract();

    xyzInternalTable_0.export("<your-output-dir>/presVmag.csv", ",");
  }
}
