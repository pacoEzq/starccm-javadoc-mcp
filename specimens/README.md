# Specimens

A specimen is a macro that was actually compiled against a declared build of Simcenter STAR-CCM+, together with the record of what happened. Specimens are evidence, not examples. A macro that fails to compile earns its place here exactly as much as one that runs, because the record of the failure is the point.

Nothing in this folder inherits the reference machine declared in `bench/README.md`. Every specimen declares the STAR-CCM+ build it was compiled against and the compiler that produced its transcript, because a specimen means nothing apart from that pair.

## Contract

A specimen is complete when it ships all five of these. An entry missing any one of them is not a specimen and does not go in the index.

1. **Source.** The `.java` file exactly as it was compiled. Not a cleaned copy.
2. **Build.** The Simcenter STAR-CCM+ version and build the specimen was compiled against, written in full, for example `2606 Build 21.04.007`.
3. **Compiler transcript.** The raw `javac` output as `javac.txt`. The compiler is the JDK that ships inside the STAR-CCM+ installation, under `<STAR-CCM+ install>\jdk\...\bin\javac.exe`, invoked by its full path and never through `PATH`, so a transcript can never come from some other compiler that happens to be on the machine. The first two lines of the file are comments giving the `javac` version and the library root the classpath pointed at. A transcript without its compiler version proves nothing about any other machine.
4. **Outcome.** One of the four levels below, chosen by what was actually observed, never by what was expected.
5. **Use.** A link to the post, the section of a post, or the issue that cites this specimen. A specimen nobody cites is a working file, not a specimen. A specimen may instead declare `none (reserved)` when its citation is decided for a future publication and that reservation is recorded in the project coordination file. The reservation is named in the row, for example `none (reserved, Agentic Macros)`. A reserved row enters the index; a row with no citation at all still does not. A reservation expires: once the series that reserved it is published without citing the specimen, the row leaves the index and the specimen goes back to being a working file.

## Outcome levels

The four levels are a ladder. Each rung catches a class of failure the rung below cannot see, and no rung implies the one above it.

| Level | What was observed | What it does not establish |
|---|---|---|
| `does-not-compile` | `javac` returned errors, transcript included | nothing beyond the errors listed |
| `compiles` | `javac` returned no errors | that the macro loads or does anything |
| `runs` | the macro ran to completion in STAR-CCM+ on a declared case | that the numbers it produced are right |
| `correct` | a human checked the output against a declared case and accepted it | that it is correct on any other case |

`correct` is never asserted by a tool. It is a person's judgement about one named case, and the specimen says which case.

## Layout

One folder per specimen, named after the macro in lower case with hyphens. Inside it, one folder per state:

```
specimens/
  wall-yplus-audit/
    published/
      WallYplusAudit.java
      javac.txt
    fixed/
      WallYplusAudit.java
      javac.txt
    notes.md
```

The state is carried by the folder, never by a suffix on the file name. A public Java class has to live in a file named after the class, so a name like `WallYplusAudit.published.java` makes `javac` emit an error about the file name that has nothing to do with the macro. That error was actually observed on the first specimen, and it is why this layout is the way it is.

`published/` is the artifact exactly as it went out to readers. `fixed/` is the working copy that repairs it. Either folder may stand alone: a specimen that was correct when published has no `fixed/`, and one that has never been repaired has no `fixed/` yet.

A second axis exists for a specimen that is a pair of macros for one task rather than one macro in two states: `grounded/` holds the macro written against the Javadoc through this server and `recorded/` holds the macro recorded in the STAR-CCM+ GUI. The two axes are not interchangeable. `published` and `fixed` are states of one file; `grounded` and `recorded` are two files of different provenance. A specimen uses one axis or the other, never both in the same folder.

`notes.md` is optional and holds analysis: what the defect is, why the fix works, what the transcript means. It never holds metadata, which lives in the index below and nowhere else. Every claim in `notes.md` is measured before it is written, including claims about file names and about where a fix was recorded. Both of those were asserted from memory on the first specimen and both were wrong.

## Rules

**A published specimen is never repaired in place.** A defect found in published material is the finding. Repairing the file destroys it. The repair goes in the `fixed/` folder and both states stay, with their two transcripts, so the difference is readable.

**Provenance is declared.** A macro is either recorded in the STAR-CCM+ GUI or written against the Javadoc through this server. The index says which. The two produce different code for the same task, and that difference is one of the things this repository exists to show.

**No machine paths and no Siemens documentation.** Specimens carry no user names, no absolute paths, no copied Javadoc pages. Use the placeholder form for any path a reader has to replace.

**A machine path in the source is redacted, and the redaction is declared.** A specimen carries no user names, so a literal such as an output folder is replaced by its placeholder form. This is permitted only for a literal that takes no part in compilation. The transcript is captured again after the change, and `notes.md` records the line, the original SHA256 and the new one. Any edit that could alter what the compiler sees is not a redaction, and a file needing one is not a specimen.

**A defect in the server is an issue, not a comment.** If a specimen exists because the server returned a name that does not resolve, a signature that misleads, or a page that was cut, open an issue in this repository and link it from the index row. The issue is the part readers of the series can see.

**A specimen is added in its own commit**, with the transcript, so the record and the evidence arrive together.

## Index

| Specimen | State | Build | javac | Outcome | Provenance | Used by |
|---|---|---|---|---|---|---|
| `wall-yplus-audit` | published | 2606 Build 21.04.007 | 25.0.1 | `does-not-compile` | written against the Javadoc through this server | Series 1, Part 6 |
| `wall-yplus-audit` | fixed | 2606 Build 21.04.007 | 25.0.1 | `compiles` | same source, repaired by hand | Series 1, Part 6 |
| `plane-table-export` | published | 2606 Build 21.04.007 | 25.0.1 | `compiles` | written against the Javadoc through this server | none (reserved, Agentic Macros) |
| `plane-table-export` | fixed | 2606 Build 21.04.007 | 25.0.1 | `compiles` | same source, repaired by hand | none (reserved, Agentic Macros) |
| `aero-forces-by-group` | published | 2606 Build 21.04.007 | 25.0.1 | `compiles` | written against the Javadoc through this server | none (reserved, Agentic Macros) |
| `plane-table-export-ref` | grounded | 2606 Build 21.04.007 | 25.0.1 | `runs` | written against the Javadoc through this server | Series 1, Part 6 |
| `plane-table-export-ref` | recorded | 2606 Build 21.04.007 | 25.0.1 | `runs` | recorded in the STAR-CCM+ GUI | Series 1, Part 6 |

No row is filled from memory or by analogy with the row above it. A row is added only once its transcript has been captured.
