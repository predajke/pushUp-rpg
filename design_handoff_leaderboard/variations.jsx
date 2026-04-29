// 5 variations of the Leaderboard screen.
// Each is a self-contained <IOSDevice> inside a <DCArtboard>.
// Variations vary the FILTER/SORT/TAB chrome above the board.

const BOARD_W = 390;
const BOARD_H = 760;

// Ornate header used by all variants
function RpgHeader({ subtitle }) {
  return (
    <div style={{
      padding: '6px 16px 8px',
      background: 'linear-gradient(180deg, #1a1310 0%, #0e0a07 100%)',
      borderBottom: '1px solid #2a1f17',
      position: 'relative',
    }}>
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
      }}>
        <StatIcon kind="rune" size={12} color="#8b6f3d" />
        <h1 style={{
          margin: 0,
          fontFamily: 'Cinzel, serif',
          fontWeight: 700,
          fontSize: 16,
          letterSpacing: '0.34em',
          color: '#c9a35b',
          textTransform: 'uppercase',
          textShadow: '0 1px 0 rgba(0,0,0,0.6), 0 0 12px rgba(201,163,91,0.18)',
        }}>Leaderboard</h1>
        <StatIcon kind="rune" size={12} color="#8b6f3d" />
      </div>
      {subtitle && (
        <div style={{
          textAlign: 'center',
          marginTop: 2,
          fontFamily: 'Cinzel, serif',
          fontSize: 8.5,
          letterSpacing: '0.32em',
          color: '#6b5430',
          textTransform: 'uppercase',
        }}>
          {subtitle}
        </div>
      )}
      {/* Decorative gold rule */}
      <div style={{
        position: 'absolute', left: 0, right: 0, bottom: -1, height: 1,
        background: 'linear-gradient(90deg, transparent 0%, #8b6f3d 20%, #c9a35b 50%, #8b6f3d 80%, transparent 100%)',
      }} />
    </div>
  );
}

// Re-usable scope segmented control
function ScopeTabs({ value, onChange, options }) {
  return (
    <div style={{
      display: 'flex',
      padding: '8px 12px',
      gap: 4,
      background: '#0e0a07',
      borderBottom: '1px solid #1a1310',
    }}>
      {options.map(o => {
        const active = value === o.key;
        return (
          <button key={o.key} onClick={() => onChange(o.key)} style={{
            flex: 1,
            padding: '7px 8px',
            background: active ? 'linear-gradient(180deg, #2a1f17 0%, #1a1310 100%)' : 'transparent',
            border: '1px solid ' + (active ? '#c9a35b' : '#2a1f17'),
            borderRadius: 2,
            fontFamily: 'Cinzel, serif',
            fontSize: 9.5,
            letterSpacing: '0.18em',
            textTransform: 'uppercase',
            color: active ? '#f0c869' : '#6b5430',
            cursor: 'pointer',
            boxShadow: active ? 'inset 0 0 0 1px rgba(0,0,0,0.4), 0 0 0 1px rgba(201,163,91,0.08)' : 'none',
            transition: 'all 0.15s',
          }}>
            {o.label}
          </button>
        );
      })}
    </div>
  );
}

// Period pills
function PeriodPills({ value, onChange }) {
  const opts = [
    { key: 'day', label: 'Day' },
    { key: 'week', label: 'Week' },
    { key: 'month', label: 'Month' },
    { key: 'all', label: 'All Time' },
  ];
  return (
    <div style={{
      display: 'flex',
      padding: '8px 12px',
      gap: 6,
      background: '#0e0a07',
    }}>
      {opts.map(o => {
        const active = value === o.key;
        return (
          <button key={o.key} onClick={() => onChange(o.key)} style={{
            flex: 1,
            padding: '5px 4px',
            background: active ? 'rgba(240,200,105,0.10)' : 'transparent',
            border: '1px solid ' + (active ? '#c9a35b' : '#3a2c20'),
            borderRadius: 14,
            fontFamily: 'Inter, sans-serif',
            fontSize: 10,
            fontWeight: 600,
            letterSpacing: '0.05em',
            color: active ? '#f0c869' : '#8b6f3d',
            cursor: 'pointer',
          }}>
            {o.label}
          </button>
        );
      })}
    </div>
  );
}

function SearchBox({ value, onChange, placeholder = 'Search champion…' }) {
  return (
    <div style={{
      margin: '8px 12px',
      display: 'flex', alignItems: 'center',
      gap: 8,
      padding: '8px 12px',
      background: '#1a1310',
      border: '1px solid #2a1f17',
      borderRadius: 2,
    }}>
      <StatIcon kind="search" size={12} color="#8b6f3d" />
      <input
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder={placeholder}
        style={{
          flex: 1,
          background: 'transparent',
          border: 'none',
          outline: 'none',
          fontFamily: 'Inter, sans-serif',
          color: '#e8d9b3',
          fontSize: 12,
        }}
      />
      {value && (
        <button onClick={() => onChange('')} style={{
          background: 'transparent', border: 'none',
          color: '#8b6f3d', cursor: 'pointer', fontFamily: 'Cinzel, serif', fontSize: 10,
        }}>CLR</button>
      )}
    </div>
  );
}

const SCOPE_OPTS = [
  { key: 'global', label: 'Global' },
  { key: 'country', label: 'Country' },
  { key: 'friends', label: 'Friends' },
];

// ──────────────────────────────────────────────────────────────────
// VARIATION 1 — "Tome of Champions"
// Pure RPG codex aesthetic. Sortable headers. No tabs/search.
// Focus: the table itself is the hero. Minimal chrome.
// ──────────────────────────────────────────────────────────────────
function VariationTome({ density, visibleCols }) {
  const [sort, setSort] = React.useState({ key: 'totalPushUps', dir: 'desc' });
  const onSort = (k) => setSort(s => s.key === k ? { key: k, dir: s.dir === 'asc' ? 'desc' : 'asc' } : { key: k, dir: 'desc' });
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: '#0e0a07' }}>
      <RpgHeader subtitle="Tome of Champions · Season III" />
      <LeaderboardBoard
        players={window.LB_PLAYERS}
        me={window.LB_ME}
        visibleCols={visibleCols}
        density={density}
        sort={sort}
        onSort={onSort}
      />
    </div>
  );
}

// ──────────────────────────────────────────────────────────────────
// VARIATION 2 — "Three Realms" (scope tabs)
// Global / Country / Friends segmented control under header.
// ──────────────────────────────────────────────────────────────────
function VariationRealms({ density, visibleCols }) {
  const [scope, setScope] = React.useState('global');
  const [sort, setSort] = React.useState({ key: 'totalPushUps', dir: 'desc' });
  const onSort = (k) => setSort(s => s.key === k ? { key: k, dir: s.dir === 'asc' ? 'desc' : 'asc' } : { key: k, dir: 'desc' });
  const scopeMeta = {
    global: { count: 500, label: 'All Realms' },
    country: { count: window.LB_PLAYERS.filter(p => p.country === window.LB_ME.country).length, label: 'Ukraine' },
    friends: { count: window.LB_PLAYERS.filter(p => p.isFriend || p.isMe).length, label: 'Allies' },
  }[scope];
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: '#0e0a07' }}>
      <RpgHeader subtitle={`${scopeMeta.label} · ${scopeMeta.count} contenders`} />
      <ScopeTabs value={scope} onChange={setScope} options={SCOPE_OPTS} />
      <LeaderboardBoard
        players={window.LB_PLAYERS}
        me={window.LB_ME}
        visibleCols={visibleCols}
        density={density}
        sort={sort}
        onSort={onSort}
        scope={scope}
      />
    </div>
  );
}

// ──────────────────────────────────────────────────────────────────
// VARIATION 3 — "Ages of War" (period tabs)
// Day / Week / Month / All-time as pill row, with ornamental flourish.
// ──────────────────────────────────────────────────────────────────
function VariationAges({ density, visibleCols }) {
  const [period, setPeriod] = React.useState('all');
  const [sort, setSort] = React.useState({ key: 'totalPushUps', dir: 'desc' });
  const onSort = (k) => setSort(s => s.key === k ? { key: k, dir: s.dir === 'asc' ? 'desc' : 'asc' } : { key: k, dir: 'desc' });
  const periodLabel = { day: 'Today', week: 'This Week', month: 'This Moon', all: 'All Time' }[period];
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: '#0e0a07' }}>
      <RpgHeader subtitle={`Ages of War · ${periodLabel}`} />
      <PeriodPills value={period} onChange={setPeriod} />
      <LeaderboardBoard
        players={window.LB_PLAYERS}
        me={window.LB_ME}
        visibleCols={visibleCols}
        density={density}
        sort={sort}
        onSort={onSort}
      />
    </div>
  );
}

// ──────────────────────────────────────────────────────────────────
// VARIATION 4 — "The Oracle" (search-first)
// Search + scope + sort dropdown. Power-user vibes.
// ──────────────────────────────────────────────────────────────────
function VariationOracle({ density, visibleCols }) {
  const [scope, setScope] = React.useState('global');
  const [q, setQ] = React.useState('');
  const [sort, setSort] = React.useState({ key: 'totalPushUps', dir: 'desc' });
  const [sortOpen, setSortOpen] = React.useState(false);
  const sortOpts = [
    { key: 'totalPushUps', label: 'Push-ups', icon: 'pushup' },
    { key: 'lvl', label: 'Level', icon: 'lvl' },
    { key: 'res', label: 'Resets', icon: 'res' },
    { key: 'power', label: 'Power', icon: 'power' },
    { key: 'armor', label: 'Armor', icon: 'armor' },
    { key: 'hp', label: 'HP', icon: 'hp' },
    { key: 'luck', label: 'Luck', icon: 'luck' },
    { key: 'age', label: 'Age', icon: 'age' },
  ];
  const onSort = (k) => setSort(s => s.key === k ? { key: k, dir: s.dir === 'asc' ? 'desc' : 'asc' } : { key: k, dir: 'desc' });
  const currentSort = sortOpts.find(o => o.key === sort.key);
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: '#0e0a07' }}>
      <RpgHeader subtitle="The Oracle · Seek your foe" />
      <SearchBox value={q} onChange={setQ} placeholder="Speak a name…" />
      <div style={{ display: 'flex', gap: 8, padding: '0 12px 8px', alignItems: 'center' }}>
        <div style={{ flex: 1 }}>
          <ScopeTabs value={scope} onChange={setScope} options={SCOPE_OPTS} />
        </div>
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0 14px 6px', position: 'relative' }}>
        <span style={{ fontFamily: 'Cinzel, serif', fontSize: 9, letterSpacing: '0.2em', color: '#6b5430', textTransform: 'uppercase' }}>
          Sort by
        </span>
        <button onClick={() => setSortOpen(o => !o)} style={{
          display: 'flex', alignItems: 'center', gap: 6,
          background: '#1a1310',
          border: '1px solid #2a1f17',
          padding: '5px 10px',
          color: '#f0c869',
          fontFamily: 'Cinzel, serif', fontSize: 10,
          letterSpacing: '0.12em', textTransform: 'uppercase',
          cursor: 'pointer', borderRadius: 2,
        }}>
          <StatIcon kind={currentSort.icon} size={10} color="#c9a35b" />
          {currentSort.label} {sort.dir === 'asc' ? '▲' : '▼'}
          <StatIcon kind="caretdown" size={9} color="#8b6f3d" />
        </button>
        {sortOpen && (
          <div style={{
            position: 'absolute', right: 14, top: '100%',
            background: '#1a1310', border: '1px solid #c9a35b',
            zIndex: 20, minWidth: 140,
            boxShadow: '0 8px 24px rgba(0,0,0,0.7)',
          }}>
            {sortOpts.map(o => (
              <button key={o.key} onClick={() => { onSort(o.key); setSortOpen(false); }} style={{
                display: 'flex', alignItems: 'center', gap: 8, width: '100%',
                padding: '7px 10px',
                background: o.key === sort.key ? 'rgba(240,200,105,0.08)' : 'transparent',
                border: 'none', borderBottom: '1px solid #2a1f17',
                color: o.key === sort.key ? '#f0c869' : '#bba27a',
                fontFamily: 'Cinzel, serif', fontSize: 10,
                letterSpacing: '0.12em', textTransform: 'uppercase',
                cursor: 'pointer', textAlign: 'left',
              }}>
                <StatIcon kind={o.icon} size={10} color={o.key === sort.key ? '#f0c869' : '#8b6f3d'} />
                {o.label}
              </button>
            ))}
          </div>
        )}
      </div>
      <LeaderboardBoard
        players={window.LB_PLAYERS}
        me={window.LB_ME}
        visibleCols={visibleCols}
        density={density}
        sort={sort}
        onSort={onSort}
        scope={scope}
        q={q}
      />
    </div>
  );
}

// ──────────────────────────────────────────────────────────────────
// VARIATION 5 — "War Council" (combined power-user)
// All controls: period + scope + search + sortable cols.
// Dense, game-grade. Lots of context.
// ──────────────────────────────────────────────────────────────────
function VariationWarCouncil({ density, visibleCols }) {
  const [scope, setScope] = React.useState('global');
  const [period, setPeriod] = React.useState('all');
  const [q, setQ] = React.useState('');
  const [sort, setSort] = React.useState({ key: 'totalPushUps', dir: 'desc' });
  const onSort = (k) => setSort(s => s.key === k ? { key: k, dir: s.dir === 'asc' ? 'desc' : 'asc' } : { key: k, dir: 'desc' });
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: '#0e0a07' }}>
      <RpgHeader subtitle="War Council · Full View" />
      <ScopeTabs value={scope} onChange={setScope} options={SCOPE_OPTS} />
      <PeriodPills value={period} onChange={setPeriod} />
      <SearchBox value={q} onChange={setQ} placeholder="Filter by name…" />
      <LeaderboardBoard
        players={window.LB_PLAYERS}
        me={window.LB_ME}
        visibleCols={visibleCols}
        density={density}
        sort={sort}
        onSort={onSort}
        scope={scope}
        q={q}
      />
    </div>
  );
}

Object.assign(window, {
  VariationTome, VariationRealms, VariationAges, VariationOracle, VariationWarCouncil,
  BOARD_W, BOARD_H,
});
