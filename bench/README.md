# Benchmark and provenance

This folder holds the Part 4 benchmark of the Agentic Macros series: the frozen query plan (`plan.json`), one run per server configuration under `runs/`, and this note on where every published figure comes from.

Reference machine for every number in this repository: Simcenter STAR-CCM+ 2606 Build 21.04.007, the JDK it ships, javac 25.0.1, Node v20.14.0, Windows 11. Measured August 2026.

## Runs

| Run | Server state | What it measures |
|---|---|---|
| `runs/antes-20000/` | default cap 20,000 characters, before whitespace collapse | the "before" of Part 4 |
| `runs/colapso-50000/` | whitespace collapse applied, cap 50,000 | effect of the collapse alone |
| `runs/despues-50000/` | cap 50,000, before whitespace collapse | the "after" of the cap change |
| `runs/ab1-A1/`, `runs/ab1-A2/` | this repository at `df16abc`, before the 1.1 changes | the 15 questions of Part 4, two identical runs |
| `runs/ab1-B1/`, `runs/ab1-B2/` | a build with the 1.1 changes (`75ce7be` in the private working repository) | the same 15 questions, two identical runs |
| `runs/ab2-A1/`, `runs/ab2-A2/` | `df16abc` | find one method's signature in a given class, 16 questions, two identical runs |
| `runs/ab2-B1/`, `runs/ab2-B2/` | the 1.1 build | the same 16 questions, two identical runs |

Each run keeps `bench-calls.tsv` (one row per query: characters returned, milliseconds) and `bench-summary.json`. The per call server log (`mcp-calls.tsv`) is not published: it is a session log, not a result.

## The 1.1 comparison

The root README quotes two figures for what the 1.1 changes save. The per call files behind them are the `ab1-*` and `ab2-*` runs, measured on 2026-11-12 with the reference machine above and `STARCCM_MCP_VENTANA=0`. Each side ran twice. The characters of the two runs are identical; only the millisecond columns differ.

| Question set | Files | Before 1.1 (`df16abc`) | With 1.1 | Fewer |
|---|---|---|---|---|
| Part 4, find a class and read its page, 15 questions | `bench-calls.tsv`, `bench-summary.json` | 439,414 characters in 54 calls | 420,075 in 54 | 4.4% |
| Signature of one method, 16 questions | `bench2-calls.tsv`, `bench2-summary.json` | 281,576 in 32 calls | 4,107 in 16 | 98.5% |

To check them: in `ab1-*` add up the `chars` column of `bench-calls.tsv`, which has 54 rows (the 36 calls of the questions plus 18 lookups that find each class first). In `ab2-*` add up the `chars` column of `bench2-calls.tsv` over every row except `search_api(resolve)`. The older server needs one such lookup per question, 16 in all, 131,797 characters, and that lookup is not counted in the 281,576. The newer server needs none. The two sets are counted differently on that point, which is why they are never averaged.

Two things in these files read like failures and are not. First, the `canario_rescatada` and `canario_no_rescatada` lines in the `ab1-B*` summaries say `FALLO`. The canary values were fixed on the pages as the older server returns them (`Simulation` 26,898 characters, `PartManager` 50,016 and truncated). The 1.1 server returns lighter pages, 23,268 and 46,945 with no truncation, so the check cannot pass; the cause is the side index that 1.1 drops. Second, the first column of `ab2-*` files numbers the questions `C01` to `C16` by position, because the question file repeats the id `Q10`.

The files are the originals with one change: their line endings were converted from CRLF to LF. An earlier build of the 1.1 side, which lacked a fix already present here, was also measured and is not published; it was replaced by the build above before the figures were taken. The per call server logs (`mcp-calls.tsv`) of these runs are not published either, for the reason given under the runs table.

## The per call server log

Two facts about `mcp-calls.tsv`, measured on 2026-09-07, both easy to get wrong.

The server writes the log only when `STARCCM_MCP_LOG` is declared in the `Environment` block of the MCP registration (`claude mcp add --env`). That block wins over the variables of the shell that launches the session, so exporting the variable in the terminal before running the client has no effect at all.

The file has no header row. It opens with the first call, seven tab separated fields: timestamp, tool, args, characters, milliseconds, meta, version. A parser that skips the first line eats one call.

## History reset

The history of this repository was reset on 2026-09-15: it now starts at a single root commit holding the tree as published. The previous `series1` tag pointed to an earlier tree, which is kept outside this repository. The tag of the same name now points to the root commit of the new history. Where a run plan compares against `this repo @series1`, it means the earlier tree.

## Plan schema version

The plan schema identifier changed from version 2 to version 3. The format is
unchanged: same fields, same meaning, same order. Only the identifier itself was
renamed. Run summaries published before that change cite the version 2
identifier and remain valid as they stand.

## Figures cited in the root README, with their custody

The public repository is derived from a private working repository on the reference machine. Some custody files stay private because they contain the corporate user path in a line that certifies where a physical backup was kept; rewriting them would break the hash under which they were accredited. Their aggregated figures are published here instead, each with the SHA256 of the private file and the commit that produced it.

| Figure | Value | Custody file (private) | Notes |
|---|---|---|---|
| Class pages in the corpus | 23,498 | `salida-c2/corpus-blocks-summary.json` | denominator for every percentage; the tree holds 48,865 HTML files, 23,498 are class pages |
| Median extracted text per page | 5,587 chars | same | `textChars` after extraction, before collapse |
| 99th percentile | 19,854 chars | same | falls just under the old 20,000 cap |
| Pages above 20,000 before collapse | 224 (0.95%) | same | measured 2026-08-05, sweep `c2`, 66,778 ms, canary OK |
| Pages above 20,000 after removing downward class lists only | 196 | same | not 181: that figure came from a criterion that also removed upward lists |
| Pages saved by the list suppression | 28 of 224 | same | |
| Pages above 20,000 after whitespace collapse | 144 | `tok/corpus-post-colapso-summary.json` (public) | collapse commit `a677897` |
| Pages above 50,000 after collapse | 11 | same | 13 before collapse |
| `Simulation`, raw HTML / extracted / `javap` | 201,275 / 29,981 / 7,920 | `salida-b/corpus-summary.json` and the 2026-08-03 session log | `get_doc` 29,981 is pre collapse (`runs/despues-50000/`); 26,898 after (`runs/colapso-50000/`) |
| Grounding one class, `search_api` + `get_doc` | 33,316 chars | same | 3,335 + 29,981 |
| Collapse effect on the benchmark | 10 to 12% fewer tokens | `bench/runs/colapso-50000/` (public) | 18 `get_doc` calls, cap 50,000; 6.6% fewer characters |
| Characters per token, corpus wide, after collapse | 4.996 `o200k_base`, 4.621 Anthropic legacy | `tok/muestra-corpus.tsv` (public) | stratified sample of 513 classes, census denominator 134,600,475 characters |
| Characters per token by size stratum | monotone decreasing, 9.9% between extremes | same | S1 under 2,365 chars 5.312 / 5.018; S6 over 30,000 chars 4.832 / 4.579 |

Three claims that were withdrawn before publication, kept here so nobody rediscovers them. First, the truncation is not silent (`get_doc` appends `...[truncated]`, 16 characters, on every truncated page). Second, "removing the class lists solves half the problem" is true in characters but not in pages (28 of 224). Third, the comparison against raw HTML that circulated early on, a ratio of 25 times, measured the markup this server strips rather than what it costs to read a page. The ratio against `javap` is the honest one.

## About `tok/esperados-*.md`

The four acceptance criteria in `tok/` are the private custody files copied verbatim, with two exceptions, both in `esperados-dedup-flatten.md`: line 31 has the corporate user path of a physical backup replaced by `<respaldo fisico fuera del repo>`, and an internal workstream field was removed from the front matter. The private original keeps its accredited hash; the public copy differs from it in those two lines only.

## Hashes of the private custody files

Paths are relative to the private repository root. These files are not published; the hashes let anyone check that a copy they are shown is the one measured here.

| Path | SHA256 | Commit |
| --- | --- | --- |
| `salida/corpus-summary.json` | `4002B0ECE5C56BFA9E109A63BC344749B2D35F3B3B00C450B15E630B284BD5EA` | `d1e2f42` |
| `salida-b/corpus-summary.json` | `BFF0E53010940F5026A4D183DF1D70C52A929F11CADD7A3336CD1AB36755C543` | `d1e2f42` |
| `salida-c/corpus-blocks-summary.json` | `A652A97EB86BF11127E5741AD516ADBB770AC9023DF4EDC5AA321FBFD7B0C680` | `d1e2f42` |
| `salida-c2/corpus-blocks-summary.json` | `78458CF1DC2CBBC22FF6D4B74AEDE5FF951E1E48C3D7BEEE64E02D0367E22D62` | `d1e2f42` |
| `salida-c2/confirmacion-canario.txt` | `882873FBBD5F4C623624A88129C435745DD680EEA6197634DFD9472C652EFD4C` | `7ca7743` |
| `tok/corpus-pre-colapso-summary.json` | `648FD705AAA591422289272C225E2C8D6508C87E8061BC95A78C50B556DA220B` | `4100dd0` |
| `tok/recon-zonas.txt` | `BA56F778DCE88CBCD52F424B31CA49BE04635C3F38950E170A73B32EC15A515D` | `74a8888` |
