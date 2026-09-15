// PlaneTableExport.java
//
// STAR-CCM+ Java macro: cuts a Z-normal plane section through all regions and
// boundaries at (0.5, 0, 0) m, samples it with an XYZ internal table carrying
// four field functions, extracts and exports the table to CSV.
//
// Every API call below was grounded against the STAR-CCM+ Javadoc via the
// starccm MCP tools; see the grounding log in the accompanying message.
// Output path and the string field-function names are the only items that
// could NOT be verified by the tools and may need adjusting for your sim.

package macro;

import java.util.Arrays;
import java.util.Vector;

import star.base.neo.DoubleVector;
import star.common.Boundary;
import star.common.FieldFunction;
import star.common.FieldFunctionManager;
import star.common.Region;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.XyzInternalTable;
import star.vis.PlaneSection;

public class PlaneTableExport extends StarMacro {

  // NOTE: unverifiable by the API tools -- set to a path valid on the machine
  // running STAR-CCM+. Uses forward slashes so it is portable across OSes.
  private static final String OUTPUT_PATH =
      "<output-folder>/PlaneTableExport.csv";

  // NOTE: string field-function names cannot be verified against the API
  // (they depend on the loaded simulation). Adjust if your sim differs.
  private static final String FF_U_VELOCITY_AP    = "U_VelocityAp";
  private static final String FF_TANGENTIAL_VEL   = "TangentialVelocity";
  private static final String FF_VELOCITY         = "Velocity";   // magnitude taken from this
  private static final String FF_PRESSURE         = "Pressure";

  @Override
  public void execute() {

    // 1. Active simulation -- StarMacro.getActiveSimulation()
    Simulation sim = getActiveSimulation();

    // 2. Collect every region and every boundary as the cut inputs.
    //    Simulation.getRegionManager() -> RegionManager.getRegions()
    //    Region.getBoundaryManager()   -> BoundaryManager.getBoundaries()
    Vector<Object> inputParts = new Vector<>();
    for (Region region : sim.getRegionManager().getRegions()) {
      inputParts.add(region);
      for (Boundary boundary : region.getBoundaryManager().getBoundaries()) {
        inputParts.add(boundary);
      }
    }

    // 3. Create the plane section (a derived part).
    //    PartManager.createImplicitPart(List inputs, List orientation,
    //        List origin, int valueMode, int nvals, DoubleVector offsets)
    //    -> returns PlaneSection.
    //    orientation = plane normal (Z axis); origin = (0.5, 0, 0) m;
    //    valueMode = 0 (single value), nvals = 1, offsets = {0.0}.
    DoubleVector normal  = new DoubleVector(new double[] {0.0, 0.0, 1.0});
    DoubleVector origin  = new DoubleVector(new double[] {0.5, 0.0, 0.0});
    DoubleVector offsets = new DoubleVector(new double[] {0.0});

    PlaneSection plane = (PlaneSection) sim.getPartManager().createImplicitPart(
        inputParts, normal, origin, 0, 1, offsets);

    // 4. Create the internal table that samples the plane.
    //    Simulation.getTableManager() -> TableManager.createTable(Class)
    XyzInternalTable table =
        sim.getTableManager().createTable(XyzInternalTable.class);

    //    Attach the plane to the table.
    //    XyzInternalTable/InternalTable.getParts() -> PartGroup.addPart(NamedObject)
    table.getParts().addPart(plane);

    // 5. Put the field functions on the table, in order.
    //    getFunction(String) returns a NullFieldFunction on a miss — never null,
    //    never an exception — so missing names produce silent empty columns in the
    //    CSV. Check all four with hasFunction first; collect every absent name and
    //    abort in one message so the user sees all problems at once.
    //    FF_VELOCITY must be confirmed before chaining .getMagnitudeFunction().
    FieldFunctionManager ffm = sim.getFieldFunctionManager();
    java.util.List<String> missing = new java.util.ArrayList<>();
    if (!ffm.hasFunction(FF_U_VELOCITY_AP))  missing.add(FF_U_VELOCITY_AP);
    if (!ffm.hasFunction(FF_TANGENTIAL_VEL)) missing.add(FF_TANGENTIAL_VEL);
    if (!ffm.hasFunction(FF_VELOCITY))       missing.add(FF_VELOCITY);
    if (!ffm.hasFunction(FF_PRESSURE))       missing.add(FF_PRESSURE);
    if (!missing.isEmpty()) {
      sim.println("PlaneTableExport: field function(s) not found: " + missing
          + " — aborting. Check that the simulation has been solved and the"
          + " function names match your setup.");
      return;
    }
    FieldFunction uVelocityAp      = ffm.getFunction(FF_U_VELOCITY_AP);
    FieldFunction tangentialVel    = ffm.getFunction(FF_TANGENTIAL_VEL);
    FieldFunction velocityMagnitude = ffm.getFunction(FF_VELOCITY).getMagnitudeFunction();
    FieldFunction pressure         = ffm.getFunction(FF_PRESSURE);

    //    FieldFunctionTable.setFieldFunctions(Collection<FieldFunction>) -- order preserved.
    table.setFieldFunctions(Arrays.asList(
        uVelocityAp, tangentialVel, velocityMagnitude, pressure));

    // 6. Extract and export to CSV (comma separator).
    //    Table.extract() then Table.export(String fileName, String delimiter)
    table.extract();
    table.export(OUTPUT_PATH, ",");

    sim.println("PlaneTableExport: exported table to " + OUTPUT_PATH);
  }
}
