/** Exercise palette generation, selection, and navigation persistence through the real UI. */
const assert = require('node:assert/strict');
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const base = process.env.DEMO_SERVER || 'http://127.0.0.1:8795';
const names = ['Expressive', 'Vibrant', 'Tonal', 'CMF'];
async function selected(choice) {
  return await choice.getAttribute('aria-selected') === 'true' || await choice.getAttribute('aria-checked') === 'true';
}
async function reveal(page, choice) {
  for (let i = 0; i < 14; i++) {
    const box = await choice.count() ? await choice.boundingBox() : null;
    const viewport = page.viewportSize();
    if (box && box.y >= 64 && box.y + box.height <= viewport.height - (viewport.width>=840 ? 16 : 80)) return;
    await page.mouse.move(viewport.width * .65, viewport.height * .7);
    await page.mouse.wheel(0, box && box.y < 64 ? -220 : 220);
    await page.waitForTimeout(150);
  }
  assert.fail('Could not reveal style choice');
}
(async () => {
  const browser = await chromium.launch();
  try {
    for (const [mode, viewport] of [['light', {width:390,height:844}], ['dark', {width:960,height:600}]]) {
      const page = await browser.newPage({viewport, colorScheme:mode, locale:'en-US', reducedMotion:'reduce'});
      const errors=[]; page.on('pageerror', e=>errors.push(e.message));
      await page.goto(`${base}/?capturePreferencesDelay=100`);
      await page.getByRole('button',{name:'Allow access',exact:true}).click({force:true});
      await page.getByRole('tab',{name:'Settings',exact:true}).click({force:true});
      await page.waitForTimeout(650);
      let previous;
      for (const name of names) {
        const choice=page.getByRole('radio',{name,exact:true}); await reveal(page,choice);
        await choice.click({force:true});
        for (let i=0;i<30&&!await selected(choice);i++) await page.waitForTimeout(100);
        assert(await selected(choice), `${name} did not select`);
        for (const other of names.filter(n=>n!==name)) assert(!await selected(page.getByRole('radio',{name:other,exact:true})), 'Multiple styles selected');
        await page.waitForTimeout(200);
        // The navigation chrome is outside the picker: a change here proves the app theme applied.
        const clip=viewport.width>=840 ? {x:0,y:0,width:96,height:viewport.height}
          : {x:0,y:viewport.height-72,width:viewport.width,height:72};
        const colors=await page.screenshot({clip});
        if(previous) assert(!previous.equals(colors), `${name} did not change the app colors`);
        previous=colors;
      }
      await page.getByRole('tab',{name:'Home',exact:true}).click({force:true});
      await page.waitForTimeout(300);
      await page.getByRole('tab',{name:'Settings',exact:true}).click({force:true});
      const cmf=page.getByRole('radio',{name:'CMF',exact:true}); await cmf.waitFor();
      assert(await selected(cmf), 'Saved CMF style reset when returning to Settings');
      assert.equal(errors.length,0,errors.join('\n'));
      console.log(`${mode}: all four styles apply, exactly one selected, CMF preserved on return`);
      await page.close();
    }
  } finally {await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
