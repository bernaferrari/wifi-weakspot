/** Exercise Router check and the adaptive navigation controls in the web demo. */
const assert = require('node:assert/strict');
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const base = process.env.DEMO_SERVER || 'http://127.0.0.1:8795';

(async () => {
  const browser = await chromium.launch();
  try {
    const page = await browser.newPage({ viewport: { width: 1280, height: 900 }, locale: 'en-US' });
    page.setDefaultTimeout(10_000);
    const errors = [];
    page.on('pageerror', error => errors.push(error.message));
    const click = locator => locator.click({ force: true });
    const tab = name => page.getByRole('tab', { name, exact: true });
    const selected = name => page.getByRole('tab', { name, exact: true, selected: true });
    const select = async name => {
      await click(tab(name));
      await selected(name).waitFor();
    };
    const router = page.getByRole('button', { name: /^Router check/ });
    const back = page.getByRole('button', { name: 'Back', exact: true });

    await page.goto(base);
    await click(page.getByRole('button', { name: 'Allow access', exact: true }));
    await router.waitFor();
    const railHome = await tab('Home').boundingBox();
    const railBefore = await page.screenshot({ clip: railHome });
    await click(router);
    await back.waitFor();
    await selected('Home').waitFor();
    assert.deepEqual(await tab('Home').boundingBox(), railHome, 'Opening Router check moved the rail');
    // Sample the rail after the screen transition settles. Its selected Home item stays visible.
    await page.waitForTimeout(400);
    const railAfter = await page.screenshot({ clip: railHome });
    assert(railBefore.equals(railAfter), 'Router check painted over the Home rail item');

    await select('Settings');
    await back.waitFor({ state: 'hidden' });
    await select('Home');
    await back.waitFor();

    await page.setViewportSize({ width: 390, height: 844 });
    await back.waitFor();
    await selected('Home').waitFor();
    await page.setViewportSize({ width: 1280, height: 900 });
    await back.waitFor();
    await click(back);
    await back.waitFor({ state: 'hidden' });
    await router.waitFor();

    await click(router);
    await back.waitFor();
    await select('Home');
    await back.waitFor({ state: 'hidden' });
    await router.waitFor();

    for (const name of ['Networks', 'Rooms', 'Settings', 'Home', 'Rooms', 'Home']) {
      await select(name);
    }
    assert.deepEqual(errors, []);
    console.log('Router check opens in the rail layout, survives tab and window changes, and closes with Back or Home reselect.');
  } finally {
    await browser.close();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
