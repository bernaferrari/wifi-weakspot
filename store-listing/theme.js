// WiFi Weakspot: a night-sky family where each card has its own glow and motif (a lit floor grid,
// flowing signal lines, radiating waves, a constellation of readings), plus a small label above
// the title and, on the router card, one big number.
const random = seed => () => ((seed = (seed * 16807) % 2147483647) - 1) / 2147483646;
const hex = (c, a) => c + Math.round(a * 255).toString(16).padStart(2, '0');

const motifs = {
  // A perspective grid floor under the floating graph.
  grid({W, H, accent}) {
    let lines = '';
    const horizon = H * .5, vx = W / 2;
    for (let i = -10; i <= 10; i++) lines += `<line x1="${vx}" y1="${horizon}" x2="${vx + i * W * .18}" y2="${H}" stroke="${accent}" stroke-opacity=".16" stroke-width="2"/>`;
    for (let k = 1; k <= 9; k++) { const y = horizon + (H - horizon) * (k / 9) ** 1.8; lines += `<line x1="0" y1="${y}" x2="${W}" y2="${y}" stroke="${accent}" stroke-opacity="${.05 + k * .015}" stroke-width="2"/>`; }
    return lines;
  },
  // Flowing signal lines, one dipping like a walk through a weak spot.
  flow({W, H, accent, next}) {
    let paths = '';
    for (let l = 0; l < 7; l++) {
      const base = H * (.42 + l * .07), pts = [];
      for (let x = -40; x <= W + 40; x += 20) {
        const dip = l === 3 ? -H * .12 * Math.exp(-(((x / W) - .55) ** 2) * 40) : 0;
        pts.push(`${x},${(base + Math.sin(x / W * 5 + l) * H * .015 - dip).toFixed(1)}`);
      }
      paths += `<path d="M${pts.join('L')}" fill="none" stroke="${accent}" stroke-opacity="${l === 3 ? .55 : .12 + next() * .1}" stroke-width="${l === 3 ? 6 : 3}"/>`;
    }
    return paths;
  },
  // Radiating waves from behind the device.
  waves({W, H, accent}) {
    return [.22, .38, .54, .7, .86].map((r, i) => `<circle cx="${W / 2}" cy="${H * .66}" r="${r * H}" fill="none" stroke="${accent}" stroke-opacity="${.22 - i * .035}" stroke-width="${10 - i}"/>`).join('');
  },
  // A constellation of readings joined by faint lines.
  stars({W, H, accent, next}) {
    const pts = Array.from({length: 26}, () => [next() * W, H * .3 + next() * H * .7]);
    let out = '';
    pts.forEach(([x, y], i) => {
      const [x2, y2] = pts[(i * 7 + 3) % pts.length];
      if (Math.hypot(x - x2, y - y2) < W * .5) out += `<line x1="${x}" y1="${y}" x2="${x2}" y2="${y2}" stroke="${accent}" stroke-opacity=".12" stroke-width="2"/>`;
      out += `<circle cx="${x}" cy="${y}" r="${3 + next() * 7}" fill="${accent}" opacity="${.25 + next() * .5}"/>`;
    });
    return out;
  }
};
const motifFor = {hero: 'grid', bleed: 'flow', floor: 'stars', float: 'stars', number: 'waves', stack: 'flow', duo: 'waves'};

export default {
  art(ctx) {
    const {W, H, slide, index, kind} = ctx;
    const accent = slide.accent || '#7d8bff';
    const layout = (slide.layout || '').split(' ')[0];
    const next = random(index * 131 + kind.length * 17 + 3);
    const glowY = layout === 'stack' ? .5 : .62;
    const glow = `<radialGradient id="g" cx="${index % 2 ? .35 : .65}" cy="${glowY}" r=".75"><stop offset="0" stop-color="${hex(accent, .55)}"/><stop offset=".55" stop-color="${hex(accent, .12)}"/><stop offset="1" stop-color="${hex(accent, 0)}"/></radialGradient>`;
    const motif = motifs[motifFor[layout] || (index % 2 ? 'stars' : 'waves')]({W, H, accent, next});
    return `<defs>${glow}<linearGradient id="sky" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#0b0e26"/><stop offset="1" stop-color="#121640"/></linearGradient></defs>
      <rect width="${W}" height="${H}" fill="url(#sky)"/><rect width="${W}" height="${H}" fill="url(#g)"/>${motif}`;
  },
  compose({slide, main}) {
    if (slide.accent) main.style.setProperty('--accent', slide.accent);
    const header = main.querySelector('header');
    if (header && slide.badge) {
      const badge = document.createElement('div');
      badge.className = 'badge';
      badge.textContent = slide.badge;
      header.prepend(badge);
    }
    if (slide.stat) {
      const stat = document.createElement('div');
      stat.className = 'stat';
      stat.textContent = slide.stat;
      main.append(stat);
    }
  }
};
