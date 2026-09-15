// WallYplusAudit.java
// ---------------------------------------------------------------------------
// A one-click wall y+ audit for Simcenter STAR-CCM+.
//
// It walks every wall boundary in the model, computes the area-averaged and
// maximum wall y+, and flags any wall outside the y+ band your turbulence
// wall treatment expects. Read-only: it makes two temporary reports and
// removes them at the end, leaving the simulation tree clean.
//
// WHY a band, not one number (this is the "intelligence"):
//   - Resolved sublayer / low-Re treatment: target max y+ ~ 1
//        -> YPLUS_MIN = 0,  YPLUS_MAX = 1
//   - Standard wall functions: target ~ 30 to 300
//        -> YPLUS_MIN = 30, YPLUS_MAX = 300
//   - All-y+ treatment is tolerant; widen the band to taste.
// The verdict uses BOTH metrics: a high max y+ catches local spikes that an
// area average hides.
//
// API status: every call below was checked against the STAR-CCM+ Javadoc.
//   - AreaAverageReport ("Surface Average") -> WeightedAverageReport -> ScalarReport
//   - MaxReport ("Maximum")                 -> MinMaxReportBase     -> ScalarReport
//   setFieldFunction / setObjects(Collection<NamedObject>) / getValue() are all
//   inherited from ScalarReport / AnalysisReport. createReport(Class) and
//   removeObjects(Collection) are on ReportManager. The wall test uses the
//   verified type WallBoundary (extends BoundaryType).
//   The only runtime-only name is the field function "WallYplus" (guarded below).
// ---------------------------------------------------------------------------

import java.util.*;
import star.common.*;
import star.base.report.*;          // AreaAverageReport, MaxReport, ReportManager

public class WallYplusAudit extends StarMacro {

    // --- the only thing you normally edit: the acceptable y+ band ---
    static final double YPLUS_MIN = 30.0;
    static final double YPLUS_MAX = 300.0;

    public void execute() {
        Simulation sim = getActiveSimulation();

        // Built-in wall y+ field function. Internal (function) name is "WallYplus".
        // It only exists when a turbulence model with wall treatment is active,
        // so fail with a clear message instead of an NPE.
        FieldFunction yplus = sim.getFieldFunctionManager().getFunction("WallYplus");
        if (yplus == null) {
            sim.println("WallYplusAudit: field function 'WallYplus' not found. "
                    + "Is a turbulence model with wall treatment active and the case solved?");
            return;
        }

        // Two reusable reports, retargeted to one wall at a time.
        AreaAverageReport avg = sim.getReportManager().createReport(AreaAverageReport.class);
        MaxReport         max = sim.getReportManager().createReport(MaxReport.class);
        avg.setFieldFunction(yplus);
        max.setFieldFunction(yplus);

        int walls = 0, flagged = 0;
        sim.println(String.format("%-34s %10s %10s   %s", "Wall boundary", "avg y+", "max y+", "verdict"));
        sim.println("---------------------------------------------------------------------");

        for (Region region : sim.getRegionManager().getRegions()) {
            for (Boundary b : region.getBoundaryManager().getBoundaries()) {
                if (!isWall(b)) continue;
                walls++;

                Collection<NamedObject> one = Collections.<NamedObject>singletonList(b);
                avg.setObjects(one);
                max.setObjects(one);

                double a = avg.getValue();
                double m = max.getValue();
                boolean ok = (a >= YPLUS_MIN) && (m <= YPLUS_MAX);
                if (!ok) flagged++;

                sim.println(String.format("%-34s %10.2f %10.2f   %s",
                        b.getPresentationName(), a, m, ok ? "OK" : "OUT OF BAND"));
            }
        }

        sim.println("---------------------------------------------------------------------");
        sim.println(String.format("Walls audited: %d    within [%.0f, %.0f]: %d    flagged: %d",
                walls, YPLUS_MIN, YPLUS_MAX, walls - flagged, flagged));

        // Leave the tree clean (comment out to keep the reports for monitoring).
        sim.getReportManager().removeObjects(Arrays.asList(avg, max));
    }

    // Wall test, grounded against the API: the boundary's type is a WallBoundary
    // (WallBoundary extends BoundaryType). Robust to renaming/localization.
    private boolean isWall(Boundary b) {
        return b.getBoundaryType() instanceof WallBoundary;
    }
}
