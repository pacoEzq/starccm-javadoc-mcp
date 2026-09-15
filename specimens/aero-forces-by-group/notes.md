# aero-forces-by-group

An aerodynamic force breakdown by wall group. It sorts wall boundaries into named groups by matching substrings of their names, builds a force report for each group, and prints the breakdown. It was written against the Javadoc through this server, and the source cites the page behind each call inline.

## It compiles clean

`javac` returns exit code 0 with no output at all. Recompiled with `-Xlint:deprecation` it is still silent, so unlike the plane table macro this one touches nothing marked old. The transcript here is two comment lines and nothing else, which is what a clean build looks like.

## The contrast with the first specimen

The first specimen in this folder fails to compile on `NamedObject`, a symbol its imports never reach. This macro uses the same package and builds, because it imports `star.base.neo.*` outright. Two macros written the same way, against the same API, and only one of them reaches the compiler intact. The difference is not in the grounding, which was equally correct in both, but in an import line that no API search asks about.

## What the compiler does not settle

A defect was recorded against this macro when it was first written: the drag component comes out with the wrong sign, traced to the flow direction vector. That finding is carried here from the working notes, not measured by this specimen. Nothing in this folder tests it, and no transcript here can: a sign error is arithmetic on a running case, three rungs above where a clean build stops. It is written down because a specimen that only says `compiles` should say plainly what it is not saying.

## Citation

No published post cites this macro. It was drafted as a third test for the final part of the first series and left out when that part was redesigned, held back as a possible appendix on physical failure in the Agentic Macros series. The index row therefore reads `none (reserved, Agentic Macros)`, and the reservation is recorded in the project coordination file as the contract requires.
