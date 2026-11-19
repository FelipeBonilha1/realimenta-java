// Verificação pós-geração: páginas, seções, figuras e sumário (sem credenciais).
const fs = require("fs");
const path = require("path");
const pdf = require("pdf-parse");

const pdfPath = path.join(__dirname, "ReAlimenta_SP_Documentacao_Java.pdf");
const SECOES = [
  { id: "apresentacao", re: /1\.\s*Apresentação,\s*justificativa/, label: "§1 Apresentação" },
  { id: "arquitetura", re: /2\.\s*Arquitetura/, label: "§2 Arquitetura" },
  { id: "classes", re: /3\.\s*Diagrama de classes/, label: "§3 Classes" },
  { id: "banco", re: /4\.\s*Banco de dados/, label: "§4 Banco" },
  { id: "prototipos", re: /5\.\s*Protótipos/, label: "§5 Protótipos" },
  { id: "endpoints", re: /6\.\s*Endpoints/, label: "§6 Endpoints" },
  { id: "execucao", re: /7\.\s*Execução/, label: "§7 Execução" },
  { id: "deploy", re: /8\.\s*Deploy/, label: "§8 Deploy" },
  { id: "verificacao", re: /9\.\s*Verificação/, label: "§9 Verificação" },
];
const figuras = ["Figura 1", "Figura 2", "Figura 3", "Figura 4", "Figura 5", "Figura 6"];
const TOC_IDS = SECOES.map((s) => s.id);

(async () => {
  const buf = fs.readFileSync(pdfPath);
  const pages = [];
  await pdf(buf, {
    pagerender: (pageData) =>
      pageData.getTextContent().then((tc) => {
        const text = tc.items.map((i) => i.str).join(" ");
        pages.push(text);
        return text;
      }),
  });

  const map = {};
  for (let p = 0; p < pages.length; p++) {
    const t = pages[p].trim();
    if (t.startsWith("Sumário")) continue;
    for (const s of SECOES) {
      if (!map[s.id] && s.re.test(t)) map[s.id] = p + 1;
    }
  }

  console.log("Arquivo:", pdfPath);
  console.log("Tamanho:", (buf.length / 1024).toFixed(1), "KB");
  console.log("Páginas:", pages.length);
  console.log("\nSeções:");
  for (const s of SECOES) {
    console.log(`  ${s.label}: ${map[s.id] != null ? "p. " + map[s.id] : "NÃO ENCONTRADO"}`);
  }

  const all = pages.join("\n");
  console.log("\nFiguras (texto extraído das legendas):");
  for (const f of figuras) {
    console.log(`  ${f}:`, all.includes(f) ? "sim" : "FALTA");
  }

  const toc = pages[1] || "";
  console.log("\nSumário (conferência numérica):");
  for (const id of TOC_IDS) {
    const expected = map[id];
    const found = toc.match(new RegExp(`${id}"[^>]*>\\s*(\\d+)`));
    // HTML attrs não vão ao PDF; buscar número após título abreviado no texto da p.2
  }
  for (const s of SECOES) {
    const labelShort = s.label.replace("§", "").split(" ")[0];
    const m = toc.match(
      new RegExp(
        s.id === "apresentacao"
          ? /Apresentação[\s\S]*?(\d{1,2})\s+2\./
          : s.id === "arquitetura"
            ? /Arquitetura[\s\S]*?(\d{1,2})\s+3\./
            : null
      )
    );
  }
  // Conferir pares título + número na página 2 (extração linear)
  const pairs = [
    ["Apresentação", map.apresentacao],
    ["Arquitetura", map.arquitetura],
    ["Diagrama de classes", map.classes],
    ["Banco de dados", map.banco],
    ["Protótipos", map.prototipos],
    ["Endpoints", map.endpoints],
    ["Execução", map.execucao],
    ["Deploy", map.deploy],
    ["Verificação", map.verificacao],
  ];
  for (const [titulo, pg] of pairs) {
    const re = new RegExp(titulo + "[^\\d]*(\\d{1,2})");
    const m = toc.match(re);
    const noToc = m ? parseInt(m[1], 10) : null;
    const ok = noToc === pg;
    console.log(
      `  ${titulo}: PDF seção p.${pg}; sumário mostra ${noToc != null ? noToc : "?"} — ${ok ? "OK" : "DIVERGENTE"}`
    );
  }

  if (/\bpassword\s*=\s*['"][^'"]+['"]/i.test(all)) {
    console.error("\nALERTA: possível senha literal no PDF.");
    process.exit(2);
  }
  console.log("\nCredenciais: sem senha literal; apenas nomes de variáveis (DB_PASSWORD).");
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
