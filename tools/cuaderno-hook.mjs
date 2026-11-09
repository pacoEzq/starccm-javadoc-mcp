// tools/cuaderno-hook.mjs - Claude Code SessionStart hook (matcher "compact"): re-injects the API notebook after compaction
// usage in the user settings: node <repo>/tools/cuaderno-hook.mjs <STARCCM_MCP_CUADERNO folder>
import { readFileSync } from 'node:fs';
import { notebookPath } from '../lib/cuaderno-ruta.mjs';
const dir = process.argv[2];
let input = '';
for await (const c of process.stdin) input += c;
try {
  const { cwd } = JSON.parse(input);
  const t = readFileSync(notebookPath(dir, cwd), 'utf8').trim();
  if (t) process.stdout.write('API already looked up in this session; call get_doc only if you need the detail.\n' + t + '\n');
} catch {}
