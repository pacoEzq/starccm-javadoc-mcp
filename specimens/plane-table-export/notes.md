# plane-table-export

A plane sampling and export macro. It cuts a Z normal plane section at (0.5, 0, 0) m through all regions and boundaries, samples it with an XYZ internal table carrying four field functions, and exports the table to CSV. It was written against the Javadoc through this server.

## Both states compile

Unlike the first specimen, the compiler separates nothing here. `published/` and `fixed/` both return exit code 0 and both emit the same two notes about a deprecated API. Whatever the difference between the two files is, `javac` cannot see it.

## The deprecated call, named

The plain transcript says only that a deprecated API is used somewhere. Recompiling the published file with `-Xlint:deprecation` names it:

```
PlaneTableExport.java:74: warning: [deprecation] <T>createTable(Class<T>) in TableManager has been deprecated
        sim.getTableManager().createTable(XyzInternalTable.class);
```

Both files carry that call, so `fixed/` does not address it. What the current replacement is has not been measured here and is deliberately not stated.

## The defect the compiler cannot see

The published file calls `getFunction(String)` four times with nothing checked first. `fixed/` adds a `hasFunction` test for each of the four names, collects every absent one, and aborts with a single message before the first lookup. The reason recorded with the fix is that a miss returns an object rather than null, so a null check would be dead code and an absent name would reach the CSV as a silent empty column. That reason is not measured by this specimen, which stops at the compiler.

## A declared redaction

Line 32 of both files held the output CSV path as an absolute path carrying a user name. The contract forbids that in a specimen and also requires the source exactly as compiled, so the two rules met head on here. The path was replaced by the placeholder `<output-folder>`, the transcripts were captured again afterwards, and both files still compile to exit code 0 with the same deprecation note. The literal is a string constant that no other line reads at compile time, which is what makes the redaction admissible under the rule this specimen added to the contract.

| File | SHA256 before | Bytes before | SHA256 after | Bytes after |
|---|---|---|---|---|
| `published/PlaneTableExport.java` | `B6DAF546574D72CD44C77507707522604DFE74DFF45564106B5A1B9EBF627FF3` | 4481 | `C57680B22C842EA0A67BC205146F5733660E1B4EECC84BBF73DD4CC753DE2B60` | 4470 |
| `fixed/PlaneTableExport.java` | `9F80BA9C02A6526B30B99245C596895137087964F1CDEDEBA4B0EBBF86B012F0` | 5342 | `E5C9B826D2EB29B207E9E787533996633559240DDB17BE462A07DF315EC77E4C` | 5331 |

Eleven bytes leave each file, which is the length the substitution predicts.

## Citation

Neither state is cited by a published post. The macro is an earlier generation than the pair that closes Part 6 of the first series, and the contrast between them, one calling a deprecated factory that only `-Xlint:deprecation` caught and one calling the current form, is material reserved for the Agentic Macros series and its compile loop. The index rows therefore read `none (reserved, Agentic Macros)`.

## What this specimen shows

The first specimen failed to compile, so the compiler was enough to separate the two states. Here it is not. Every call was grounded, every symbol resolves, both files build, and the difference between a macro that reports a missing field function and one that quietly exports empty columns is invisible to both the API tools and `javac`. That is the rung above `compiles` on the ladder, and this specimen does not claim it.
