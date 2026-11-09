// lib/cuaderno.mjs - API notebook that survives context compaction ("cuaderno" is Spanish for notebook)
// Active only when STARCCM_MCP_CUADERNO names a folder; without it, it does nothing. It never changes tool responses.
// One line per (declaring class, method) pair the session looked up; a fixed core (lib/cuaderno-nucleo.txt) goes first;
// cap 4000 characters without the core, oldest lines dropped first. tools/cuaderno-hook.mjs re-injects the file after compaction.
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import path from 'node:path';
import { summarizeClass } from './resumen.mjs';
import { notebookPath } from './cuaderno-ruta.mjs';

const CAP = 4000;
const DIR = process.env.STARCCM_MCP_CUADERNO || '';
let FILE = '', CORE = '', ctx = null, last = null;
const lines = new Map();
const summaries = new Map();

function save() {
  let body = [...lines.values()].join('\n');
  while (body.length > CAP) { lines.delete(lines.keys().next().value); body = [...lines.values()].join('\n'); }
  writeFileSync(FILE, [CORE, body].filter(Boolean).join('\n') + '\n', 'utf8');
}

if (DIR) {
  try {
    mkdirSync(DIR, { recursive: true });
    FILE = notebookPath(DIR, process.cwd());
    try { CORE = readFileSync(new URL('./cuaderno-nucleo.txt', import.meta.url), 'utf8').trim(); } catch {}
    save();
  } catch { FILE = ''; }
}

function summaryOf(name, html) {
  if (html !== undefined) { const r = summarizeClass(html); summaries.set(name, r); return r; }
  if (summaries.has(name)) return summaries.get(name);
  const t = ctx.types.find(t => t.name === name || (t.pkg + '.' + t.name) === name);
  if (!t) return null;
  let r = null;
  try { r = summarizeClass(readFileSync(path.join(ctx.root, t.url), 'utf8')); } catch {}
  summaries.set(name, r);
  return r;
}

function lineFor(cls, met, r) {
  const ms = r ? r.methods.filter(m => m.name === met) : [];
  if (!ms.length) return null;
  const s = (ms.find(m => m.sentence) || {}).sentence;
  return cls + '.' + met + ': Yes. ' + ms.map(m => m.name + m.params + ': ' + m.ret).join('; ') + (s ? ' | ' + s : '');
}

function pair(cls, met) {
  const r = summaryOf(cls);
  if (!r) return null;
  const own = lineFor(cls, met, r);
  if (own) return [cls + '.' + met, own];
  for (const [base, names] of Object.entries(r.inherited)) {
    if (!names.includes(met)) continue;
    const b = base.split('.').pop();
    return [b + '.' + met, lineFor(b, met, summaryOf(base)) || b + '.' + met + ': Yes.'];
  }
  return [cls + '.' + met, cls + '.' + met + ': No.'];
}

function note(cls, met) {
  const p = pair(cls, met);
  if (!p || lines.has(p[0])) return;
  lines.set(p[0], p[1]);
  save();
}

export const notebook = {
  async search(query, kind, types, members, root) {
    if (!FILE) return;
    try {
      ctx = { types, root };
      last = null;
      if (kind === 'type') return;
      const p = query.trim().split(/\s+/);
      if (p.length === 2) { note(p[0], p[1]); last = { cls: p[0], met: p[1] }; return; }
      if (p.length !== 1 || !/^[a-z]\w*$/.test(p[0])) return;
      const classes = [...new Set(members.filter(m => m.name.split('(')[0] === p[0]).map(m => m.cls))].slice(0, 3);
      for (const c of classes) note(c, p[0]);
      if (classes.length === 1) last = { cls: classes[0], met: p[0] };
    } catch {}
  },
  async page(clean, html) {
    if (!FILE || !ctx || !last) return;
    try {
      const base = path.basename(clean, '.html');
      if (base !== last.cls) return;
      summaryOf(base, html);
      note(base, last.met);
    } catch {}
  }
};
