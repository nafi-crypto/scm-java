import fs from "node:fs";
import path from "node:path";
import { createServer } from "node:http";
import { context } from "esbuild";
import { buildStyles, outputDirectory, root } from "./build-utils.mjs";

const gateway = process.env.SCM_GATEWAY_URL ?? "http://localhost:8080";
const port = Number(process.env.PORT ?? 5173);

const bundle = await context({
  entryPoints: ["src/main.tsx"],
  bundle: true,
  platform: "browser",
  format: "esm",
  target: "es2022",
  jsx: "automatic",
  loader: { ".css": "empty" },
  entryNames: "app",
  outdir: `${outputDirectory}/assets`,
  sourcemap: "inline",
});
await bundle.watch();
await buildStyles();

let styleTimer;
fs.watch(path.join(root, "src"), { recursive: true }, (_, filename) => {
  if (filename && /\.(tsx|ts|css)$/.test(filename)) {
    clearTimeout(styleTimer);
    styleTimer = setTimeout(() => {
      void buildStyles().catch((error) => console.error("Tailwind rebuild failed:", error));
    }, 100);
  }
});

const contentTypes = new Map([
  [".css", "text/css; charset=utf-8"],
  [".html", "text/html; charset=utf-8"],
  [".js", "text/javascript; charset=utf-8"],
  [".svg", "image/svg+xml"],
]);

const server = createServer(async (request, response) => {
  try {
    const requestPath = new URL(request.url ?? "/", "http://localhost").pathname;
    if (requestPath.startsWith("/api/")) {
      const headers = new Headers(request.headers);
      headers.delete("host");
      const method = request.method ?? "GET";
      const body = method === "GET" || method === "HEAD" ? undefined : await readBody(request);
      const upstream = await fetch(new URL(request.url ?? "/", gateway), { method, headers, body });
      response.writeHead(upstream.status, {
        "content-type": upstream.headers.get("content-type") ?? "application/json",
        "x-correlation-id": upstream.headers.get("x-correlation-id") ?? "",
      });
      response.end(Buffer.from(await upstream.arrayBuffer()));
      return;
    }

    const assetRoot = path.resolve(outputDirectory, "assets");
    const filePath = requestPath.startsWith("/assets/")
      ? path.resolve(outputDirectory, decodeURIComponent(requestPath.slice(1)))
      : path.resolve(root, "index.html");
    if (requestPath.startsWith("/assets/") && !filePath.startsWith(`${assetRoot}${path.sep}`)) {
      response.writeHead(404);
      response.end();
      return;
    }
    const content = await fs.promises.readFile(filePath);
    response.writeHead(200, {
      "content-type": contentTypes.get(path.extname(filePath)) ?? "application/octet-stream",
      "x-content-type-options": "nosniff",
      "x-frame-options": "DENY",
      "referrer-policy": "no-referrer",
    });
    response.end(content);
  } catch (error) {
    response.writeHead(500, { "content-type": "text/plain; charset=utf-8" });
    response.end(error instanceof Error ? error.message : "Development server error");
  }
});

server.listen(port, "0.0.0.0", () => {
  console.log(`SCM frontend available at http://localhost:${port}`);
});

function readBody(request) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    request.on("data", (chunk) => chunks.push(chunk));
    request.on("end", () => resolve(Buffer.concat(chunks)));
    request.on("error", reject);
  });
}
