// Captura as quatro telas de docs/prototipos.html (protótipo estático, sem API).
const puppeteer = require("puppeteer-core");
const path = require("path");

const edge =
  process.env.PUPPETEER_EXECUTABLE_PATH ||
  "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";

const dir = __dirname;
const protoHtml = path.join(dir, "..", "prototipos.html");
const url = "file:///" + protoHtml.replace(/\\/g, "/");
const telas = ["cadastro", "reserva", "entrega", "painel"];

(async () => {
  const browser = await puppeteer.launch({
    executablePath: edge,
    headless: true,
    args: ["--no-sandbox"],
  });
  const page = await browser.newPage();
  await page.setViewport({ width: 1100, height: 880, deviceScaleFactor: 2 });
  await page.goto(url, { waitUntil: "networkidle0", timeout: 60000 });

  for (let i = 0; i < telas.length; i++) {
    const id = telas[i];
    await page.evaluate((tela) => {
      document.querySelectorAll("nav button").forEach((b) => b.classList.remove("ativa"));
      const btn = document.querySelector(`nav button[data-tela="${tela}"]`);
      if (btn) btn.classList.add("ativa");
      document.querySelectorAll("main section").forEach((s) => s.classList.remove("visivel"));
      const sec = document.getElementById(tela);
      if (sec) sec.classList.add("visivel");
    }, id);
    await new Promise((r) => setTimeout(r, 250));
    const out = path.join(dir, `fig-proto-${i + 1}-${id}.png`);
    await page.screenshot({ path: out, fullPage: true, type: "png" });
    console.log("Capturado:", out);
  }
  await browser.close();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});