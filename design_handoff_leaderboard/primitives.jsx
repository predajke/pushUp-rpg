// Shared Leaderboard primitives — RPG / Diablo vibe
// Used by all variations.

// ──────────────────────────────────────────────────────────────────
// Flags — pure CSS/SVG, no emoji. We approximate national flags
// by drawing geometric stripes/cross/blocks per ISO code.
// Goal: recognizable miniatures, not pixel-perfect.
// ──────────────────────────────────────────────────────────────────

const FLAGS = {
  // 3-stripe horizontal: [topColor, midColor, botColor]
  hStripes: (a, b, c) => (
    <div style={{ display: 'flex', flexDirection: 'column', width: '100%', height: '100%' }}>
      <div style={{ flex: 1, background: a }} />
      <div style={{ flex: 1, background: b }} />
      <div style={{ flex: 1, background: c }} />
    </div>
  ),
  vStripes: (a, b, c) => (
    <div style={{ display: 'flex', width: '100%', height: '100%' }}>
      <div style={{ flex: 1, background: a }} />
      <div style={{ flex: 1, background: b }} />
      <div style={{ flex: 1, background: c }} />
    </div>
  ),
  twoH: (a, b) => (
    <div style={{ display: 'flex', flexDirection: 'column', width: '100%', height: '100%' }}>
      <div style={{ flex: 1, background: a }} />
      <div style={{ flex: 1, background: b }} />
    </div>
  ),
};

const FLAG_DEFS = {
  UA: () => FLAGS.twoH('#0057B7', '#FFD700'),
  PL: () => FLAGS.twoH('#fff', '#dc143c'),
  DE: () => FLAGS.hStripes('#000', '#dd0000', '#ffce00'),
  FR: () => FLAGS.vStripes('#0055A4', '#fff', '#EF4135'),
  IT: () => FLAGS.vStripes('#009246', '#fff', '#CE2B37'),
  ES: () => FLAGS.hStripes('#AA151B', '#F1BF00', '#AA151B'),
  GB: () => (
    <div style={{ position: 'relative', width: '100%', height: '100%', background: '#012169' }}>
      <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(45deg, transparent 45%, #fff 45%, #fff 55%, transparent 55%), linear-gradient(-45deg, transparent 45%, #fff 45%, #fff 55%, transparent 55%)' }} />
      <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(0deg, transparent 42%, #fff 42%, #fff 58%, transparent 58%), linear-gradient(90deg, transparent 42%, #fff 42%, #fff 58%, transparent 58%)' }} />
      <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(0deg, transparent 45%, #C8102E 45%, #C8102E 55%, transparent 55%), linear-gradient(90deg, transparent 45%, #C8102E 45%, #C8102E 55%, transparent 55%)' }} />
    </div>
  ),
  US: () => (
    <div style={{ position: 'relative', width: '100%', height: '100%', background: 'repeating-linear-gradient(0deg, #B22234 0, #B22234 2px, #fff 2px, #fff 4px)' }}>
      <div style={{ position: 'absolute', top: 0, left: 0, width: '40%', height: '54%', background: '#3C3B6E' }} />
    </div>
  ),
  BR: () => (
    <div style={{ position: 'relative', width: '100%', height: '100%', background: '#009C3B', display: 'grid', placeItems: 'center' }}>
      <div style={{ width: '70%', height: '60%', background: '#FFDF00', transform: 'rotate(45deg) scale(0.7)' }} />
      <div style={{ position: 'absolute', width: '34%', height: '60%', background: '#002776', borderRadius: '50%' }} />
    </div>
  ),
  AR: () => FLAGS.hStripes('#74ACDF', '#fff', '#74ACDF'),
  MX: () => FLAGS.vStripes('#006847', '#fff', '#CE1126'),
  JP: () => (
    <div style={{ width: '100%', height: '100%', background: '#fff', display: 'grid', placeItems: 'center' }}>
      <div style={{ width: '55%', height: '70%', background: '#bc002d', borderRadius: '50%' }} />
    </div>
  ),
  KR: () => (
    <div style={{ width: '100%', height: '100%', background: '#fff', display: 'grid', placeItems: 'center' }}>
      <div style={{ width: '50%', height: '70%', background: 'linear-gradient(180deg, #cd2e3a 50%, #0047a0 50%)', borderRadius: '50%' }} />
    </div>
  ),
  CN: () => <div style={{ width: '100%', height: '100%', background: '#de2910' }} />,
  IN: () => FLAGS.hStripes('#FF9933', '#fff', '#138808'),
  RU: () => FLAGS.hStripes('#fff', '#0039A6', '#D52B1E'),
  BY: () => FLAGS.hStripes('#CE1720', '#CE1720', '#4AA657'),
  TR: () => <div style={{ width: '100%', height: '100%', background: '#E30A17' }} />,
  CA: () => (
    <div style={{ display: 'flex', width: '100%', height: '100%' }}>
      <div style={{ flex: 1, background: '#FF0000' }} />
      <div style={{ flex: 2, background: '#fff', display: 'grid', placeItems: 'center' }}>
        <div style={{ width: '50%', height: '60%', background: '#FF0000', clipPath: 'polygon(50% 0, 60% 30%, 100% 35%, 70% 55%, 80% 100%, 50% 80%, 20% 100%, 30% 55%, 0 35%, 40% 30%)' }} />
      </div>
      <div style={{ flex: 1, background: '#FF0000' }} />
    </div>
  ),
  AU: () => <div style={{ width: '100%', height: '100%', background: '#012169' }} />,
  NZ: () => <div style={{ width: '100%', height: '100%', background: '#012169' }} />,
  SE: () => <div style={{ width: '100%', height: '100%', background: '#006AA7', position: 'relative' }}>
    <div style={{ position: 'absolute', top: 0, bottom: 0, left: '32%', width: '14%', background: '#FECC00' }} />
    <div style={{ position: 'absolute', left: 0, right: 0, top: '38%', height: '24%', background: '#FECC00' }} />
  </div>,
  NO: () => <div style={{ width: '100%', height: '100%', background: '#EF2B2D', position: 'relative' }}>
    <div style={{ position: 'absolute', top: 0, bottom: 0, left: '32%', width: '14%', background: '#fff' }} />
    <div style={{ position: 'absolute', left: 0, right: 0, top: '38%', height: '24%', background: '#fff' }} />
    <div style={{ position: 'absolute', top: 0, bottom: 0, left: '36%', width: '6%', background: '#002868' }} />
    <div style={{ position: 'absolute', left: 0, right: 0, top: '42%', height: '16%', background: '#002868' }} />
  </div>,
  FI: () => <div style={{ width: '100%', height: '100%', background: '#fff', position: 'relative' }}>
    <div style={{ position: 'absolute', top: 0, bottom: 0, left: '32%', width: '14%', background: '#003580' }} />
    <div style={{ position: 'absolute', left: 0, right: 0, top: '38%', height: '24%', background: '#003580' }} />
  </div>,
  DK: () => <div style={{ width: '100%', height: '100%', background: '#C8102E', position: 'relative' }}>
    <div style={{ position: 'absolute', top: 0, bottom: 0, left: '32%', width: '14%', background: '#fff' }} />
    <div style={{ position: 'absolute', left: 0, right: 0, top: '38%', height: '24%', background: '#fff' }} />
  </div>,
  NL: () => FLAGS.hStripes('#AE1C28', '#fff', '#21468B'),
  BE: () => FLAGS.vStripes('#000', '#FAE042', '#ED2939'),
  CZ: () => FLAGS.twoH('#fff', '#D7141A'),
  SK: () => FLAGS.hStripes('#fff', '#0B4EA2', '#EE1C25'),
  HU: () => FLAGS.hStripes('#CD2A3E', '#fff', '#436F4D'),
  RO: () => FLAGS.vStripes('#002B7F', '#FCD116', '#CE1126'),
  GR: () => <div style={{ width: '100%', height: '100%', background: 'repeating-linear-gradient(0deg, #0D5EAF 0, #0D5EAF 11%, #fff 11%, #fff 22%)' }} />,
  PT: () => (
    <div style={{ display: 'flex', width: '100%', height: '100%' }}>
      <div style={{ flex: 2, background: '#046A38' }} />
      <div style={{ flex: 3, background: '#DA291C' }} />
    </div>
  ),
  IE: () => FLAGS.vStripes('#169B62', '#fff', '#FF883E'),
  AT: () => FLAGS.hStripes('#ED2939', '#fff', '#ED2939'),
  CH: () => <div style={{ width: '100%', height: '100%', background: '#D52B1E', position: 'relative' }}>
    <div style={{ position: 'absolute', top: '36%', bottom: '36%', left: '24%', right: '24%', background: '#fff' }} />
    <div style={{ position: 'absolute', top: '24%', bottom: '24%', left: '40%', right: '40%', background: '#fff' }} />
  </div>,
  CL: () => (
    <div style={{ width: '100%', height: '100%', display: 'flex', flexDirection: 'column' }}>
      <div style={{ flex: 1, display: 'flex' }}>
        <div style={{ width: '34%', background: '#0039A6' }} />
        <div style={{ flex: 1, background: '#fff' }} />
      </div>
      <div style={{ flex: 1, background: '#D52B1E' }} />
    </div>
  ),
  CO: () => (
    <div style={{ width: '100%', height: '100%', display: 'flex', flexDirection: 'column' }}>
      <div style={{ flex: 2, background: '#FCD116' }} />
      <div style={{ flex: 1, background: '#003893' }} />
      <div style={{ flex: 1, background: '#CE1126' }} />
    </div>
  ),
  PE: () => FLAGS.vStripes('#D91023', '#fff', '#D91023'),
  VE: () => FLAGS.hStripes('#FFCC00', '#00247D', '#CF142B'),
  KZ: () => <div style={{ width: '100%', height: '100%', background: '#00ABC2' }} />,
  UZ: () => FLAGS.hStripes('#1EB53A', '#fff', '#0099B5'),
  GE: () => <div style={{ width: '100%', height: '100%', background: '#fff', position: 'relative' }}>
    <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(0deg, transparent 42%, #ff0000 42%, #ff0000 58%, transparent 58%), linear-gradient(90deg, transparent 42%, #ff0000 42%, #ff0000 58%, transparent 58%)' }} />
  </div>,
  AM: () => FLAGS.hStripes('#D90012', '#0033A0', '#F2A800'),
  AZ: () => FLAGS.hStripes('#0092BC', '#EF3340', '#509E2F'),
  IL: () => <div style={{ width: '100%', height: '100%', background: '#fff', position: 'relative' }}>
    <div style={{ position: 'absolute', left: 0, right: 0, top: '14%', height: '14%', background: '#0038B8' }} />
    <div style={{ position: 'absolute', left: 0, right: 0, bottom: '14%', height: '14%', background: '#0038B8' }} />
  </div>,
  SA: () => <div style={{ width: '100%', height: '100%', background: '#006C35' }} />,
  AE: () => (
    <div style={{ display: 'flex', width: '100%', height: '100%' }}>
      <div style={{ width: '24%', background: '#FF0000' }} />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <div style={{ flex: 1, background: '#009639' }} />
        <div style={{ flex: 1, background: '#fff' }} />
        <div style={{ flex: 1, background: '#000' }} />
      </div>
    </div>
  ),
  IR: () => FLAGS.hStripes('#239F40', '#fff', '#DA0000'),
  IQ: () => FLAGS.hStripes('#CE1126', '#fff', '#000'),
  SG: () => FLAGS.twoH('#EF3340', '#fff'),
  MY: () => <div style={{ width: '100%', height: '100%', background: 'repeating-linear-gradient(0deg, #cc0001 0, #cc0001 7.14%, #fff 7.14%, #fff 14.28%)' }} />,
  TW: () => <div style={{ width: '100%', height: '100%', background: '#FE0000', position: 'relative' }}>
    <div style={{ position: 'absolute', top: 0, left: 0, width: '50%', height: '50%', background: '#000095' }} />
  </div>,
  HK: () => <div style={{ width: '100%', height: '100%', background: '#DE2910' }} />,
  LT: () => FLAGS.hStripes('#FDB913', '#006A44', '#C1272D'),
  LV: () => FLAGS.hStripes('#9E3039', '#fff', '#9E3039'),
  EE: () => FLAGS.hStripes('#0072CE', '#000', '#fff'),
  BG: () => FLAGS.hStripes('#fff', '#00966E', '#D62612'),
  HR: () => FLAGS.hStripes('#FF0000', '#fff', '#171796'),
  RS: () => FLAGS.hStripes('#C6363C', '#0C4076', '#fff'),
  SI: () => FLAGS.hStripes('#fff', '#0000FF', '#FF0000'),
  TH: () => <div style={{ width: '100%', height: '100%', background: 'repeating-linear-gradient(0deg, #ED1C24 0, #ED1C24 16.6%, #fff 16.6%, #fff 33%, #241D4F 33%, #241D4F 66%, #fff 66%, #fff 83.4%, #ED1C24 83.4%)' }} />,
  VN: () => <div style={{ width: '100%', height: '100%', background: '#DA251D' }} />,
  ID: () => FLAGS.twoH('#FF0000', '#fff'),
  PH: () => FLAGS.twoH('#0038A8', '#CE1126'),
  PK: () => (
    <div style={{ display: 'flex', width: '100%', height: '100%' }}>
      <div style={{ width: '24%', background: '#fff' }} />
      <div style={{ flex: 1, background: '#01411C' }} />
    </div>
  ),
  EG: () => FLAGS.hStripes('#CE1126', '#fff', '#000'),
  ZA: () => <div style={{ width: '100%', height: '100%', background: '#007A4D' }} />,
  NG: () => FLAGS.vStripes('#008751', '#fff', '#008751'),
  KE: () => FLAGS.hStripes('#000', '#BB0000', '#006600'),
  MA: () => <div style={{ width: '100%', height: '100%', background: '#C1272D' }} />,
};

function Flag({ code, size = 18 }) {
  const r = FLAG_DEFS[code];
  return (
    <div
      title={(window.LB_COUNTRIES && window.LB_COUNTRIES[code]) || code}
      style={{
        width: size * 1.5, height: size,
        flexShrink: 0,
        overflow: 'hidden',
        borderRadius: 2,
        boxShadow: 'inset 0 0 0 1px rgba(0,0,0,0.5), 0 0 0 0.5px rgba(201,163,91,0.4)',
        background: '#1a1310',
      }}
    >
      {r ? r() : (
        <div style={{ width: '100%', height: '100%', display: 'grid', placeItems: 'center', fontSize: 7, color: '#c9a35b', fontFamily: 'monospace' }}>{code}</div>
      )}
    </div>
  );
}

// ──────────────────────────────────────────────────────────────────
// Stat icons — simple SVG glyphs, gold
// ──────────────────────────────────────────────────────────────────
function StatIcon({ kind, size = 11, color = '#c9a35b' }) {
  const s = size;
  switch (kind) {
    case 'power': // hammer
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <rect x="2" y="3" width="9" height="5" rx="0.5" fill={color} />
          <rect x="3" y="2" width="2" height="1" fill={color} />
          <rect x="8" y="2" width="2" height="1" fill={color} />
          <rect x="6" y="8" width="1.5" height="6" fill={color} />
        </svg>
      );
    case 'armor': // shield
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <path d="M8 1 L14 3 V8 C14 11 11.5 13.5 8 15 C4.5 13.5 2 11 2 8 V3 Z" fill={color} />
          <path d="M8 4 V12" stroke="#1a1310" strokeWidth="0.8" />
          <path d="M5 7 H11" stroke="#1a1310" strokeWidth="0.8" />
        </svg>
      );
    case 'hp': // heart
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <path d="M8 14 C2 10 1 6.5 3 4 C5 1.5 7 3 8 4.5 C9 3 11 1.5 13 4 C15 6.5 14 10 8 14 Z" fill={color} />
        </svg>
      );
    case 'luck': // 4-leaf clover
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <circle cx="5" cy="5" r="3" fill={color} />
          <circle cx="11" cy="5" r="3" fill={color} />
          <circle cx="5" cy="11" r="3" fill={color} />
          <circle cx="11" cy="11" r="3" fill={color} />
          <circle cx="8" cy="8" r="1.4" fill="#1a1310" />
        </svg>
      );
    case 'age': // hourglass
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <path d="M3 2 H13 V4 L9 8 L13 12 V14 H3 V12 L7 8 L3 4 Z" fill={color} />
          <path d="M4.5 3 H11.5 V3.5 L8 6.5 L4.5 3.5 Z" fill="#1a1310" />
        </svg>
      );
    case 'pushup': // small dumbbell
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <rect x="1" y="6" width="2" height="4" fill={color} />
          <rect x="3" y="5" width="1" height="6" fill={color} />
          <rect x="4" y="7" width="8" height="2" fill={color} />
          <rect x="12" y="5" width="1" height="6" fill={color} />
          <rect x="13" y="6" width="2" height="4" fill={color} />
        </svg>
      );
    case 'lvl': // star
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <path d="M8 1 L10 6 L15 6.5 L11 10 L12 15 L8 12.5 L4 15 L5 10 L1 6.5 L6 6 Z" fill={color} />
        </svg>
      );
    case 'res': // refresh / circle arrow
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <path d="M3 8 A5 5 0 0 1 12 5" stroke={color} strokeWidth="1.6" fill="none" />
          <path d="M13 8 A5 5 0 0 1 4 11" stroke={color} strokeWidth="1.6" fill="none" />
          <path d="M10 3 L13 5 L11 7" stroke={color} strokeWidth="1.6" fill="none" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M6 13 L3 11 L5 9" stroke={color} strokeWidth="1.6" fill="none" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      );
    case 'search':
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <circle cx="7" cy="7" r="4.5" stroke={color} strokeWidth="1.5" fill="none" />
          <path d="M11 11 L14 14" stroke={color} strokeWidth="1.5" strokeLinecap="round" />
        </svg>
      );
    case 'caretdown':
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <path d="M3 6 L8 11 L13 6" stroke={color} strokeWidth="1.6" fill="none" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      );
    case 'medal':
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <path d="M5 1 L4 5 M11 1 L12 5" stroke={color} strokeWidth="1.4" />
          <circle cx="8" cy="10" r="5" fill={color} stroke="#1a1310" strokeWidth="0.5" />
          <circle cx="8" cy="10" r="2.6" fill="#1a1310" />
        </svg>
      );
    case 'rune':
      return (
        <svg width={s} height={s} viewBox="0 0 16 16" fill="none">
          <path d="M4 2 L8 8 L4 14 M12 2 L8 8 L12 14" stroke={color} strokeWidth="1.4" fill="none" />
        </svg>
      );
    default: return null;
  }
}

// Number formatter w/ thousands sep
function fmt(n) {
  if (n == null) return '—';
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(2).replace(/\.?0+$/, '') + 'M';
  if (n >= 10_000) return (n / 1_000).toFixed(1).replace(/\.?0+$/, '') + 'k';
  return n.toLocaleString('en-US');
}
function fmtAge(days) {
  if (days >= 365) {
    const y = Math.floor(days / 365);
    const d = days % 365;
    return `${y}y ${d}d`;
  }
  return `${days}d`;
}

// Rank cell — gold for top 3
function RankCell({ rank, isMe }) {
  const top3 = rank <= 3;
  const colors = { 1: '#f0c869', 2: '#cfd1d4', 3: '#c8814a' };
  if (top3) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 3 }}>
        <StatIcon kind="medal" size={14} color={colors[rank]} />
        <span style={{ color: colors[rank], fontWeight: 700, fontFamily: 'Cinzel, serif', fontSize: 12 }}>{rank}</span>
      </div>
    );
  }
  return (
    <span style={{ color: isMe ? '#f0c869' : '#8b6f3d', fontFamily: 'Inter, sans-serif', fontWeight: 600, fontSize: 11, fontVariantNumeric: 'tabular-nums' }}>
      {rank}
    </span>
  );
}

// Avatar — letter monogram in a tarnished gold ring
function Avatar({ name, isMe, size = 22 }) {
  const initial = (name || '?')[0].toUpperCase();
  // Hash to a hue
  let h = 0;
  for (let i = 0; i < name.length; i++) h = (h * 31 + name.charCodeAt(i)) % 360;
  const bg = `oklch(0.32 0.06 ${h})`;
  return (
    <div style={{
      width: size, height: size, borderRadius: '50%',
      background: bg,
      display: 'grid', placeItems: 'center',
      fontFamily: 'Cinzel, serif',
      color: isMe ? '#f0c869' : '#e8d9b3',
      fontSize: size * 0.42,
      fontWeight: 700,
      boxShadow: isMe
        ? 'inset 0 0 0 1.5px #f0c869, 0 0 6px rgba(240,200,105,0.5)'
        : 'inset 0 0 0 1px #5a4525',
      flexShrink: 0,
    }}>
      {initial}
    </div>
  );
}

Object.assign(window, { Flag, StatIcon, fmt, fmtAge, RankCell, Avatar });
