# cpe-views

A batch macro that works on the active simulation, which already holds a converged solution. By its own header it creates new scenes and sections with the prefix `cpe_`, exports three PNG images, and does not save the `.sim`.

## It does not compile

`javac` returns exit code 1 with three errors and one warning. All three errors are the same symbol, `star.common.NamedObject`, which the source writes out in full at line 287 (once) and at line 290 (twice). The compiler reports that the package `star.common` has no class of that name. `NamedObject` is declared in `star.base.neo`, as the `wall-yplus-audit` specimen shows.

## The same symbol, a different cause

`wall-yplus-audit` fails on `NamedObject` because an import is missing. Here no import is missing: the source reaches for the class by its full name and names the wrong package. The class exists. The package is the part that is wrong.

## The warning

The compiler also reports one warning, at line 202: `setValue(double)` on `ParallelScale` is deprecated and marked for removal. A warning does not stop the build, so this call would still compile once the three errors are gone.

## What was removed before publication

The file in this folder is not the file as the model wrote it. Comments were removed, and nothing else:

- line 1 of the original, an internal label;
- lines 5 to 78 of the original, a comment block that listed where each call was taken from;
- 14 comment lines in the body that cited files of private macro repositories, among them the note on the deprecated call at line 202.

No line of code changed. The transcript in `javac.txt` was captured again on the edited file, so its line numbers match this source. The verdict of the compiler is the same as on the original: the same three errors on the same symbol, 86 lines higher, and the same warning, 85 lines higher.

- Original SHA256: `04a5981ef282ea8d71489b7d5fbe3958e512fa96d01f56c0162587e825c741f5`
- Published SHA256: `38e585501fe654e75487f9c556575c47fb1d26b4765bddaca3e4b27309af54b5`

## Where the calls come from

The removed header said that a macro repository was searched first and the server used only where noted. The index says so in its provenance column.

## What this specimen does not show

It stops at the compiler. Nothing here says what the macro does when it runs, and the source is not repaired in this folder.

## Citation

Reserved for Agentic Macros, Part 2, which is not published yet. The index row reads `none (reserved, Agentic Macros)` until it is.
