// Standalone STAR-CCM+ 21.04 batch macro, operates on the currently active simulation, which
// already holds a converged solution. Does not initialize or run the solver. Does not save the
// .sim: this macro only creates new, cpe_-prefixed scenes/sections and exports three PNGs.

package macro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import star.base.neo.DoubleVector;
import star.common.Boundary;
import star.common.FieldFunction;
import star.common.PartManager;
import star.common.PartSurface;
import star.common.Region;
import star.common.ScalarPhysicalQuantity;
import star.common.Simulation;
import star.common.StarMacro;
import star.vis.AutoRangeMode;
import star.vis.BackgroundColorMode;
import star.vis.ClipMode;
import star.vis.DisplayerBase;
import star.vis.Legend;
import star.vis.LegendOrientation;
import star.vis.ParallelScale;
import star.vis.PlaneSection;
import star.vis.ScalarDisplayer;
import star.vis.ScalarFillMode;
import star.vis.Scene;
import star.vis.SolidBackgroundColor;
import star.vis.VisProjectionMode;

import java.awt.Color;
import java.io.File;

public class cpe_views extends StarMacro {

    private static final String FLUID_REGION_NAME = "fluid";
    private static final String[] CUBE_BOUNDARY_PREFIX = { "cube_" };
    private static final String FIELD_NAME = "cp";

    private static final int IMAGE_WIDTH = 1600;
    private static final int IMAGE_HEIGHT = 1000;
    private static final int IMAGE_MAGNIFICATION = 1;
    private static final int COLOR_BAR_LEVELS = 256;
    private static final int COLOR_BAR_LABELS = 5;
    private static final String COLOR_BAR_LABEL_FORMAT = "%.2f";

    private static final String VIEW1_NAME = "vertex_face";
    private static final double[] VIEW1_RANGE = { 0.0, 2.4 };
    private static final double[] VIEW1_FOCAL = { -0.5, -0.45, 0.45 };
    private static final double[] VIEW1_POSITION = { -2.0, -0.45, 0.45 };
    private static final double[] VIEW1_VIEW_UP = { 0.0, 0.0, 1.0 };
    private static final double VIEW1_SCALE = 0.12;

    private static final String VIEW2_NAME = "vertex_diag";
    private static final double[] VIEW2_RANGE = { -2.6, 2.4 };
    private static final double[] VIEW2_PLANE_ORIGIN = { 0.0, 0.0, 0.0 };
    private static final double[] VIEW2_PLANE_NORMAL = { 0.0, 1.0, 1.0 };
    private static final double[] VIEW2_FOCAL = { -0.5, -0.5, 0.5 };
    private static final double[] VIEW2_VIEW_UP = normalize(new double[] { 0.0, -1.0, 1.0 });
    private static final double VIEW2_SCALE = 0.12;
    private static final double VIEW2_CAMERA_OFFSET = 2.0;

    private static final String VIEW3_NAME = "edge_y0";
    private static final double[] VIEW3_RANGE = { -2.6, 2.4 };
    private static final double[] VIEW3_PLANE_ORIGIN = { 0.0, 0.0, 0.0 };
    private static final double[] VIEW3_PLANE_NORMAL = { 0.0, 1.0, 0.0 };
    private static final double[] VIEW3_FOCAL = { -0.45, 0.0, 0.5 };
    private static final double[] VIEW3_VIEW_UP = { 0.0, 0.0, 1.0 };
    private static final double VIEW3_SCALE = 0.15;
    private static final double VIEW3_CAMERA_OFFSET = 2.0;

    private Simulation sim;

    @Override
    public void execute() {
        sim = getActiveSimulation();

        Region fluid = fluidRegion();
        FieldFunction cp = fieldFunction(FIELD_NAME);

        exportFaceView(fluid, cp);
        exportPlaneView(VIEW2_NAME, fluid, cp, VIEW2_PLANE_ORIGIN, VIEW2_PLANE_NORMAL, VIEW2_RANGE,
            VIEW2_FOCAL, VIEW2_VIEW_UP, VIEW2_SCALE, VIEW2_CAMERA_OFFSET);
        exportPlaneView(VIEW3_NAME, fluid, cp, VIEW3_PLANE_ORIGIN, VIEW3_PLANE_NORMAL, VIEW3_RANGE,
            VIEW3_FOCAL, VIEW3_VIEW_UP, VIEW3_SCALE, VIEW3_CAMERA_OFFSET);

        log("done");
    }

    // ---- view 1: vertex_face, the 6 cube_* boundaries, surface mesh visible ------------------
    private void exportFaceView(Region fluid, FieldFunction cp) {
        String sceneName = "cpe_scene_" + VIEW1_NAME;
        Scene scene = createScene(sceneName);

        String dispName = "cpe_disp_" + VIEW1_NAME;
        ScalarDisplayer sd = createScalarDisplayer(scene, dispName, cp, VIEW1_RANGE);

        List<PartSurface> surfaces = cubeBoundarySurfaces(fluid);
        for (PartSurface ps : surfaces) {
            addPartOnce(dispName, sd, ps, ps.getPresentationName());
        }

        styleScene(scene);
        setCamera(sceneName, scene, VIEW1_FOCAL, VIEW1_POSITION, VIEW1_VIEW_UP, VIEW1_SCALE);

        printView(scene, VIEW1_NAME);
    }

    // ---- views 2/3: a new plane section through the volume, volume mesh visible --------------
    private void exportPlaneView(String viewName, Region fluid, FieldFunction cp, double[] origin,
            double[] normal, double[] range, double[] focal, double[] viewUp, double scale, double offset) {
        String sceneName = "cpe_scene_" + viewName;
        Scene scene = createScene(sceneName);

        String dispName = "cpe_disp_" + viewName;
        ScalarDisplayer sd = createScalarDisplayer(scene, dispName, cp, range);

        PlaneSection section = createPlaneSection("cpe_section_" + viewName, fluid, origin, normal);
        addPartOnce(dispName, sd, section, section.getPresentationName());

        styleScene(scene);
        double[] position = {
            focal[0] + offset * normal[0],
            focal[1] + offset * normal[1],
            focal[2] + offset * normal[2]
        };
        setCamera(sceneName, scene, focal, position, viewUp, scale);

        printView(scene, viewName);
    }

    // ---- shared scene/displayer/style plumbing ------------------------------------------------

    private Scene createScene(String sceneName) {
        if (sim.getSceneManager().hasScene(sceneName) != null) {
            throw new RuntimeException("cpe_views: scene '" + sceneName + "' already exists.");
        }
        Scene scene = sim.getSceneManager().createScene();
        scene.setPresentationName(sceneName);
        log("created " + sceneName);
        return scene;
    }

    private ScalarDisplayer createScalarDisplayer(Scene scene, String dispName, FieldFunction cp, double[] range) {
        if (scene.getDisplayerManager().hasDisplayer(dispName)) {
            throw new RuntimeException("cpe_views: displayer '" + dispName + "' already exists.");
        }
        ScalarDisplayer sd = scene.getDisplayerManager().createScalarDisplayer(dispName);
        sd.setPresentationName(dispName);

        sd.setRepresentation(sim.getRepresentationManager().getDefaultFvRepresentation());
        sd.setFillMode(ScalarFillMode.NODE_FILLED);

        sd.getScalarDisplayQuantity().setFieldFunction(cp);
        sd.getScalarDisplayQuantity().setAutoRange(AutoRangeMode.NONE);
        sd.getScalarDisplayQuantity().setRange(new DoubleVector(range));
        sd.getScalarDisplayQuantity().setClip(ClipMode.NONE); // clamp: out of range keeps the end colour

        // TODO(unverified signature): ScalarDisplayer.setDisplayMeshBoolean(boolean); no repo precedent,
        // mcp: star/vis/ScalarDisplayer.html.
        sd.setDisplayMeshBoolean(true);

        Legend legend = sd.getLegend();
        legend.setLevels(COLOR_BAR_LEVELS);
        legend.setNumberOfLabels(COLOR_BAR_LABELS);
        legend.setLabelFormat(COLOR_BAR_LABEL_FORMAT);
        // TODO(unverified signature): Legend.setOrientation(LegendOrientation); no repo precedent,
        // mcp: star/vis/LegendOrientation.html.
        legend.setOrientation(LegendOrientation.LEGEND_ORIENTATION_HORIZONTAL);

        log("created " + dispName);
        return sd;
    }

    private void styleScene(Scene scene) {
        // TODO(unverified signature): Scene.setAxesVisible(boolean); mcp: star/vis/Scene.html.
        scene.setAxesVisible(false);
        // TODO(unverified signature): Scene.setBackgroundColorMode(BackgroundColorMode),
        // BackgroundColorMode.SOLID, Scene.getSolidBackgroundColor(),
        // SolidBackgroundColor.setColorColor(Color); mcp: star/vis/Scene.html,
        // star/vis/BackgroundColorMode.html, star/vis/SolidBackgroundColor.html.
        scene.setBackgroundColorMode(BackgroundColorMode.SOLID);
        SolidBackgroundColor bg = (SolidBackgroundColor) scene.getSolidBackgroundColor();
        bg.setColorColor(Color.WHITE);
    }

    private void setCamera(String sceneName, Scene scene, double[] focal, double[] position, double[] viewUp,
            double scale) {
        Object mode = scene.getCurrentView().getProjectionMode();
        if (!isParallel(mode)) {
            scene.getCurrentView().setProjectionMode(VisProjectionMode.PARALLEL);
        }

        scene.getCurrentView().setFocalPoint(new DoubleVector(focal));
        scene.getCurrentView().setPosition(new DoubleVector(position));
        scene.getCurrentView().setViewUp(new DoubleVector(viewUp));

        ((ParallelScale) scene.getCurrentView().getParallelScale()).setValue(scale);

        log(sceneName + " camera focal=" + vec(focal) + " position=" + vec(position) + " up=" + vec(viewUp)
            + " scale=" + scale);
    }

    private static boolean isParallel(Object mode) {
        return Integer.valueOf(1).equals(mode) || VisProjectionMode.PARALLEL.equals(mode)
            || (mode != null && "PARALLEL".equalsIgnoreCase(mode.toString()));
    }

    // ---- plane section, no repo precedent, built from star/vis/PlaneSection.html and
    // star/common/PartManager.html / Part.html -------------------------------------------------
    private PlaneSection createPlaneSection(String name, Region fluid, double[] origin, double[] normal) {
        PartManager partManager = sim.getPartManager();
        if (partManager.hasObject(name) != null) {
            throw new RuntimeException("cpe_views: part '" + name + "' already exists.");
        }
        // TODO(unverified signature): PartManager.createPlaneSection(), Part.setInputPartsCollection(Collection),
        // PlaneSection.setOrigin/setOrientation(DoubleVector); no repo precedent for any PlaneSection call,
        // mcp: star/common/PartManager.html, star/common/Part.html, star/vis/PlaneSection.html.
        PlaneSection section = partManager.createPlaneSection();
        section.setPresentationName(name);
        section.setInputPartsCollection(Collections.singletonList(fluid));
        section.setOrigin(new DoubleVector(origin));
        section.setOrientation(new DoubleVector(normal));
        log("created " + name);
        return section;
    }


    private Region fluidRegion() {
        Object o;
        try {
            o = sim.getRegionManager().getObject(FLUID_REGION_NAME);
        } catch (RuntimeException e) {
            o = null;
        }
        if (o == null) {
            throw new RuntimeException("cpe_views: missing region '" + FLUID_REGION_NAME + "'.");
        }
        if (!(o instanceof Region)) {
            throw new RuntimeException("cpe_views: object '" + FLUID_REGION_NAME + "' is not a Region.");
        }
        return (Region) o;
    }

    private FieldFunction fieldFunction(String name) {
        Object o = sim.getFieldFunctionManager().hasObject(name);
        if (o == null) {
            throw new RuntimeException("cpe_views: missing field function '" + name + "'.");
        }
        if (!(o instanceof FieldFunction)) {
            throw new RuntimeException("cpe_views: object '" + name + "' is not a FieldFunction.");
        }
        return (FieldFunction) o;
    }

    private List<PartSurface> cubeBoundarySurfaces(Region fluid) {
        List<PartSurface> surfaces = new ArrayList<PartSurface>();
        for (Boundary b : fluid.getBoundaryManager().getObjects()) {
            String name = b.getPresentationName();
            boolean isCube = false;
            for (String prefix : CUBE_BOUNDARY_PREFIX) {
                if (name.startsWith(prefix)) {
                    isCube = true;
                    break;
                }
            }
            if (!isCube) {
                continue;
            }
            Collection<PartSurface> ps = b.getPartSurfaceGroup().getObjects();
            if (ps == null || ps.isEmpty()) {
                throw new RuntimeException("cpe_views: boundary '" + name + "' has no part surfaces.");
            }
            surfaces.addAll(ps);
        }
        if (surfaces.size() != 6) {
            throw new RuntimeException("cpe_views: expected 6 cube_* part surfaces, found " + surfaces.size() + ".");
        }
        return surfaces;
    }

    private void addPartOnce(String owner, DisplayerBase disp, Object part, String label) {
        if (!(part instanceof star.common.NamedObject)) {
            throw new RuntimeException("cpe_views: part '" + label + "' for '" + owner + "' is not a NamedObject.");
        }
        star.common.NamedObject no = (star.common.NamedObject) part;
        if (disp.hasPart(no)) {
            return;
        }
        disp.addPart(no);
    }

    private void printView(Scene scene, String viewName) {
        String sessionDir = sim.getSessionDir();
        if (sessionDir == null || sessionDir.isEmpty()) {
            throw new RuntimeException("cpe_views: active simulation has no session directory.");
        }
        File out = new File(sessionDir, sim.getPresentationName() + "_" + viewName + ".png");
        try {
            scene.printAndWait(out.getAbsolutePath(), IMAGE_MAGNIFICATION, IMAGE_WIDTH, IMAGE_HEIGHT);
        } catch (RuntimeException e) {
            throw new RuntimeException("cpe_views: print of '" + viewName + "' failed: " + e.getMessage());
        }
        if (!out.isFile() || out.length() == 0L) {
            throw new RuntimeException("cpe_views: " + rel(out) + " was not written by '" + viewName + "'.");
        }
        log(rel(out) + " " + IMAGE_WIDTH + "x" + IMAGE_HEIGHT + " bytes=" + out.length());
    }

    private String rel(File f) {
        return f.getName();
    }

    private static double[] normalize(double[] v) {
        double len = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return new double[] { v[0] / len, v[1] / len, v[2] / len };
    }

    private static String vec(double[] v) {
        return "(" + v[0] + "," + v[1] + "," + v[2] + ")";
    }

    private void log(String msg) {
        sim.println("cpe: " + msg);
    }
}
