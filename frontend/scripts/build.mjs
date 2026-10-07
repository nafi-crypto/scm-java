import { build } from "esbuild";
import { buildStyles, outputDirectory } from "./build-utils.mjs";

await build({
  entryPoints: ["src/main.tsx"],
  bundle: true,
  minify: true,
  platform: "browser",
  format: "esm",
  target: "es2022",
  jsx: "automatic",
  loader: { ".css": "empty" },
  entryNames: "app",
  outdir: `${outputDirectory}/assets`,
  legalComments: "none",
});

await buildStyles();
