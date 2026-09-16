# plane-table-export-ref

Two macros for one task, from different provenance. `grounded/` holds `PlaneTableExportRef.java`, written against the Javadoc through this server. `recorded/` holds `presVmag.java`, recorded in the STAR-CCM+ GUI. Both cut a plane section at X = 0.5 m through the region and its boundaries, sample it with an XYZ internal table carrying velocity magnitude and pressure, and export to CSV. They close Part 6 of the first series, which asks whether a grounded macro can match a recording.

## The result

Played on the same case, the two produce the same file. Both CSV are 11301 bytes, 137 lines (one header and 136 points), md5 `5ACD848CA55E759647CCA6E47EE5E959`. A binary compare of the two returns exit code 0. The claim of the post is byte for byte identity, and this is it.

Both also compile to exit code 0 with no output. Recompiled with `-Xlint:deprecation` both stay silent, so neither touches API marked old. That separates this pair from the earlier `plane-table-export`, whose published state calls a deprecated table factory.

## Two machines, declared

This specimen was executed and compiled on different machines, and says so rather than picking one.

The run is Linux: Simcenter STAR-CCM+ 2606 Build 21.04.007 (linux-x86_64-2.28/clang20.1-r8), on `testFluid.sim` (4815590 bytes, md5 `F79FA55666F44C4B0FCAF069AD239546`), a single region of 10400 cells with an inlet, an outlet and a wall. The case file is not in this folder; it ships as an attachment to the post.

The transcripts are Windows: `javac` 25.0.1 from the JDK inside Simcenter STAR-CCM+ 2606 Build 21.04.007. Nothing here inherits a reference machine, so both are named in full and neither stands for the other.

One detail the source cannot settle: the header of `presVmag.java` records the version as 21.04.007, without the `-r8` suffix and without the precision. The full build string comes from the run, not from the file.

## What the recording writes and the grounded macro does not

The point of the pair is that identical output does not mean identical code. Measured on these two files:

| Step | `recorded/presVmag.java` | `grounded/PlaneTableExportRef.java` |
|---|---|---|
| Create the table | `create("star.common.XyzInternalTable")` (line 50) | `createTabularObject(XyzInternalTable.class)` (line 122) |
| Attach the plane | `getParts().setObjects(planeSection_0)` (line 54) | `getParts().addPart(plane)` (line 127) |
| Set the plane origin | `getOriginCoordinate().setCoordinate(units, ...)` (line 45) | `setOrigin(new DoubleVector(...))` (line 116) |
| Hold a field function | casts to `PrimitiveFieldFunction` (lines 57 and 63) | keeps `FieldFunction` |
| Choose the input parts | `getInputParts().setObjects(region, boundaries)` (line 40) | absent |
| Reset a part group | `getParts().setQuery(null)` (lines 26 and 52) | absent |

The first four are the divergences the post tabulates. The last two are not in that table and are recorded here because they were measured: the recording carries six call sites the grounded macro never emits, not four. A part group reset to a null query has no counterpart at all in a macro written from the documentation, because nothing on the page suggests it.

## A declared redaction

Both files carry a redaction under the rule this repository added on 2026-09-07. In `grounded/PlaneTableExportRef.java` it is line 64, the output CSV path; in `recorded/presVmag.java` it is line 69, the argument of the export call. Both were replaced by the placeholder `<your-output-dir>`, and both are string literals that no other line reads at compile time.

| File | Source of record | Bytes | md5 | Published | Bytes | md5 |
|---|---|---|---|---|---|---|
| `PlaneTableExportRef.java` | run copy | 7467 | `B76B6FA7D4A956CBEAC88F61B4D3EBBC` | this folder | 7441 | `5E3E8E016A488D7F7F3773434AC994CC` |
| `presVmag.java` | run copy | 2513 | `E25A67BD89561F81D25EDA0CD887E584` | this folder | 2487 | `01F1802C7B9C0BE27F7D33C699E34709` |

Each redaction changes one line and one line only, verified by diff. Two further copies of the grounded macro exist with other output paths; they differ from the run copy on the same line and nowhere else, so they are the same specimen with a different destination, not another version.

## Why the run logs are not here

The two STAR-CCM+ output logs sit on the machine that produced them and stay there. They carry the license server address, the internal host name of the machine, the corporate installation path and the user's home directory, none of which belong in a public repository. Redacting them was rejected: the rule this repository added permits redacting a literal in a source that takes no part in compilation, and a run log is not a source, it is the evidence itself. Editing evidence to publish it defeats the purpose of keeping it.

Nothing is lost by the omission. What the logs would support, that both macros ran to completion on the declared case, is already carried by the two CSV files and their identity.
