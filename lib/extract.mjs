const DOWNWARD_LABELS = new Set([
  "All Known Implementing Classes",
  "All Known Subinterfaces",
  "Direct Known Subclasses",
]);

const OPEN = '<dl class="notes">';
const CLOSE = "</dl>";

function labelOf(block) {
  const a = block.indexOf("<dt>");
  if (a < 0) return null;
  const b = block.indexOf("</dt>", a);
  if (b < 0) return null;
  return block
    .slice(a + 4, b)
    .replace(/<[^>]+>/g, "")
    .replace(/&nbsp;/g, " ")
    .replace(/\s+/g, " ")
    .trim()
    .replace(/:$/, "")
    .trim();
}

export function stripDownwardBlocks(html) {
  let out = "";
  let cursor = 0;
  let removed = 0;
  let anomalies = 0;
  for (;;) {
    const start = html.indexOf(OPEN, cursor);
    if (start < 0) break;
    const close = html.indexOf(CLOSE, start);
    if (close < 0) { anomalies++; break; }
    const end = close + CLOSE.length;
    const block = html.slice(start, end);
    if (block.indexOf("<dl", OPEN.length) >= 0) {
      anomalies++;
      out += html.slice(cursor, end);
      cursor = end;
      continue;
    }
    const label = labelOf(block);
    if (label !== null && DOWNWARD_LABELS.has(label)) {
      out += html.slice(cursor, start);
      removed++;
    } else {
      out += html.slice(cursor, end);
    }
    cursor = end;
  }
  out += html.slice(cursor);
  return { html: out, removed, anomalies };
}

export function flattenToText(html) {
  return html
    .replace(/<script[\s\S]*?<\/script>/gi, " ")
    .replace(/<style[\s\S]*?<\/style>/gi, " ")
    .replace(/<[^>]+>/g, " ")
    .replace(/&nbsp;/g, " ")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&amp;/g, "&")
    .replace(/\s+/g, " ")
    .trim();
}

// Side index (nav.toc): it lists again the sections and members that the class
// page already contains. It is cut from the END OF TOP NAVBAR comment to the
// START OF CLASS DATA comment. Unless there is exactly one of each, in that
// order, the page is left untouched.
const SIDE_FROM = "END OF TOP NAVBAR";
const SIDE_TO = "START OF CLASS DATA";

export function stripSideIndex(html) {
  const cs = [];
  for (const m of html.matchAll(/<!--([\s\S]*?)-->/g)) cs.push({ i: m.index, t: m[1] });
  const a = cs.filter((c) => c.t.includes(SIDE_FROM));
  const b = cs.filter((c) => c.t.includes(SIDE_TO));
  if (a.length !== 1 || b.length !== 1 || a[0].i >= b[0].i) return { html, removed: false };
  return { html: html.slice(0, a[0].i) + html.slice(b[0].i), removed: true };
}

export function extractText(html, maxChars) {
  const pruned = stripDownwardBlocks(html);
  const side = stripSideIndex(pruned.html);
  const text = flattenToText(side.html);
  const truncated = text.length > maxChars;
  const out = truncated ? text.slice(0, maxChars) + "\n\n...[truncated]" : text;
  return { text, out, truncated, blocksRemoved: pruned.removed, sideIndexRemoved: side.removed };
}
