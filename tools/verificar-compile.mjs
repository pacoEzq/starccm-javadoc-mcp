// Checks the compile tool through a real MCP stdio client. A1: the published Part 1 macro gives the published javac output. A3: without its variables the tool is not exposed.
// Usage: node tools/verificar-compile.mjs <AeroForcesByGroup.java> <published javac.txt>
// Reads STARCCM_JDK (bin folder), STARCCM_CLASSPATH and STARCCM_DOC from the environment.
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";
import { promises as fs } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const [src, published] = process.argv.slice(2);
if (!src || !published) { console.error("usage: verificar-compile.mjs <file.java> <javac.txt>"); process.exit(2); }
const server = path.join(path.dirname(fileURLToPath(import.meta.url)), "..", "server.mjs");

async function conectar(env) {
  const transport = new StdioClientTransport({ command: process.execPath, args: [server], env, stderr: "pipe" });
  let err = "";
  transport.stderr?.on("data", (b) => { err += b; });
  const client = new Client({ name: "verificar-compile", version: "0" });
  await client.connect(transport);
  return { client, err: () => err };
}

let fallos = 0;
const check = (ok, msg) => { console.log(`${ok ? "PASS" : "FAIL"} ${msg}`); if (!ok) fallos++; };

// A1
{
  const { client } = await conectar({ ...process.env });
  const tools = (await client.listTools()).tools.map((t) => t.name);
  check(tools.includes("compile"), `A1 compile exposed (${tools.join(", ")})`);
  const r = await client.callTool({ name: "compile", arguments: { path: path.resolve(src) } });
  const text = r.content[0].text;
  const nl = text.indexOf("\n");
  const first = text.slice(0, nl);
  const rest = text.slice(nl + 1);
  const raw = await fs.readFile(published, "utf8");
  const esperado = raw.split(/\r?\n/).filter((l) => !l.startsWith("# ")).join("\n").replace(/^\n+/, "");
  console.log(`first line: ${JSON.stringify(first)}; diagnostics: ${JSON.stringify(rest)}`);
  check(first === "exit 0", "A1 exit 0");
  check(rest === esperado, `A1 output equals published javac.txt without its header (${esperado.length} chars)`);
  const bad = await client.callTool({ name: "compile", arguments: { path: path.join(path.parse(server).root, "no-existe", "x.java") } });
  check(bad.content[0].text.startsWith("exit rejected"), "reject missing file");
  const bad2 = await client.callTool({ name: "compile", arguments: { path: path.resolve(server) } });
  check(bad2.content[0].text.startsWith("exit rejected"), "reject non-.java");
  await client.close();
}

// A3
for (const quitar of [["STARCCM_JDK", "STARCCM_CLASSPATH"], ["STARCCM_CLASSPATH"], ["STARCCM_JDK"]]) {
  const env = { ...process.env };
  for (const k of quitar) delete env[k];
  const { client, err } = await conectar(env);
  const tools = (await client.listTools()).tools.map((t) => t.name);
  await new Promise((r) => setTimeout(r, 200));
  check(!tools.includes("compile") && tools.includes("search_api") && tools.includes("get_doc"),
    `A3 without ${quitar.join("+")}: starts, compile not exposed (${tools.join(", ")})`);
  check(/compile tool not exposed/.test(err()), `A3 without ${quitar.join("+")}: warns -> ${JSON.stringify(err().trim())}`);
  await client.close();
}
process.exit(fallos ? 1 : 0);
