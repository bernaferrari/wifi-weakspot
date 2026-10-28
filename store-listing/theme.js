// WiFi Weakspot: one night sky under the whole strip. Its stars are the networks around you, scattered evenly through
// every card and drawn in panorama coordinates so they cross each seam; one soft indigo glow lights every phone.
const random = seed => () => ((seed = (seed * 16807) % 2147483647) - 1) / 2147483646;

// The app's verdict badge: a scalloped shape in the verdict's colour with the percentage inside.
const cookie = (cx, cy, r, colour, label) => {
  const pts = [];
  for (let i = 0; i < 360; i++) { const t = i / 360 * Math.PI * 2, rr = r * (.93 + .07 * Math.cos(9 * t)); pts.push(`${(cx + rr * Math.cos(t)).toFixed(1)},${(cy + rr * Math.sin(t)).toFixed(1)}`); }
  return `<path d="M${pts.join('L')}Z" fill="${colour}"/><text x="${cx}" y="${cy + r * .26}" text-anchor="middle" font-family="Google Sans Flex" font-weight="760" font-size="${r * .72}" fill="#fff">${label}</text>`;
};

// README art sits on the app's own coverage-map colours: a soft field from weak coral through amber to strong
// green, blurred like the map, with the map's reading points scattered over it.
function heatField(W, H, slide) {
  const blobs = slide.field || [[.1, .2, '#ff8a78'], [.35, .85, '#ffc46b'], [.62, .25, '#ffd98a'], [.85, .8, '#6fd6a0'], [1.0, .15, '#8fe0b4']];
  // The map's reading points: a loose grid, each point a small ring, exactly as the coverage map draws them.
  const next = random(W + (slide.file || '').length * 31);
  let dots = '';
  for (let y = 70; y < H; y += 150) for (let x = 60 + (y / 150 % 2) * 75; x < W; x += 150)
    dots += `<circle cx="${(x + (next() - .5) * 40).toFixed(1)}" cy="${(y + (next() - .5) * 40).toFixed(1)}" r="8" fill="none" stroke="#ffffff" stroke-opacity=".55" stroke-width="3.5"/>`;
  return `<defs><filter id="soft" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="160"/></filter></defs>
    <rect width="${W}" height="${H}" fill="#fff4ea"/><g filter="url(#soft)">${blobs.map(([x, y, c]) => `<circle cx="${W * x}" cy="${H * y}" r="${H * .55}" fill="${c}" fill-opacity=".8"/>`).join('')}</g>${dots}`;
}

export default {
  art({W, H, index, slides, slide}) {
    if (W === 2400 && H === 1260) return heatField(W, H, slide);
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
