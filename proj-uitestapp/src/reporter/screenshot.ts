import * as fs from 'fs';
import * as path from 'path';
import type { Page } from 'playwright';

export async function captureScreenshot(page: Page, flowStem: string, counter: number): Promise<string> {
  const dir = 'screenshots';
  fs.mkdirSync(dir, { recursive: true });
  const padded = String(counter).padStart(3, '0');
  const filename = `${flowStem}-fail-${padded}.png`;
  const screenshotPath = path.join(dir, filename);
  await page.screenshot({ path: screenshotPath });
  return screenshotPath;
}
