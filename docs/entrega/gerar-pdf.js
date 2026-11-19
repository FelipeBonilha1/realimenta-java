// Gera o PDF da documentacao a partir do HTML local. Nao usa rede nem credenciais.
// Passagem 1: PDF temporario -> mapa de paginas das secoes.
// Passagem 2: atualiza sumario no HTML e grava o PDF final.
const fs = require("fs");
const path = require("path");
const puppeteer = require("puppeteer-core");
const pdf = require("pdf-parse");

const edge =
  process.env.PUPPETEER_EXECUTABLE_PATH ||
  "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";

const dir = __dirname;
const htmlPath = path.join(dir, "ReAlimenta_SP_Documentacao_Java.html");
const pdfPath = path.join(dir, "ReAlimenta_SP_Documentacao_Java.pdf");
const tempPdf = path.join(dir, ".tmp-documentacao.pdf");

const SECOES = [
  { id: "apresentacao", re: /1\.\s*Apresentação,\s*justificativa/ },
  { id: "arquitetura", re: /2\.\s*Arquitetura e implementação/ },
  { id: "classes", re: /3\.\s*Diagrama de classes/ },
  { id: "banco", re: /4\.\s*Banco de dados/ },
  { id: "prototipos", re: /5\.\s*Protótipos de telas/ },
  { id: "endpoints", re: /6\.\s*Endpoints da API/ },
  { id: "execucao", re: /7\.\s*Execução local e Docker/ },
  { id: "deploy", re: /8\.\s*Deploy no Render/ },
  { id: "verificacao", re: /9\.\s*Verificação de testes/ },
];

async function renderPdf(browser, outFile) {
  const page = await browser.newPage();
  const url = "file:///" + htmlPath.replace(/\\/g, "/");
  await page.goto(url, { waitUntil: "networkidle0", timeout: 120000 });
  await page.pdf({
    path: outFile,
    format: "A4",
    printBackground: true,
    margin: { top: "18mm", bottom: "20mm", left: "16mm", right: "16mm" },
    displayHeaderFooter: true,
    headerTemplate: "<span></span>",
    footerTemplate:
      '<div style="font-size:8px;width:100%;padding:0 16mm;color:#555;text-align:center;">' +
      'ReAlimenta SP — Documentação Java — <span class="pageNumber"></span> / <span class="totalPages"></span></div>',
  });
  await page.close();
}

async function mapSectionPages(buf) {
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
  return { map, numpages: pages.length };
}

function atualizarSumarioNoHtml(map) {
  let html = fs.readFileSync(htmlPath, "utf8");
  for (const s of SECOES) {
  const n = map[s.id];
  if (n == null) continue;
  const re = new RegExp(
    `(data-toc="${s.id}"[^>]*>)—(<\\/span>)`,
    "g"
  );
  html = html.replace(re, `$1${n}$2`);
  const reNum = new RegExp(
    `(data-toc="${s.id}"[^>]*>)\\d+(<\\/span>)`,
    "g"
  );
  html = html.replace(reNum, `$1${n}$2`);
  }
  fs.writeFileSync(htmlPath, html, "utf8");
  return map;
}

(async () => {
  const browser = await puppeteer.launch({
    executablePath: edge,
    headless: true,
    args: ["--no-sandbox"],
  });

  await renderPdf(browser, tempPdf);
  const buf = fs.readFileSync(tempPdf);
  const { map, numpages } = await mapSectionPages(buf);
  console.log("Mapa de seções (passagem 1):", map);

  atualizarSumarioNoHtml(map);
  await renderPdf(browser, pdfPath);
  await browser.close();

  try {
    fs.unlinkSync(tempPdf);
  } catch (_) {}

  const buf2 = fs.readFileSync(pdfPath);
  const check = await mapSectionPages(buf2);
  let tocOk = true;
  for (const s of SECOES) {
    if (check.map[s.id] !== map[s.id]) {
      tocOk = false;
      console.warn(
        `Sumário: seção ${s.id} mudou de p.${map[s.id]} para p.${check.map[s.id]} na passagem 2`
      );
    }
  }
  if (tocOk) {
    console.log("Sumário conferido: páginas estáveis após inserir números.");
  } else {
    atualizarSumarioNoHtml(check.map);
    console.log("HTML atualizado com páginas da passagem 2; execute gerar-pdf.js uma vez se necessário.");
  }

  console.log("PDF gerado:", pdfPath, `(${numpages} páginas, ${(buf2.length / 1024).toFixed(1)} KB)`);
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
