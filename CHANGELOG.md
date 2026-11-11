# Changelog

What changed in the server since the `series1` tag (15 September 2026), newest first. Figures for what these changes save are left out on purpose: they will be added here once they are measured again against the current server.

## 2026-11-09

### Optional API notebook that survives compaction

When a long Claude Code session compacts its context, the model forgets which classes and methods it already looked up and fetches them again. Set `STARCCM_MCP_CUADERNO` to a folder and the server keeps a short notebook per project with what the session looked up; a SessionStart hook, `tools/cuaderno-hook.mjs`, reads it back in after compaction. Off by default, and it never changes what `search_api` or `get_doc` return. Setup in the README, [Optional: an API notebook that survives compaction](README.md#optional-an-api-notebook-that-survives-compaction).

### `get_doc` reads long pages in parts

When a page is longer than `maxChars`, the result now ends with the `offset` to pass on the next call, and `get_doc` accepts that `offset`. The rest of the page can be read without fetching the start again.

### `search_api` understands "Class member"

A two-word query such as `Simulation getRegionManager` that finds nothing as a whole is now read as a class name followed by a member name, and the server lists the members of that class that match. If the class declares no such member, the answer says so, warns that the member may be inherited (`get_doc` on the class page lists inherited members), and shows the classes that do declare it.

## 2026-10-29

### Working links to methods, and a stricter `get_doc`

Links to methods and fields returned by `search_api` were missing the `#` before the anchor, so they did not open. Fixed. `get_doc` now reads only `.html` and `.htm` pages, and resolves symbolic links before checking that the page is inside the Javadoc folder. Both reported by Pitohuie-AIversion in pull request 3.

## 2026-10-10

### `search_api` states its limit

`search_api` lists a method or field only under the class that declares it, not under subclasses that inherit it. The tool description and the README now say so and point to `get_doc` on the subclass page for inherited members ([issue #1](https://github.com/pacoEzq/starccm-javadoc-mcp/issues/1)).

## Maintenance

- 2026-11-06: Node.js requirement moved to supported lines, 22.23.4 or newer on 22 and 24.22.1 or newer on 24; 18 and 20 are end-of-life.
- 2026-10-29: `@modelcontextprotocol/sdk` updated to 1.32.2 and the lockfile refreshed.
- 2026-10-29: security policy added, see `SECURITY.md`.
