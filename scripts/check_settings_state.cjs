/** Verify Settings preserves selections on entry, including slow preference reads. */
const assert = require('node:assert/strict');
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const base = process.env.DEMO_SERVER || 'http://127.0.0.1:8795';

async function waitForChecked(page, role, name) {
  const choice = page.getByRole(role, { name, exact: true });
  for (let i = 0; i < 50; i++) {
    if (await choice.isChecked() || await choice.getAttribute('aria-selected') === 'true') return;
    await page.waitForTimeout(100);
  }
  assert.fail(`${name} never became selected`);
}

(async () => {
  const browser = await chromium.launch();
  try {
    const page = await browser.newPage({ viewport: { width: 390, height: 844 }, locale: 'en-US', reducedMotion: 'reduce' });
    await page.goto(`${base}/?capturePreferencesDelay=700`);
    await page.getByRole('button', { name: 'Allow access', exact: true }).click({ force: true });
    const tab = name => page.getByRole('tab', { name, exact: true });
    await tab('Settings').click({ force: true });
    await page.getByRole('checkbox', { name: 'Auto', exact: true }).waitFor();
    await page.waitForTimeout(800);
    await page.getByRole('radio', { name: 'Green', exact: true }).click({ force: true });
    await waitForChecked(page, 'radio', 'Green');
    await page.getByRole('radio', { name: 'CMF', exact: true }).click({ force: true });
    await waitForChecked(page, 'radio', 'CMF');
    await page.mouse.move(370, 650);
    await page.mouse.wheel(0, 450);
    await page.waitForTimeout(700);
    await page.getByRole('checkbox', { name: 'dBm', exact: true }).click({ force: true });
    await waitForChecked(page, 'checkbox', 'dBm');
    for (const theme of ['Dark', 'Light', 'Auto']) {
      await page.mouse.move(370, 500);
      await page.mouse.wheel(0, -2000);
      await page.waitForTimeout(700);
      await page.getByRole('checkbox', { name: theme, exact: true }).click({ force: true });
      await waitForChecked(page, 'checkbox', theme);
      await tab('Home').click({ force: true });
      await page.getByRole('checkbox', { name: theme, exact: true }).waitFor({ state: 'detached' });
      await tab('Settings').click({ force: true });
      // Assert as soon as the controls appear, before the delayed read can hide a reset.
      await page.getByRole('checkbox', { name: 'Auto', exact: true }).waitFor();
      assert(await page.getByRole('checkbox', { name: theme, exact: true }).isChecked(), `${theme} reset on entry`);
      const palette = page.getByRole('radio', { name: 'Green', exact: true });
      assert(await palette.isChecked() || await palette.getAttribute('aria-selected') === 'true', 'Palette reset on entry');
      const style = page.getByRole('radio', { name: 'CMF', exact: true });
      assert(await style.isChecked() || await style.getAttribute('aria-selected') === 'true', 'Color style reset on entry');
      assert(await page.getByRole('checkbox', { name: 'dBm', exact: true }).isChecked(), 'Signal display reset on entry');
      console.log(`${theme}: theme, palette, color style, and signal display preserved on first frame`);
    }
  } finally {
    await browser.close();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
