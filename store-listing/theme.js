// WiFi Weakspot: a night-sky family where each card has its own glow and a scatter of light points
// (waves only on the router card), plus a small label above the title and one big router number.
const random = seed => () => ((seed = (seed * 16807) % 2147483647) - 1) / 2147483646;
const hex = (c, a) => c + Math.round(a * 255).toString(16).padStart(2, '0');

const motifs = {
  // Points of light, like readings or nearby networks; no lines.
  points({W, H, accent, next}) {
    return Array.from({length: 34}, () => {
      const r = 2 + next() * 6;
      return `<circle cx="${(next() * W).toFixed(1)}" cy="${(H * .28 + next() * H * .72).toFixed(1)}" r="${r.toFixed(1)}" fill="${next() < .7 ? '#ffffff' : accent}" opacity="${(.12 + next() * .4).toFixed(2)}"/>`;
    }).join('');
  },
  // Radiating waves, only where a signal leaves a device.
  waves({W, H, accent}) {
    return [.22, .38, .54, .7].map((r, i) => `<circle cx="${W / 2}" cy="${H * .66}" r="${r * H}" fill="none" stroke="${accent}" stroke-opacity="${.18 - i * .04}" stroke-width="${8 - i}"/>`).join('');
  }
};
const motifFor = {number: 'waves'};

export default {
  art(ctx) {
    const {W, H, slide, index, kind} = ctx;
    const accent = slide.accent || '#7d8bff';
    const layout = (slide.layout || '').split(' ')[0];
    const next = random(index * 131 + kind.length * 17 + 3);
    const glowY = layout === 'stack' ? .5 : .62;
    const glow = `<radialGradient id="g" cx="${index % 2 ? .35 : .65}" cy="${glowY}" r=".75"><stop offset="0" stop-color="${hex(accent, .55)}"/><stop offset=".55" stop-color="${hex(accent, .12)}"/><stop offset="1" stop-color="${hex(accent, 0)}"/></radialGradient>`;
    const motif = motifs[motifFor[layout] || 'points']({W, H, accent, next});
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
