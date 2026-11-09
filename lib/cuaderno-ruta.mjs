// lib/cuaderno-ruta.mjs - notebook file per project; shared by lib/cuaderno.mjs and tools/cuaderno-hook.mjs
import path from 'node:path';
export const notebookPath = (dir, cwd) => path.join(dir, String(cwd).toLowerCase().replace(/[^a-z0-9]/g, '-') + '.md');
