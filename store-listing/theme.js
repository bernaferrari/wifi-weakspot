// WiFi Weakspot: every image sits on the app's own coverage map, a soft field of signal colours with the map's
// reading points over it.

// The app's verdict badge: a scalloped shape in the verdict's colour with the percentage inside.
const cookie = (cx, cy, r, colour, label) => {
  const pts = [];
  for (let i = 0; i < 360; i++) { const t = i / 360 * Math.PI * 2, rr = r * (.93 + .07 * Math.cos(9 * t)); pts.push(`${(cx + rr * Math.cos(t)).toFixed(1)},${(cy + rr * Math.sin(t)).toFixed(1)}`); }
  return `<path d="M${pts.join('L')}Z" fill="${colour}"/><text x="${cx}" y="${cy + r * .26}" text-anchor="middle" font-family="Google Sans Flex" font-weight="760" font-size="${r * .72}" fill="#fff">${label}</text>`;
};

// README art uses the store cards' clean gradient, one slice of the signal scale per image, light on the side the
// words sit, with the same soft light from the top.
function wash(W, H, slide) {
  const colours = slide.wash || ['#fff3df', '#ffd8a8', '#ffb9a0'];
  const stops = colours.map((c, i) => `<stop offset="${(i / (colours.length - 1)).toFixed(3)}" stop-color="${c}"/>`).join('');
  return `<defs><linearGradient id="wash" x1="0" y1="0" x2="1" y2="0">${stops}</linearGradient>
    <linearGradient id="light" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#fffaf3" stop-opacity=".55"/><stop offset=".5" stop-color="#fffaf3" stop-opacity="0"/></linearGradient></defs>
    <rect width="${W}" height="${H}" fill="url(#wash)"/><rect width="${W}" height="${H}" fill="url(#light)"/>`;
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
    if (W === 2400 && H === 1260) return wash(W, H, slide);
    return walk(W, H, index, slides.length);
  },

  compose({main, index, kind, slide}) {
    // The highlighter under each key phrase: one channel wave, repeated, in the colour of what the words describe.
    const wave = colour => `url("data:image/svg+xml,${encodeURIComponent(`<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100' preserveAspectRatio='none'><path d='M0 58 C25 58 25 40 50 40 S75 58 100 58 L100 92 C75 92 75 74 50 74 S25 92 0 92 Z' fill='${colour}'/></svg>`)}")`;
    const colours = {coral: '#ffc4b8', green: '#b6ecc8', amber: '#ffe2a3', violet: '#dcd6ff'};
    for (const [name, colour] of Object.entries(colours)) main.style.setProperty(`--wave-${name}`, wave(colour));
    const tone = {channels: 'green', history: 'amber', coverage: 'coral', rooms: 'green', home: 'coral', router: 'amber', network: 'violet', appearance: 'violet', 'readme-hero': 'coral', 'readme-channels': 'green', 'readme-tools': 'amber'}[slide.feature];
    if (tone) main.style.setProperty('--wave', `var(--wave-${tone})`);
    // The README's first image is the app's name card: icon and name, large, over the real app; nothing else.
    if (slide.identity) {
      main.querySelector('header')?.remove();
      const id = document.createElement('div');
      id.className = 'identity';
      id.dataset.checkBounds = '';
      id.innerHTML = '<img src="../../docs/app-icon.png" alt="">WiFi Weakspot';
      main.append(id);
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
