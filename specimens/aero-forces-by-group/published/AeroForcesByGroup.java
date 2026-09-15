// AeroForcesByGroup.java
//
// Simcenter STAR-CCM+ macro: aerodynamic force breakdown by wall group.
//
// Every class/method used is grounded on the STAR-CCM+ Javadoc (get_doc pages
// are cited inline as [grounded: <ClassPage>#<method>]). See the grounding log
// that accompanies this file for the full table.
package macro;

import java.util.*;

import star.base.neo.*;      // NamedObject
import star.base.report.*;   // ReportManager
import star.common.*;        // StarMacro, Simulation, Region, Boundary, WallBoundary,
                             // BoundaryManager, VectorPhysicalQuantity
import star.flow.*;          // ForceReport

public class AeroForcesByGroup extends StarMacro {

    // Group name substrings, matched case-insensitively in priority order.
    // Anything that matches none of these falls into "Other".
    private static final String[] GROUP_KEYS = { "Fuselage", "Canopy", "Duct", "Arm" };
    private static final String OTHER = "Other";

    public void execute() {
        // StarMacro.getActiveSimulation() -> Simulation
        // [grounded: star/common/StarMacro.html#getActiveSimulation()]
        Simulation sim = getActiveSimulation();

        // ------------------------------------------------------------------
        // 1) Collect every WALL boundary across all regions.
        //    Simulation.getRegionManager() -> RegionManager
        //      [grounded: star/common/Simulation.html#getRegionManager()]
        //    RegionManager.getRegions() -> Collection<Region>
        //      [grounded: star/common/RegionManager.html#getRegions()]
        //    Region.getBoundaryManager() -> BoundaryManager
        //      [grounded: star/common/Region.html#getBoundaryManager()]
        //    BoundaryManager.getBoundaries() -> Collection<Boundary>
        //      [grounded: star/common/BoundaryManager.html#getBoundaries()]
        //    Boundary.getBoundaryType() -> BoundaryType, and
        //      WallBoundary extends BoundaryType, so a wall is identified by
        //      an instanceof check.
        //      [grounded: star/common/Boundary.html#getBoundaryType(),
        //                 star/common/WallBoundary.html (extends BoundaryType)]
        // ------------------------------------------------------------------
        List<Boundary> allWalls = new ArrayList<Boundary>();
        for (Region region : sim.getRegionManager().getRegions()) {
            for (Boundary b : region.getBoundaryManager().getBoundaries()) {
                if (b.getBoundaryType() instanceof WallBoundary) {
                    allWalls.add(b);
                }
            }
        }

        if (allWalls.isEmpty()) {
            sim.println("AeroForcesByGroup: no wall boundaries found - nothing to do.");
            return;
        }

        // ------------------------------------------------------------------
        // 2) Group walls by case-insensitive name substring.
        //    Boundary.getPresentationName() (from NamedObject).
        //      [grounded: star/base/neo/NamedObject#getPresentationName(),
        //                 inherited by star/common/Boundary.html]
        //    LinkedHashMap keeps a stable table order.
        // ------------------------------------------------------------------
        LinkedHashMap<String, List<Boundary>> groups = new LinkedHashMap<String, List<Boundary>>();
        for (String k : GROUP_KEYS) {
            groups.put(k, new ArrayList<Boundary>());
        }
        groups.put(OTHER, new ArrayList<Boundary>());

        for (Boundary b : allWalls) {
            String name = b.getPresentationName().toLowerCase();
            String matched = OTHER;
            for (String k : GROUP_KEYS) {
                if (name.contains(k.toLowerCase())) {
                    matched = k;
                    break;
                }
            }
            groups.get(matched).add(b);
        }

        // ------------------------------------------------------------------
        // 3) Create the three force reports ONCE and fix their directions.
        //    ReportManager.createReport(Class<T>) -> T extends Report
        //      [grounded: star/base/report/ReportManager.html#createReport(java.lang.Class)]
        //
        //    Direction-vector setter:
        //      VectorReport.getDirection() -> VectorPhysicalQuantity
        //        [grounded: star/base/report/VectorReport.html#getDirection()]
        //      VectorPhysicalQuantity.setComponents(double,double,double)
        //        [grounded: star/common/VectorPhysicalQuantity.html#setComponents(double,double,double)]
        //    Directions are interpreted in the report's coordinate system,
        //    VectorReport.getCoordinateSystem() (default = Laboratory global CSYS).
        //        [grounded: star/base/report/VectorReport.html#getCoordinateSystem()]
        //
        //    AXIS CONVENTION ASSUMED (see notes below the code):
        //      +X = streamwise (freestream)  -> force along +X is DRAG
        //      +Y = lateral                  -> force along +Y is SIDE
        //      +Z = vertical (up)            -> force along +Z is LIFT
        // ------------------------------------------------------------------
        ReportManager repMgr = sim.getReportManager();
        ForceReport fxReport = repMgr.createReport(ForceReport.class); // drag  (X)
        ForceReport fyReport = repMgr.createReport(ForceReport.class); // side  (Y)
        ForceReport fzReport = repMgr.createReport(ForceReport.class); // lift  (Z)

        fxReport.setPresentationName("AeroForce_X_Drag");
        fyReport.setPresentationName("AeroForce_Y_Side");
        fzReport.setPresentationName("AeroForce_Z_Lift");

        fxReport.getDirection().setComponents(1.0, 0.0, 0.0); // streamwise -> drag
        fyReport.getDirection().setComponents(0.0, 1.0, 0.0); // lateral    -> side
        fzReport.getDirection().setComponents(0.0, 0.0, 1.0); // vertical   -> lift

        // ------------------------------------------------------------------
        // 4) Whole-model totals (all walls together).
        // ------------------------------------------------------------------
        double[] whole = forceXYZ(allWalls, fxReport, fyReport, fzReport);
        double totalMag = magnitude(whole);

        // ------------------------------------------------------------------
        // 5) Per-group forces + each group's % of the total force magnitude.
        // ------------------------------------------------------------------
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Aerodynamic forces by wall group ===\n");
        sb.append(String.format("%-14s %15s %15s %15s %15s %12s%n",
                "Group", "Fx=Drag [N]", "Fy=Side [N]", "Fz=Lift [N]", "|F| [N]", "% of |Ftot|"));
        sb.append("--------------------------------------------------------------------------------------------\n");

        for (Map.Entry<String, List<Boundary>> e : groups.entrySet()) {
            List<Boundary> walls = e.getValue();
            if (walls.isEmpty()) {
                continue; // skip empty groups (e.g. "Other" when everything matched)
            }
            double[] f = forceXYZ(walls, fxReport, fyReport, fzReport);
            double mag = magnitude(f);
            double pct = (totalMag != 0.0) ? 100.0 * mag / totalMag : 0.0;
            sb.append(String.format("%-14s %15.4f %15.4f %15.4f %15.4f %11.2f%%%n",
                    e.getKey(), f[0], f[1], f[2], mag, pct));
        }

        sb.append("--------------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-14s %15.4f %15.4f %15.4f %15.4f %11.2f%%%n",
                "WHOLE MODEL", whole[0], whole[1], whole[2], totalMag, 100.0));
        sb.append("Note: group percentages are |F_group| / |F_total|. They need NOT sum to 100% because\n");
        sb.append("      the per-group force vectors partially cancel when summed into the whole-model vector.\n");

        sim.println(sb.toString());

        // ------------------------------------------------------------------
        // 6) Cleanup - left commented out on purpose.
        //    ReportManager inherits removeObjects(Collection) from
        //    ClientServerObjectManager.
        //      [grounded: star/base/report/ReportManager.html -> inherited
        //                 removeObjects(java.util.Collection)]
        // ------------------------------------------------------------------
        // sim.getReportManager().removeObjects(Arrays.asList(fxReport, fyReport, fzReport));
    }

    /**
     * Retarget the three pre-built reports onto {@code walls} and read the
     * force along each fixed direction.
     *
     * Parts assignment / retarget:
     *   AnalysisReport.setObjects(Collection<NamedObject>)
     *     [grounded: star/base/report/AnalysisReport.html#setObjects(java.util.Collection)]
     *   Boundary IS-A NamedObject (Boundary -> ... -> NamedObject), so a
     *   Collection of Boundary satisfies Collection<NamedObject>.
     *
     * Value accessor:
     *   VectorReport.getReportMonitorValue() -> double (report units, N)
     *     [grounded: star/base/report/VectorReport.html#getReportMonitorValue()]
     */
    private double[] forceXYZ(List<Boundary> walls, ForceReport fx, ForceReport fy, ForceReport fz) {
        Collection<NamedObject> parts = new ArrayList<NamedObject>(walls);
        fx.setObjects(parts);
        fy.setObjects(parts);
        fz.setObjects(parts);
        return new double[] {
                fx.getReportMonitorValue(),
                fy.getReportMonitorValue(),
                fz.getReportMonitorValue()
        };
    }

    private double magnitude(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }
}
