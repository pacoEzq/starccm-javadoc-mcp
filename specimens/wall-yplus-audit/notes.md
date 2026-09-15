# wall-yplus-audit

A one click wall y+ audit. It walks every wall boundary, computes the area averaged and the maximum wall y+ for each, and flags any wall outside the band the wall treatment expects. It was written against the Javadoc through this server and closes Part 6 of the first tutorial series, where it carries the point that a real API is not the same as a correct macro.

## The defect in the published version

Two errors, both on line 67, both the same cause:

```
Collection<NamedObject> one = Collections.<NamedObject>singletonList(b);
```

`NamedObject` is declared in `star.base.neo`. The file imports `star.common.*` and `star.base.report.*`, neither of which covers it, and there is no `import star.base.neo.NamedObject;`. So the symbol does not resolve and the macro never compiles.

This is worth sitting with. Every call in this macro was grounded: each method exists, sits on the class the model named, and is not deprecated. Grounding still cannot see a missing import, because the symbol is real and the search that confirmed it never asked which package the file can actually reach. That is the gap the series calls the honest limit, and here it is as a compiler error rather than an argument.

## The fix

`fixed/` adds the missing import and repairs a null guard that could never fire. It compiles with no output and exit code 0. It is not claimed to run: this specimen goes as far as the compiler and stops there.

## A note on file names

The first transcript ever taken of this macro reported a third error, complaining that a public class named `WallYplusAudit` must live in a file called `WallYplusAudit.java`. That error came from a local copy carrying a suffix in its file name, not from the macro. Recompiled with the file named after its class, it does not appear.

That is why states live in folders here and never in suffixes on the file name. A naming convention that injects a phantom compiler error into every specimen would corrupt the evidence this folder exists to hold.
