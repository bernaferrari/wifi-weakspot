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

// Every card draws its own glow, points and waves plus its neighbours', all in panorama coordinates,
// so colour and light spill across each seam and adjacent cards share their edges.
function layer(ctx, k, x0) {
  const {W, H, slides, kind} = ctx;
  const s = slides[k];
  if (!s) return {defs: '', body: ''};
  const accent = s.accent || '#7d8bff';
  const cx = x0 + k * W + W * (k % 2 ? .38 : .62), cy = H * (k % 2 ? .48 : .66);
  const R = Math.max(W, H) * .62;
  const defs = `<radialGradient id="glow${k}" gradientUnits="userSpaceOnUse" cx="${cx}" cy="${cy}" r="${R}"><stop offset="0" stop-color="${hex(accent, .5)}"/><stop offset=".5" stop-color="${hex(accent, .14)}"/><stop offset="1" stop-color="${hex(accent, 0)}"/></radialGradient>`;
  let body = `<rect x="${x0 + (k - 1) * W}" width="${W * 3}" height="${H}" fill="url(#glow${k})"/>`;
  const next = random(k * 131 + kind.length * 17 + 3);
  for (let i = 0; i < 34; i++) {
    const x = x0 + k * W + next() * W, y = H * .25 + next() * H * .75, r = 2 + next() * 6;
    body += `<circle cx="${x.toFixed(1)}" cy="${y.toFixed(1)}" r="${r.toFixed(1)}" fill="${next() < .7 ? '#ffffff' : accent}" opacity="${(.12 + next() * .4).toFixed(2)}"/>`;
  }
  if ((s.layout || '').startsWith('number')) body += [.22, .38, .54, .7].map((r, i) => `<circle cx="${x0 + k * W + W / 2}" cy="${H * .66}" r="${r * H}" fill="none" stroke="${accent}" stroke-opacity="${.18 - i * .04}" stroke-width="${8 - i}"/>`).join('');
  return {defs, body};
}

export default {
  art(ctx) {
    const {W, H, index} = ctx;
    const x0 = -index * W;
    const layers = [index - 1, index, index + 1].map(k => layer(ctx, k, x0));
    return `<defs><linearGradient id="sky" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#0b0e26"/><stop offset="1" stop-color="#121640"/></linearGradient>${layers.map(l => l.defs).join('')}</defs>
      <rect width="${W}" height="${H}" fill="url(#sky)"/>${layers.map(l => l.body).join('')}`;
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
