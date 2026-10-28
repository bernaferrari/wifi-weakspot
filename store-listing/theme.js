// WiFi Weakspot: a faint dot grid (the coverage map's floor plan) in panorama coordinates, a coloured block per card,
// and on the hero card the app's scalloped signal badge, redrawn in vector and lifted off the screen.
import {shapes} from './studio/shapes.js';

function badge(main, {value, label, colour}) {
  const node = document.createElement('figure');
  node.className = 'badge';
  node.style.margin = '0';
  node.setAttribute('role', 'img');
  node.setAttribute('aria-label', `${label}, ${value}`);
  node.innerHTML = `<svg viewBox="0 0 400 400"><circle cx="200" cy="200" r="196" fill="#ffffff"/><g fill="${colour}">${shapes.cookie(200, 200, 168, .1)}</g>` +
    `<text x="200" y="230" text-anchor="middle" font-family="Google Sans Flex" font-weight="760" font-size="104" letter-spacing="-3" fill="#ffffff">${value}</text></svg>`;
  main.append(node);
}

export default {
  art({W, H, index}) {
    const gap = Math.round(Math.min(W, H) / 16), x0 = -index * W, off = ((x0 % gap) + gap) % gap;
    return `<defs><pattern id="dots" width="${gap}" height="${gap}" patternUnits="userSpaceOnUse" x="${off}"><circle cx="${gap / 2}" cy="${gap / 2}" r="2.6" fill="#3f46c8" fill-opacity=".13"/></pattern></defs>` +
      `<rect width="${W}" height="${H}" fill="#f7f6fb"/><rect width="${W}" height="${H}" fill="url(#dots)"/>`;
  },

  compose({slide, main, index, kind}) {
    if (slide.deep) main.style.setProperty('--deep', slide.deep);
    if (slide.block) main.style.setProperty('--block', slide.block);
    if (index === 0 && kind !== 'readme' && kind !== 'desktop') {
      const brand = document.createElement('div');
      brand.className = 'brand';
      brand.dataset.checkBounds = '';
      brand.innerHTML = '<img src="../../docs/app-icon.png" alt="">WiFi Weakspot';
      main.append(brand);
    }
    if (slide.block && !/map/.test(slide.layout || '')) {
      const block = document.createElement('div');
      block.className = 'block';
      block.setAttribute('aria-hidden', 'true');
      main.append(block);
    }
    if (slide.lift) badge(main, slide.lift);
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
