// compile tool: wrapper of the javac in the JDK that ships with STAR-CCM+.
// javac is spawned without a shell, with a fixed argument list. Nothing compiled is ever run.
import { spawn } from "node:child_process";
import { promises as fs } from "node:fs";
import os from "node:os";
import path from "node:path";

export const TIMEOUT_MS = 120000;
export const MAX_CHARS = 30000;

// Returns { ok: true, javac, classpath, workdirs } or { ok: false, reason }.
// No defaults are guessed: both variables are required.
export async function compileConfig(env = process.env) {
  const jdk = env.STARCCM_JDK;
  const classpath = env.STARCCM_CLASSPATH;
  if (!jdk || !classpath) {
    const falta = [!jdk && "STARCCM_JDK", !classpath && "STARCCM_CLASSPATH"].filter(Boolean).join(" and ");
    return { ok: false, reason: `${falta} not set` };
  }
  const javac = path.join(jdk, process.platform === "win32" ? "javac.exe" : "javac");
  try {
    await fs.access(javac);
  } catch {
    return { ok: false, reason: `javac not found at ${javac} (STARCCM_JDK must be the JDK bin folder)` };
  }
  // Optional: restrict path to declared working folders (path-delimiter separated).
  const raw = env.STARCCM_WORKDIRS || "";
  const workdirs = [];
  for (const d of raw.split(path.delimiter).filter(Boolean)) {
    try { workdirs.push(await fs.realpath(d)); } catch {}
  }
  if (raw && workdirs.length === 0) {
    return { ok: false, reason: "STARCCM_WORKDIRS is set but none of its folders exist" };
  }
  return { ok: true, javac, classpath, workdirs: raw ? workdirs : null };
}

function resultado(text) {
  return { content: [{ type: "text", text }] };
}
function rechazo(msg) {
  return { content: [{ type: "text", text: `exit rejected\n${msg}` }], isError: true };
}

export async function runCompile(cfg, p) {
  if (typeof p !== "string" || !path.isAbsolute(p)) return rechazo("path must be an absolute path to a .java file");
  if (path.extname(p).toLowerCase() !== ".java") return rechazo("path must end in .java");
  let real;
  try {
    const st = await fs.stat(p);
    if (!st.isFile()) return rechazo(`not a file: ${p}`);
    real = await fs.realpath(p);
  } catch {
    return rechazo(`file not found: ${p}`);
  }
  if (cfg.workdirs) {
    const dentro = cfg.workdirs.some((w) => real === w || real.startsWith(w + path.sep));
    if (!dentro) return rechazo(`path is outside STARCCM_WORKDIRS: ${p}`);
  }

  const outDir = await fs.mkdtemp(path.join(os.tmpdir(), "starccm-compile-"));
  try {
    const args = ["-cp", cfg.classpath, "-d", outDir, p];
    const chunks = [];
    const { code, timedOut, spawnError } = await new Promise((resolve) => {
      let child;
      try {
        child = spawn(cfg.javac, args, { shell: false, windowsHide: true, stdio: ["ignore", "pipe", "pipe"] });
      } catch (e) {
        resolve({ code: null, timedOut: false, spawnError: e.message });
        return;
      }
      let timedOut = false;
      let settled = false;
      const fin = (v) => { if (!settled) { settled = true; clearTimeout(timer); resolve(v); } };
      const timer = setTimeout(() => { timedOut = true; child.kill("SIGKILL"); }, TIMEOUT_MS);
      child.stdout.on("data", (b) => chunks.push(b));
      child.stderr.on("data", (b) => chunks.push(b));
      child.on("error", (e) => fin({ code: null, timedOut: false, spawnError: e.message }));
      child.on("close", (c) => fin({ code: c, timedOut }));
    });

    const diag = Buffer.concat(chunks).toString("utf8");
    if (spawnError) return resultado(`exit spawn-failed\n${spawnError}`);
    const head = timedOut ? "exit timeout" : `exit ${code}`;
    let body = diag;
    if (body.length > MAX_CHARS) {
      body = body.slice(0, MAX_CHARS) +
        `\n...[truncated: ${MAX_CHARS} of ${diag.length} chars of javac output; fix the errors shown and call compile again]`;
    }
    return resultado(`${head}\n${body}`);
  } finally {
    await fs.rm(outDir, { recursive: true, force: true }).catch(() => {});
  }
}
