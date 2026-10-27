// WiFi Weakspot: an off-white canvas with one faint tint that drifts across the whole gallery, so the
// cards join in the Play carousel without competing with the app.
import {panoramaGradient} from './studio/shapes.js';

const tint = ['#f3f5ff', '#f6f6fb', '#f1f8f5', '#f8f5fb', '#f5f6ff'];

export default {
  art(ctx) {
    const {W, H, slide} = ctx;
    // Indigo cards punctuate the carousel; their light neighbours keep the panorama tint.
    if ((slide.layout || '').includes('indigo')) return `<defs><radialGradient id="deep" cx=".5" cy=".55" r=".75"><stop offset="0" stop-color="#4b58e0"/><stop offset="1" stop-color="#2b3299"/></radialGradient></defs><rect width="${W}" height="${H}" fill="url(#deep)"/>`;
    return `<defs>${panoramaGradient(ctx, 'tint', tint)}
      <radialGradient id="glow" cx=".5" cy=".62" r=".6"><stop offset="0" stop-color="#4150d0" stop-opacity=".09"/><stop offset="1" stop-color="#4150d0" stop-opacity="0"/></radialGradient></defs>
      <rect width="${W}" height="${H}" fill="url(#tint)"/><rect width="${W}" height="${H}" fill="url(#glow)"/>`;
  }
};
