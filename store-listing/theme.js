// WiFi Weakspot: an off-white canvas with a faint tint that drifts across the gallery, Wi-Fi waves
// radiating from behind each device, and a scatter of signal dots. Indigo cards punctuate the set.
import {panoramaGradient} from './studio/shapes.js';

const tint = ['#eef1ff', '#f6f6fb', '#eef7f3', '#f7f3fb', '#f1f3ff'];

// A deterministic scatter, so every export of a card is identical.
const random = seed => () => ((seed = (seed * 16807) % 2147483647) - 1) / 2147483646;

export default {
  art(ctx) {
    const {W, H, slide, index, kind} = ctx;
    const deep = (slide.layout || '').includes('indigo');
    const wave = deep ? '#ffffff' : '#4150d0';
    const portrait = H > W;
    // Waves rise from behind the device, like a signal leaving the phone.
    const cx = portrait ? W / 2 : W * .7, cy = portrait ? H * .62 : H * .55;
    const rings = [.28, .46, .64, .82, 1.0].map((r, i) => {
      const radius = r * Math.max(W, H) * .62;
      return `<circle cx="${cx}" cy="${cy}" r="${radius}" fill="none" stroke="${wave}" stroke-opacity="${(deep ? .16 : .09) - i * .015}" stroke-width="${portrait ? 46 - i * 6 : 40 - i * 5}"/>`;
    }).join('');
    const next = random(index * 97 + (kind.length * 13) + 7);
    const dots = Array.from({length: 22}, () => {
      const x = next() * W, y = H * .35 + next() * H * .65, r = 4 + next() * 10;
      const colors = deep ? ['#ffffff', '#c7ccff', '#9ff2d7'] : ['#4150d0', '#55cf9c', '#ff9b68', '#b8a4ff'];
      return `<circle cx="${x.toFixed(1)}" cy="${y.toFixed(1)}" r="${r.toFixed(1)}" fill="${colors[Math.floor(next() * colors.length)]}" opacity="${(.25 + next() * .4).toFixed(2)}"/>`;
    }).join('');
    const field = deep
      ? `<defs><radialGradient id="deep" cx=".5" cy=".55" r=".8"><stop offset="0" stop-color="#5563ea"/><stop offset="1" stop-color="#2a3198"/></radialGradient></defs><rect width="${W}" height="${H}" fill="url(#deep)"/>`
      : `<defs>${panoramaGradient(ctx, 'tint', tint)}<radialGradient id="glow" cx=".5" cy=".62" r=".6"><stop offset="0" stop-color="#4150d0" stop-opacity=".12"/><stop offset="1" stop-color="#4150d0" stop-opacity="0"/></radialGradient></defs>
         <rect width="${W}" height="${H}" fill="url(#tint)"/><rect width="${W}" height="${H}" fill="url(#glow)"/>`;
    return field + rings + dots;
  }
};
