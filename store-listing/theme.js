// WiFi Weakspot: faint Wi-Fi ripples drawn in panorama coordinates (so they continue across every seam), and one
// big Material 3 Expressive shape per card in that card's soft colour, sitting behind the phone.
import {panoramaX, shapes} from './studio/shapes.js';

const order = ['cookie', 'sunny', 'flower', 'clover', 'cookie', 'sunny', 'flower'];

// Where the shape sits on each layout, as fractions of the card.
const anchor = (layout, landscape) =>
  landscape ? {x: .7, y: .55, r: .44}
  : /drop/.test(layout) ? {x: .5, y: .28, r: .6}
  : {x: .5, y: .64, r: .6};

export default {
  art(ctx) {
    const {W, H, index, slides, slide} = ctx;
    const landscape = W > H, unit = Math.max(W, H);
    let body = `<rect width="${W}" height="${H}" fill="#f6f5fc"/>`;
    // Ripples centred on every other seam, alternating high and low, so neighbouring cards share them.
    for (let k = 0; k <= slides.length; k += 1) {
      const cx = panoramaX(ctx, k), cy = H * (k % 2 ? .12 : .9);
      if (cx < -unit * 1.2 || cx > W + unit * 1.2) continue;
      for (let i = 1; i <= 7; i++) body += `<circle cx="${cx}" cy="${cy}" r="${i * unit * .13}" fill="none" stroke="#5a55c8" stroke-opacity="${(.1 - i * .008).toFixed(3)}" stroke-width="3"/>`;
    }
    if (slide.title) {
      const a = anchor(slide.layout || '', landscape);
      const shape = shapes[order[index % order.length]];
      body += `<g fill="var(--soft)">${shape(W * a.x, H * a.y, Math.min(W, H) * a.r * (landscape ? 1.4 : 1), index * .4)}</g>`;
    }
    return body;
  },

  compose({slide, main, index, kind}) {
    if (slide.deep) main.style.setProperty('--deep', slide.deep);
    if (slide.soft) main.style.setProperty('--soft', slide.soft);
    if (index === 0 && kind !== 'readme' && kind !== 'desktop') {
      main.classList.add('first');
      const brand = document.createElement('div');
      brand.className = 'brand';
      brand.dataset.checkBounds = '';
      brand.innerHTML = '<img src="../../docs/app-icon.png" alt="">WiFi Weakspot';
      main.append(brand);
    }
  },

  /** Bezel and corners scale with the device width. */
  layout({main}) {
    for (const node of main.querySelectorAll('.device')) {
      const image = node.querySelector('img'), width = node.clientWidth, landscape = image.naturalWidth > image.naturalHeight;
      node.style.setProperty('--bezel', `${Math.round(width * (landscape ? .011 : .02))}px`);
      node.style.setProperty('--radius', `${Math.round(width * (landscape ? .035 : .09))}px`);
    }
  }
};
