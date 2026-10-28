// WiFi Weakspot: every image sits on the app's own coverage map, a soft field of signal colours with the map's
// reading points over it.
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
    dots += `<circle cx="${(x + (next() - .5) * 40).toFixed(1)}" cy="${(y + (next() - .5) * 40).toFixed(1)}" r="8" fill="none" stroke="#c9b8a6" stroke-opacity=".35" stroke-width="3"/>`;
  return `<defs><filter id="soft" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="160"/></filter></defs>
    <rect width="${W}" height="${H}" fill="#fbf6ef"/><g filter="url(#soft)">${blobs.map(([x, y, c]) => `<circle cx="${W * x}" cy="${H * y}" r="${H * .55}" fill="${c}" fill-opacity=".42"/>`).join('')}</g>${dots}`;
}

// Store cards are one walk through the house: a single gradient in the coverage map's own colours runs across the
// whole set, from a weak coral spot to strong green by the middle and back again, so the first four cards (all most
// people see) hold the whole arc and every seam continues. A soft light from the top keeps the copy calm.
const WALK = ['#ff9f8a', '#ffb98c', '#ffd38a', '#e9e48c', '#c4e79a', '#9edfae'];
function walk(W, H, index, count) {
  const there = [...WALK, ...WALK.slice(0, -1).reverse()];
  const stops = there.map((c, i) => `<stop offset="${(i / (there.length - 1)).toFixed(3)}" stop-color="${c}"/>`).join('');
  return `<defs><linearGradient id="walk" gradientUnits="userSpaceOnUse" x1="${-index * W}" y1="0" x2="${(count - index) * W}" y2="0">${stops}</linearGradient>
    <linearGradient id="light" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fffaf3" stop-opacity=".55"/><stop offset=".45" stop-color="#fffaf3" stop-opacity="0"/></linearGradient></defs>
    <rect width="${W}" height="${H}" fill="url(#walk)"/><rect width="${W}" height="${H}" fill="url(#light)"/>`;
}

export default {
  art({W, H, index, slide, slides}) {
    if (W === 2400 && H === 1260) return heatField(W, H, slide);
    return walk(W, H, index, slides.length);
  },

  compose({main, index, kind, slide}) {
    // Each key phrase in the colour of what it describes, from the app's signal scale.
    const sig = {channels: '#178a4c', history: '#c2410c', coverage: '#d23b2a', rooms: '#178a4c', home: '#d23b2a', router: '#c2410c', network: '#4f46c8', appearance: '#7e3fbf', 'readme-hero': '#d23b2a', 'readme-channels': '#178a4c'}[slide.feature];
    if (sig) main.style.setProperty('--sig', sig);
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
