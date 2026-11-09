// lib/resumen.mjs - minimal summary of a Javadoc class page
// methods: { name, params, ret, sentence }; inherited: { Class: [names] }
const text = s => s.replace(/<br\s*\/?>/g, ' ').replace(/<wbr>/g, '').replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/&lt;/g, '<')
  .replace(/&gt;/g, '>').replace(/&quot;/g, '"').replace(/&#39;/g, "'").replace(/&amp;/g, '&').replace(/\s+/g, ' ').trim();
const firstSentence = t => { const s = text(t); const k = s.search(/\.(\s|$)/); const f = k >= 0 ? s.slice(0, k + 1) : s; return f.length > 120 ? f.slice(0, 117) + '...' : f; };
export function summarizeClass(html) {
  const methods = [];
  const row = /<div class="col-first[^"]*method-summary-table[^"]*">([\s\S]*?)<\/div>\s*<div class="col-second[^"]*">([\s\S]*?)<\/div>\s*<div class="col-last[^"]*">\s*(?:<div class="block">([\s\S]*?)<\/div>)?/g;
  for (const m of html.matchAll(row)) {
    const sig = text(m[2]); const k = sig.indexOf('(');
    methods.push({ name: k >= 0 ? sig.slice(0, k) : sig, params: k >= 0 ? sig.slice(k) : '', ret: text(m[1]).replace(/^(public|protected|static|final|abstract|default|\s)+/, ''), sentence: m[3] ? firstSentence(m[3]) : '' });
  }
  const inherited = {};
  const inh = /<h3 id="methods-inherited-from-class-([^"]+)">[\s\S]*?<\/h3>\s*<code>([\s\S]*?)<\/code>/g;
  for (const m of html.matchAll(inh)) inherited[m[1]] = text(m[2]).split(/\s*,\s*/).filter(Boolean);
  return { methods, inherited };
}
