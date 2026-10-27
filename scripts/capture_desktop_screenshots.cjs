/** Capture the production browser demo at store resolution, with per-locale UI and layout sidecars.
 * Serve webDemo/build/dist/wasmJs/productionExecutable first; then:
 * PLAYWRIGHT_MODULE=/path/to/playwright node scripts/capture_desktop_screenshots.cjs --url http://127.0.0.1:8835 --locale de-DE
 */
const fs = require('node:fs');
const path = require('node:path');
const {parseArgs} = require('node:util');
const {chromium} = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const {values: args} = parseArgs({options: {url: {type: 'string'}, locale: {type: 'string', default: 'en-US'}}});
if (!args.url) throw Error('Pass --url for the locally served production browser demo');
if (!/^[a-z]{2,3}(-[A-Za-z0-9]{2,8})*$/.test(args.locale)) throw Error('Invalid locale');
const repo = path.resolve(__dirname, '..');
const reference = JSON.parse(fs.readFileSync(path.join(repo, 'store-listing/studio.json'))).defaultLocale || 'en-US';
const raw = path.join(repo, 'store-listing', args.locale, 'raw/desktop');
const mapFile = path.join(repo, 'store-listing', reference, 'raw/tap-map-desktop.json');
const recording = args.locale === reference;
const taps = fs.existsSync(mapFile) ? JSON.parse(fs.readFileSync(mapFile)) : {};
if (!recording && !Object.keys(taps).length) throw Error('Capture the reference locale first');

(async () => {
  const browser = await chromium.launch();
  try {
    // Separate contexts keep the theme and demo data independent and make repeats reproducible.
    for (const scheme of ['dark', 'light']) {
      const context = await browser.newContext({viewport: {width: 1280, height: 720}, deviceScaleFactor: 1.5,
        locale: args.locale, colorScheme: scheme, reducedMotion: 'reduce'});
      const page = await context.newPage();
      const errors = [];
      page.on('pageerror', e => errors.push(e.message));
      await page.goto(args.url);
      await page.getByRole('button').first().waitFor();
      if ((await page.locator('html').getAttribute('lang')).split('-')[0] !== args.locale.split('-')[0]) throw Error('Browser app language did not switch');
      const click = async (role, label) => {
        const controls = page.getByRole(role);
        const key = `${scheme}|${role}|${label}`;
        let control;
        if (recording) {
          control = page.getByRole(role, {name: label, exact: true});
          await control.waitFor();
          const all = await controls.all();
          let index = -1;
          for (let i = 0; i < all.length; i++) if (await all[i].evaluate((el, target) => el === target, await control.elementHandle())) index = i;
          if (index < 0) throw Error(`Cannot record ${key}`);
          taps[key] = index;
        } else {
          if (!(key in taps)) throw Error(`Missing reference tap ${key}`);
          control = controls.nth(taps[key]);
          await control.waitFor();
        }
        // Compose renders into a canvas over its accessibility controls; hit the recorded bounds.
        await control.click({force: true});
        await page.waitForTimeout(600);
      };
      const capture = async name => {
        await page.waitForTimeout(600);
        if (errors.length) throw Error(errors.join('\n'));
        fs.mkdirSync(raw, {recursive: true});
        const file = path.join(raw, `${name}.jpg`);
        await page.screenshot({path: file, type: 'jpeg', quality: 95});
        const nodes = await page.locator('body').evaluate(body => {
          const result = [];
          function visit(el, key) {
            const rect = el.getBoundingClientRect();
            if (rect.width > 0 && rect.height > 0) result.push({path: key, b: [rect.left, rect.top, rect.right, rect.bottom].map(v => Math.round(v * 1.5)),
              ...(el.getAttribute('role') ? {cls: el.getAttribute('role')} : {}),
              ...(el.getAttribute('aria-label') ? {desc: el.getAttribute('aria-label')} : {})});
            [...el.children].forEach((child, i) => visit(child, `${key}.${i}`));
          }
          visit(body, '0');
          return result;
        });
        fs.writeFileSync(path.join(raw, `${name}.nodes.json`), JSON.stringify({source: 'browser-accessibility', width: 1920, height: 1080, nodes}) + '\n');
        console.log(`${args.locale}/raw/desktop/${name}.jpg`);
      };
      await click('button', 'Allow access');
      if (scheme === 'dark') {
        await capture('01-home');
        await click('tab', 'Networks');
        await click('checkbox', '2.4 GHz, 18 networks');
        await capture('02-channels');
        await click('checkbox', 'Signal over time');
        await page.waitForTimeout(12000);
        await capture('03-history');
      } else {
        await click('tab', 'Settings');
        await capture('04-appearance');
      }
      await context.close();
    }
  } finally {
    if (recording) {
      fs.mkdirSync(path.dirname(mapFile), {recursive: true});
      fs.writeFileSync(mapFile, JSON.stringify(taps, null, 2) + '\n');
    }
    await browser.close();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
