// WiFi Weakspot's store screens on the capture emulator. The `screenshots` build (./gradlew :androidApp:assembleScreenshots,
// com.bernaferrari.wifiweakspot.screenshots) has in-memory settings, sample networks and saved example room readings, and
// never scans real networks; it holds the connected sample at 84%. History comes from its ScreenshotActivity, which
// records 45 seconds of an example walk through a weak spot. `npx screenshot-studio recapture` builds and installs it,
// runs this for every language and device size, exports, and refreshes the README images.
// Alone: npx screenshot-studio tour [--profile phone] [--locale de-DE] [--only channels,rooms]

const phone = d => d.profile === 'phone';
const networks = async d => { await d.tap('Networks'); await d.tap('2\\.4 GHz, 18 networks'); };

/** @type {import('screenshot-studio').Tour} */
export default {
  app: 'com.bernaferrari.wifiweakspot.screenshots',
  activity: 'com.bernaferrari.wifiweakspot.android.MainActivity',
  offline: false,  // the app needs no network and never scans
  battery: 84,
  // A full reading would mean the fixture lost its 84% sample.
  check: nodes => !nodes.some(node => `${node.text || ''}${node.desc || ''}`.includes('100%')),
  // Settings live in memory: every launch opens in the app's default dark theme, so light picks Light first.
  async start(d) {
    await d.launch();
    if (d.theme === 'light') { await d.tap('Settings'); await d.tap('Light'); await d.tap('Home'); }
  },
  scenes: {
    light: {
      signal: d => phone(d) ? undefined : false,
      channels: networks,
      'channel-advice': d => phone(d) ? d.tap(/Suggested channel.*/) : false,
      async rooms(d) { if (phone(d)) await d.back(); await d.tap('Rooms'); },
      coverage: d => d.tap('Map your home'),
      async router(d) {
        if (!phone(d)) return false;
        await d.back();
        await d.tap('Home');
        await d.scroll(.5, {x: .8});
        await d.tap('Router check');
        await d.tap('GO');
        await d.waitFor('Router responds.*', {timeout: 20_000});  // the check runs for a few seconds
      },
      async devices(d) {
        if (!phone(d)) return false;
        await d.back();
        await d.tap('Devices');
        await d.waitFor('This device');
      },
      async appearance(d) { await d.back(); await d.tap('Settings'); },
      async history(d) {
        await d.launch({activity: 'com.bernaferrari.wifiweakspot.android.ScreenshotActivity', extras: {moving: true, theme: 'light'}});
        await networks(d);
        await d.tap('Signal over time');
        await d.sleep(45_000);  // the example walk through a weak spot
        if (d.locale === d.defaultLocale && !(await d.isVisible('Signal history for.*'))) throw new Error('The history chart is not on screen');
      },
    },
    dark: {
      // The README's dark settings card: the phone, in English.
      'appearance-dark': d => phone(d) && d.locale === d.defaultLocale ? d.tap('Settings') : false,
    },
  },
};
