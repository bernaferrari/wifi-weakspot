// WiFi Weakspot: one night sky under the whole strip. Its stars are the networks around you, scattered evenly through
// every card and drawn in panorama coordinates so they cross each seam; one soft indigo glow lights every phone.
const random = seed => () => ((seed = (seed * 16807) % 2147483647) - 1) / 2147483646;

// The app's verdict badge: a scalloped shape in the verdict's colour with the percentage inside.
const cookie = (cx, cy, r, colour, label) => {
  const pts = [];
  for (let i = 0; i < 360; i++) { const t = i / 360 * Math.PI * 2, rr = r * (.93 + .07 * Math.cos(9 * t)); pts.push(`${(cx + rr * Math.cos(t)).toFixed(1)},${(cy + rr * Math.sin(t)).toFixed(1)}`); }
  return `<path d="M${pts.join('L')}Z" fill="${colour}"/><text x="${cx}" y="${cy + r * .26}" text-anchor="middle" font-family="Google Sans Flex" font-weight="760" font-size="${r * .72}" fill="#fff">${label}</text>`;
};

// Wi-Fi rings spreading from the router check's answer across the whole image.
function rings() {
  return `<svg class="rings-art" viewBox="0 0 2400 1260" aria-hidden="true">${[180, 330, 480, 640, 810, 990, 1180].map((r, i) =>
    `<circle cx="810" cy="620" r="${r}" fill="none" stroke="#aab3ff" stroke-opacity="${(.55 - i * .06).toFixed(2)}" stroke-width="${8 - i * .8}"/>`).join('')}</svg>`;
}

export default {
  art({W, H, index, slides, slide}) {
    const landscape = W > H;
    let stars = '';
    for (let k = index - 1; k <= index + 1; k++) {
      if (k < 0 || k >= slides.length) continue;
      const next = random(k * 131 + 7), x0 = (k - index) * W;
      for (let i = 0; i < 46; i++) {
        const x = x0 + next() * W, y = next() * H, r = .9 + next() * 2.6, o = .25 + next() * .6;
        stars += `<circle cx="${x.toFixed(1)}" cy="${y.toFixed(1)}" r="${r.toFixed(2)}" fill="#ffffff" fill-opacity="${o.toFixed(2)}"/>`;
      }
      // A few brighter stars with a halo: the strongest networks.
      for (let i = 0; i < 3; i++) {
        const x = x0 + next() * W, y = next() * H;
        stars += `<circle cx="${x.toFixed(1)}" cy="${y.toFixed(1)}" r="14" fill="#a9b4ff" fill-opacity=".18"/><circle cx="${x.toFixed(1)}" cy="${y.toFixed(1)}" r="4" fill="#ffffff"/>`;
      }
    }
    // The glow sits where the phone is: low on most cards, high when the phone hangs from the top.
    const high = /drop/.test(slide.layout || ''), mid = /stack/.test(slide.layout || '');
    const gx = landscape ? W * .7 : W * .5, gy = landscape ? H * .55 : H * (high ? .25 : mid ? .5 : .72);
    const R = Math.max(W, H) * .55;
    return `<defs><linearGradient id="sky" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#090b22"/><stop offset="1" stop-color="#141945"/></linearGradient>
      <radialGradient id="glow" gradientUnits="userSpaceOnUse" cx="${gx}" cy="${gy}" r="${R}"><stop offset="0" stop-color="#5b63ff" stop-opacity=".42"/><stop offset=".55" stop-color="#5b63ff" stop-opacity=".1"/><stop offset="1" stop-color="#5b63ff" stop-opacity="0"/></radialGradient></defs>
      <rect width="${W}" height="${H}" fill="url(#sky)"/><rect width="${W}" height="${H}" fill="url(#glow)"/>${stars}`;
  },

  compose({main, index, kind, slide}) {
    if (slide.layout === 'rings') main.insertAdjacentHTML('beforeend', rings());
    if (index === 0 && kind !== 'readme' && kind !== 'desktop') {
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
