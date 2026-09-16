// PlaneTableExportRef.java
//
// STAR-CCM+ Java macro.
// Resolves a region and three boundaries by name, builds an X-normal plane
// section through them, samples that plane with an internal table carrying
// Velocity Magnitude and Pressure, then extracts and exports the table to CSV.
//
// Every class/method used here was grounded against the STAR-CCM+ Java API
// Javadoc (see the grounding log accompanying this file). Nothing was written
// from memory.

package macro;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import star.base.neo.DoubleVector;
import star.common.Boundary;
import star.common.FieldFunction;
import star.common.Region;
import star.common.Simulation;
import star.common.StarMacro;
import star.common.XyzInternalTable;
import star.vis.PlaneSection;

public class PlaneTableExportRef extends StarMacro {

    // =====================================================================
    // ==========================  USER SETTINGS  ==========================
    // Everything simulation-specific lives here. To reuse this macro on
    // another simulation, change ONLY the constants in this block.
    // =====================================================================

    // --- Region (by name) ---
    private static final String REGION_NAME = "Fluid";

    // --- Boundaries (by name), used together as the plane's input parts ---
    private static final String BOUNDARY_INLET_NAME  = "Inlet";
    private static final String BOUNDARY_OUTLET_NAME = "Outlet";
    private static final String BOUNDARY_WALL_NAME   = "Wall";

    // --- Plane definition ---
    // Normal = X axis. Values are in SI units (metres): createImplicitPart
    // takes no Units argument, so these are interpreted in metres.
    private static final double[] PLANE_NORMAL = { 1.0, 0.0, 0.0 };
    // Position of the plane along X. MUST fall inside the block. For the
    // reference geometry the block spans X in [0, 1] m, so the mid-length
    // cut is at X = 0.5 m. Adjust to your geometry.
    private static final double   PLANE_POSITION_X = 0.5; // metres
    // The plane origin (a point the plane passes through). Placed at
    // (PLANE_POSITION_X, 0, 0) so, together with the X normal, the plane is
    // the X = PLANE_POSITION_X cut.
    private static final double[] PLANE_ORIGIN = { PLANE_POSITION_X, 0.0, 0.0 };

    // --- Field functions (by name) ---
    // Base VECTOR velocity function; its magnitude is taken on the table.
    private static final String VELOCITY_FUNCTION_NAME = "Velocity";
    // Scalar pressure function.
    private static final String PRESSURE_FUNCTION_NAME = "Pressure";

    // --- Output ---
    private static final String OUTPUT_CSV_PATH =
            "<your-output-dir>/plane_export.csv";
    private static final String CSV_DELIMITER = ","; // comma-separated

    // =====================================================================
    // =======================  END USER SETTINGS  =========================
    // =====================================================================

    @Override
    public void execute() {

        // 1) Active simulation.
        //    star.common.StarMacro.getActiveSimulation()
        Simulation sim = getActiveSimulation();

        // 2) Resolve the region by name.
        //    star.common.Simulation.getRegionManager()
        //    star.common.RegionManager.getRegion(String)
        Region region = sim.getRegionManager().getRegion(REGION_NAME);

        // 3) Resolve the boundaries by name (from that region's boundary manager).
        //    star.common.Region.getBoundaryManager()
        //    star.common.BoundaryManager.getBoundary(String)
        Boundary inlet  = region.getBoundaryManager().getBoundary(BOUNDARY_INLET_NAME);
        Boundary outlet = region.getBoundaryManager().getBoundary(BOUNDARY_OUTLET_NAME);
        Boundary wall   = region.getBoundaryManager().getBoundary(BOUNDARY_WALL_NAME);

        // 4) Collect the named objects to be the plane's INPUT PARTS.
        //    Region and Boundary both extend star.base.neo.NamedObject.
        List<Object> inputParts = new Vector<Object>();
        inputParts.add(region);
        inputParts.add(inlet);
        inputParts.add(outlet);
        inputParts.add(wall);

        // 5) Create the plane section (a derived part) with X normal at the
        //    given origin.
        //    star.common.Simulation.getPartManager()
        //    star.common.PartManager.createImplicitPart(List inputs,
        //                                               List orientation,
        //                                               List origin)
        //      -> returns star.vis.PlaneSection
        //    'orientation' is the plane Normal (PlaneSection.getOrientationInput()
        //    is annotated display="Normal"); 'origin' is a point on the plane.
        DoubleVector orientation = new DoubleVector(PLANE_NORMAL);
        DoubleVector origin      = new DoubleVector(PLANE_ORIGIN);
        PlaneSection plane = (PlaneSection) sim.getPartManager()
                .createImplicitPart(inputParts, orientation, origin);

        // (Redundant but explicit: pin normal and origin on the created plane.)
        //    star.vis.PlaneSection.setOrientation(DoubleVector)
        //    star.vis.PlaneSection.setOrigin(DoubleVector)
        plane.setOrientation(new DoubleVector(PLANE_NORMAL));
        plane.setOrigin(new DoubleVector(PLANE_ORIGIN));

        // 6) Create an internal table that samples the plane.
        //    star.common.Simulation.getTableManager()
        //    star.common.TableManager.createTabularObject(Class) -> T extends TabularObject
        XyzInternalTable table = sim.getTableManager()
                .createTabularObject(XyzInternalTable.class);

        // 7) Attach the plane to the table as its part.
        //    star.common.InternalTable.getParts() -> star.common.PartGroup
        //    star.common.PartGroup.addPart(NamedObject)
        table.getParts().addPart(plane);

        // 8) Build the ordered field-function list: Velocity Magnitude, then Pressure.
        //    star.common.Simulation.getFieldFunctionManager()
        //    star.common.FieldFunctionManager.getFunction(String)
        FieldFunction velocity = sim.getFieldFunctionManager()
                .getFunction(VELOCITY_FUNCTION_NAME);
        //    Magnitude of the vector velocity function:
        //    star.common.FieldFunction.getMagnitudeFunction()
        FieldFunction velocityMagnitude = velocity.getMagnitudeFunction();

        FieldFunction pressure = sim.getFieldFunctionManager()
                .getFunction(PRESSURE_FUNCTION_NAME);

        List<FieldFunction> functions = new ArrayList<FieldFunction>();
        functions.add(velocityMagnitude); // first
        functions.add(pressure);          // second

        // 9) Put the field functions on the table (order preserved).
        //    star.common.FieldFunctionTable.setFieldFunctions(Collection<FieldFunction>)
        table.setFieldFunctions(functions);

        // 10) Extract the table data, then export to CSV with a comma delimiter.
        //    star.common.Table.extract()
        //    star.common.Table.export(String fileName, String delimiter)
        table.extract();
        table.export(OUTPUT_CSV_PATH, CSV_DELIMITER);

        sim.println("PlaneTableExportRef: exported plane table to " + OUTPUT_CSV_PATH);
    }
}
