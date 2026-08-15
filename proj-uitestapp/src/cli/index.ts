#!/usr/bin/env node
import * as fs from 'fs';
import { parseArgs } from './args.js';
import { resolveTarget } from './resolver.js';
import { runAll } from './runner.js';
import { generateHtmlReport } from '../reporter/html.js';
import { generateMarkdownReport } from '../reporter/markdown.js';

async function main(): Promise<void> {
  const parsed = parseArgs(process.argv);
  const { target, headed, slowMo, reporter } = parsed;
  const options = { headed, slowMo, reporter };

  let targets: string[];
  try {
    targets = resolveTarget(target);
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : String(err);
    process.stdout.write(`Error: ${msg}\n`);
    process.exit(1);
  }

  const result = await runAll(targets, options);

  if (reporter === 'html') {
    const html = generateHtmlReport(result);
    fs.writeFileSync('webt-report.html', html, 'utf8');
  } else if (reporter === 'md') {
    const md = generateMarkdownReport(result);
    fs.writeFileSync('webt-report.md', md, 'utf8');
  }

  process.exit(result.failedFlows > 0 ? 1 : 0);
}

main().catch((err: unknown) => {
  const msg = err instanceof Error ? err.message : String(err);
  process.stderr.write(`Fatal error: ${msg}\n`);
  process.exit(1);
});
