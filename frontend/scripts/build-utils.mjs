import fs from "node:fs/promises";
import path from "node:path";
import postcss from "postcss";
import tailwindcss from "@tailwindcss/postcss";

export const root = process.cwd();
export const outputDirectory = path.join(root, "dist");

export async function buildStyles() {
  const input = path.join(root, "src", "styles.css");
  const output = path.join(outputDirectory, "assets", "styles.css");
  await fs.mkdir(path.dirname(output), { recursive: true });
  const source = await fs.readFile(input, "utf8");
  const result = await postcss([tailwindcss()])
    .process(source, { from: input, to: output });
  await fs.writeFile(output, result.css);
}
