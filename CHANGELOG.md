# Changelog

What changed in the server, newest first. Version numbers follow [Semantic Versioning](https://semver.org/): a new minor number adds features and keeps every existing setup working. What the 1.1 changes save, measured in November 2026, is in the README, [What the 1.1 changes save](README.md#what-the-11-changes-save).

## 1.1.1 (2026-11-15)

### "Class member" warns about inherited members more often (2026-11-13)

When the class declares members whose names contain the one asked for, but none with exactly that name, `search_api` still lists them and now also warns that a member with exactly that name may be inherited, pointing to `get_doc` on the class page. Before, the warning appeared only when the class declared no matching member at all.

### Class pages come back lighter: the side index is dropped (2026-11-15)

`get_doc` now removes the side index (`nav.toc`) that Javadoc places beside each class page. It only lists again the sections and members that the page already contains, so nothing the page says is lost. The cut runs from the end of the top navigation bar to the start of the class data; if a page does not have exactly one of each, in that order, it is returned as before. On the Part 4 benchmark, `get_doc` returns 7.5% fewer characters.

## 1.1.0 (2026-11-11)

### Optional API notebook that survives compaction (2026-11-09)

When a long Claude Code session compacts its context, the model forgets which classes and methods it already looked up and fetches them again. Set `STARCCM_MCP_CUADERNO` to a folder and the server keeps a short notebook per project with what the session looked up; a SessionStart hook, `tools/cuaderno-hook.mjs`, reads it back in after compaction. Off by default, and it never changes what `search_api` or `get_doc` return. Setup in the README, [Optional: an API notebook that survives compaction](README.md#optional-an-api-notebook-that-survives-compaction).

### `get_doc` reads long pages in parts (2026-11-09)

When a page is longer than `maxChars`, the result now ends with the `offset` to pass on the next call, and `get_doc` accepts that `offset`. The rest of the page can be read without fetching the start again.

### `search_api` understands "Class member" (2026-11-09)

A two-word query such as `Simulation getRegionManager` that finds nothing as a whole is now read as a class name followed by a member name, and the server lists the members of that class that match. If the class declares no such member, the answer says so, warns that the member may be inherited (`get_doc` on the class page lists inherited members), and shows the classes that do declare it.

### Working links to methods, and a stricter `get_doc` (2026-10-29)

Links to methods and fields returned by `search_api` were missing the `#` before the anchor, so they did not open. Fixed. `get_doc` now reads only `.html` and `.htm` pages, and resolves symbolic links before checking that the page is inside the Javadoc folder. Both reported by Pitohuie-AIversion in pull request 3.

### `search_api` states its limit (2026-10-10)

`search_api` lists a method or field only under the class that declares it, not under subclasses that inherit it. The tool description and the README now say so and point to `get_doc` on the subclass page for inherited members ([issue #1](https://github.com/pacoEzq/starccm-javadoc-mcp/issues/1)).

### Maintenance

- 2026-11-06: Node.js requirement moved to supported lines, 22.23.4 or newer on 22 and 24.22.1 or newer on 24; 18 and 20 are end-of-life.
- 2026-10-29: `@modelcontextprotocol/sdk` updated to 1.32.2 and the lockfile refreshed.
- 2026-10-29: security policy added, see `SECURITY.md`.

## 1.0.0 (2026-09-15)

The server as published with Series 1, tag `series1`. Tag `agentic1` adds documentation, benchmark and specimen files around the same server.
