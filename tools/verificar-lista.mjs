// Checks that every file tracked by git is either derived (listed in
// tools/publish-list.txt) or native to this public repo (EXEMPT below).
// Also flags listed paths that are not tracked.
// Usage: node tools/verificar-lista.mjs [path-to-list]
// Exit code: 0 = consistent, 1 = gaps found.
import { execFileSync } from "node:child_process";
import { readFileSync } from "node:fs";

const EXEMPT_EXACT = new Set([
  ".gitattributes",
  "LICENSE",
  "README.md",
  "bench/README.md",
  "tools/publish-list.txt",
  "tools/verificar-lista.mjs",
]);
const EXEMPT_PREFIX = ["specimens/"];

const root = execFileSync("git", ["rev-parse", "--show-toplevel"], { encoding: "utf8" }).trim();
process.chdir(root);

const listPath = process.argv[2] || "tools/publish-list.txt";
const tracked = execFileSync("git", ["ls-files", "-z"], { encoding: "utf8" })
  .split("\0")
  .filter(Boolean);
const listed = readFileSync(listPath, "utf8")
  .split(/\r?\n/)
  .map((s) => s.trim())
  .filter((s) => s && !s.startsWith("#"));

const listedSet = new Set(listed);
const trackedSet = new Set(tracked);
const isExempt = (p) => EXEMPT_EXACT.has(p) || EXEMPT_PREFIX.some((x) => p.startsWith(x));

const unlisted = tracked.filter((p) => !listedSet.has(p) && !isExempt(p));
const missing = listed.filter((p) => !trackedSet.has(p));

console.log("published but not in the list: " + unlisted.length);
for (const p of unlisted) console.log("  " + p);
console.log("in the list but not published: " + missing.length);
for (const p of missing) console.log("  " + p);

if (unlisted.length || missing.length) {
  console.log("RESULT: gaps found");
  process.exit(1);
}
console.log("RESULT: OK (" + listed.length + " listed, " + tracked.length + " tracked)");