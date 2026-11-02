// WiFi Weakspot's store screens on the capture emulator. The `screenshots` build (./gradlew :androidApp:assembleScreenshots,
// com.bernaferrari.wifiweakspot.screenshots) has in-memory settings, sample networks and saved example room readings, and
// never scans real networks; it holds the connected sample at 84%. History comes from its ScreenshotActivity, which
// records 45 seconds of an example walk through a weak spot. `npx screenshot-studio recapture` builds and installs it,
// runs this for every language and device size, exports, and refreshes the README images.
// Alone: npx screenshot-studio tour [--profile phone] [--locale de-DE] [--only channels,rooms]

const tap = (d, label) => d.tap(label, {wait: 1000, attempts: 20});
const phone = d => d.profile === 'phone';
const networks = d => tap(d, '2\\.4 GHz, 18 networks');

/** @type {import('screenshot-studio').Tour} */
export default {
  app: 'com.bernaferrari.wifiweakspot.screenshots',
  activity: 'com.bernaferrari.wifiweakspot.android.MainActivity',
  offline: false,  // the app needs no network and never scans
  battery: 84,
  settle: 0,
  // A full reading would mean the fixture lost its 84% sample.
  check: nodes => !nodes.some(node => `${node.text || ''}${node.desc || ''}`.includes('100%')),
  // Settings live in memory: every launch opens in the app's default dark theme, so light picks Light first.
  async start(d) {
    await d.launch({wait: 2000});
    if (d.theme === 'light') { await tap(d, 'Settings'); await tap(d, 'Light'); await tap(d, 'Home'); }
  },
  scenes: {
    light: {
      signal: d => phone(d) ? undefined : false,
      async channels(d) { await tap(d, 'Networks'); await networks(d); },
      'channel-advice': d => phone(d) ? tap(d, /Suggested channel.*/) : false,
      async rooms(d) { if (phone(d)) await d.back(500); await tap(d, 'Rooms'); },
      coverage: d => tap(d, 'Map your home'),
      async router(d) {
        if (!phone(d)) return false;
        await d.back(500);
        await tap(d, 'Home');
        await d.scroll(.5, {x: .8, wait: 400});
        await tap(d, 'Router check');
        await tap(d, 'GO');
        await d.sleep(8000);
      },
      async devices(d) {
        if (!phone(d)) return false;
        await d.back(500);
        await tap(d, 'Devices');
        await d.sleep(4000);
      },
      async appearance(d) { await d.back(500); await tap(d, 'Settings'); },
      async history(d) {
        await d.launch({wait: 2000, activity: 'com.bernaferrari.wifiweakspot.android.ScreenshotActivity', extras: {moving: true, theme: 'light'}});
        await tap(d, 'Networks');
        await networks(d);
        await tap(d, 'Signal over time');
        await d.sleep(45_000);  // the example walk through a weak spot
        if (d.locale === d.defaultLocale && !(await d.tree()).some(node => node.desc?.startsWith('Signal history for')))
          throw new Error('The history chart is not on screen');
      },
    },
    dark: {
      // The README's dark settings card: the phone, in English.
      'appearance-dark': d => phone(d) && d.locale === d.defaultLocale ? tap(d, 'Settings') : false,
    },
  },
};
