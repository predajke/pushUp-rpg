// LeaderboardBoard — the shared scrollable table + sticky-you row.
// Variations differ in: header chrome (tabs, period, search, sort).
// All share this list rendering.

const ALL_COLS = [
  { key: 'rank',        label: '#',     w: 28,  align: 'center' },
  { key: 'flag',        label: '',      w: 24,  align: 'center' },
  { key: 'name',        label: 'Name',  w: 'flex', align: 'left' },
  { key: 'res',         label: 'RES',   w: 30,  align: 'right', icon: 'res' },
  { key: 'lvl',         label: 'LVL',   w: 28,  align: 'right', icon: 'lvl' },
  { key: 'totalPushUps',label: 'PUSH',  w: 50,  align: 'right', icon: 'pushup' },
  { key: 'power',       label: 'PWR',   w: 42,  align: 'right', icon: 'power' },
  { key: 'armor',       label: 'ARM',   w: 42,  align: 'right', icon: 'armor' },
  { key: 'hp',          label: 'HP',    w: 42,  align: 'right', icon: 'hp' },
  { key: 'luck',        label: 'LCK',   w: 36,  align: 'right', icon: 'luck' },
  { key: 'age',         label: 'AGE',   w: 44,  align: 'right', icon: 'age' },
];

function colValue(p, key) {
  if (key === 'age') return fmtAge(p.ageDays);
  if (key === 'totalPushUps') return fmt(p.totalPushUps);
  if (['power','armor','hp','luck'].includes(key)) return fmt(p[key]);
  return p[key];
}

function ColumnHeader({ cols, sortKey, sortDir, onSort, density }) {
  const padY = density === 'compact' ? 4 : 6;
  return (
    <div style={{
      display: 'flex',
      alignItems: 'stretch',
      padding: `${padY}px 8px`,
      borderTop: '1px solid #2a1f17',
      borderBottom: '1px solid #2a1f17',
      background: 'linear-gradient(180deg, #1a1310 0%, #14100c 100%)',
      position: 'sticky', top: 0, zIndex: 2,
      fontFamily: 'Cinzel, serif',
      fontSize: 9,
      letterSpacing: '0.12em',
      color: '#8b6f3d',
      textTransform: 'uppercase',
    }}>
      {cols.map(c => {
        const sortable = c.key !== 'flag' && c.key !== 'rank';
        const active = sortKey === c.key;
        const style = {
          width: c.w === 'flex' ? undefined : c.w,
          flex: c.w === 'flex' ? 1 : undefined,
          textAlign: c.align,
          padding: '0 4px',
          cursor: sortable && onSort ? 'pointer' : 'default',
          display: 'flex',
          alignItems: 'center',
          justifyContent: c.align === 'right' ? 'flex-end' : c.align === 'center' ? 'center' : 'flex-start',
          gap: 3,
          color: active ? '#f0c869' : undefined,
          userSelect: 'none',
        };
        return (
          <div key={c.key} style={style} onClick={() => sortable && onSort && onSort(c.key)}>
            {c.icon && <StatIcon kind={c.icon} size={9} color={active ? '#f0c869' : '#6b5430'} />}
            <span>{c.label}</span>
            {active && (
              <span style={{ fontSize: 8, marginLeft: 1 }}>{sortDir === 'asc' ? '▲' : '▼'}</span>
            )}
          </div>
        );
      })}
    </div>
  );
}

function PlayerRow({ p, cols, density, alt, isMeRow }) {
  const padY = density === 'compact' ? 4 : 8;
  const bg = isMeRow
    ? 'linear-gradient(90deg, rgba(240,200,105,0.10) 0%, rgba(240,200,105,0.04) 100%)'
    : alt ? 'rgba(255,255,255,0.012)' : 'transparent';
  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      padding: `${padY}px 8px`,
      borderBottom: '1px solid rgba(42,31,23,0.45)',
      background: bg,
      fontFamily: 'Inter, sans-serif',
      fontSize: 11,
      color: isMeRow ? '#f5e8c8' : '#bba27a',
      fontVariantNumeric: 'tabular-nums',
      position: 'relative',
    }}>
      {isMeRow && (
        <div style={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: 2, background: '#f0c869', boxShadow: '0 0 8px #f0c869' }} />
      )}
      {cols.map(c => {
        const cellStyle = {
          width: c.w === 'flex' ? undefined : c.w,
          flex: c.w === 'flex' ? 1 : undefined,
          textAlign: c.align,
          padding: '0 4px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: c.align === 'right' ? 'flex-end' : c.align === 'center' ? 'center' : 'flex-start',
          minWidth: 0,
        };
        if (c.key === 'rank') {
          return <div key={c.key} style={cellStyle}><RankCell rank={p.rank} isMe={isMeRow} /></div>;
        }
        if (c.key === 'flag') {
          return <div key={c.key} style={cellStyle}><Flag code={p.country} size={11} /></div>;
        }
        if (c.key === 'name') {
          return (
            <div key={c.key} style={cellStyle}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, minWidth: 0 }}>
                <Avatar name={p.name} isMe={isMeRow} size={density === 'compact' ? 16 : 20} />
                <span style={{
                  fontFamily: 'Cinzel, serif',
                  fontWeight: isMeRow ? 700 : 500,
                  fontSize: 11,
                  color: isMeRow ? '#f0c869' : '#e8d9b3',
                  overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                }}>{p.name}</span>
                {p.isFriend && !isMeRow && (
                  <span style={{ fontSize: 7, color: '#8b6f3d', fontFamily: 'monospace', marginLeft: 2 }}>•</span>
                )}
              </div>
            </div>
          );
        }
        return (
          <div key={c.key} style={cellStyle}>
            {colValue(p, c.key)}
          </div>
        );
      })}
    </div>
  );
}

// Filters scope: 'global' | 'country' | 'friends'
// Sort: { key, dir }
function applyFilters(players, { scope, me, q, period }) {
  let list = players;
  if (scope === 'country') list = list.filter(p => p.country === me.country || p.isMe);
  else if (scope === 'friends') list = list.filter(p => p.isFriend || p.isMe);
  if (q) list = list.filter(p => p.name.toLowerCase().includes(q.toLowerCase()));
  return list;
}

function applySort(list, sort) {
  if (!sort || !sort.key || sort.key === 'rank') return list;
  const k = sort.key;
  const arr = list.slice().sort((a, b) => {
    let av = a[k], bv = b[k];
    if (k === 'age') { av = a.ageDays; bv = b.ageDays; }
    if (typeof av === 'string') return av.localeCompare(bv) * (sort.dir === 'asc' ? 1 : -1);
    return (av - bv) * (sort.dir === 'asc' ? 1 : -1);
  });
  return arr;
}

// Pre-compute friend ring around me for sticky row context
function neighborsAroundMe(list, me, span = 1) {
  const idx = list.findIndex(p => p.isMe);
  if (idx === -1) return [];
  const out = [];
  for (let i = idx - span; i <= idx + span; i++) {
    if (i >= 0 && i < list.length) out.push(list[i]);
  }
  return out;
}

// ── The whole board (header + scroll body + sticky-you)
function LeaderboardBoard({
  players, me,
  visibleCols,
  density,
  sort,
  onSort,
  scope = 'global',
  q = '',
  showStickyMe = true,
  onSelectPlayer,
}) {
  const cols = ALL_COLS.filter(c => c.key === 'rank' || c.key === 'flag' || c.key === 'name' || visibleCols.includes(c.key));
  const filtered = React.useMemo(() => applyFilters(players, { scope, me, q }), [players, scope, q, me]);
  const sorted = React.useMemo(() => applySort(filtered, sort), [filtered, sort]);

  // If user is sorting, ranks shown still come from p.rank (global rank). Good.
  return (
    <div style={{
      display: 'flex', flexDirection: 'column',
      flex: 1, minHeight: 0,
      background: '#0e0a07',
    }}>
      <ColumnHeader cols={cols} sortKey={sort?.key} sortDir={sort?.dir} onSort={onSort} density={density} />
      <div className="lb-scroll" style={{
        flex: 1, overflowY: 'auto', overflowX: 'hidden',
        scrollbarWidth: 'thin',
        scrollbarColor: '#2a1f17 #0e0a07',
      }}>
        {sorted.map((p, i) => (
          <div key={p.rank + '-' + p.name} onClick={() => onSelectPlayer && onSelectPlayer(p)}>
            <PlayerRow p={p} cols={cols} density={density} alt={i % 2 === 1} isMeRow={p.isMe} />
          </div>
        ))}
        {sorted.length === 0 && (
          <div style={{ padding: 32, textAlign: 'center', color: '#5a4525', fontFamily: 'Cinzel, serif', fontSize: 12 }}>
            No champions match.
          </div>
        )}
        <div style={{ height: 80 }} />
      </div>
      {showStickyMe && me && (
        <StickyMeRow me={me} cols={cols} density={density} players={sorted} />
      )}
    </div>
  );
}

// Sticky "You" row. Slightly elevated, gold accent. Shows ±1 neighbor on the right (mini context)
function StickyMeRow({ me, cols, density, players }) {
  const above = players.find((p, i) => players[i + 1] && players[i + 1].isMe);
  const below = (() => {
    const idx = players.findIndex(p => p.isMe);
    return idx >= 0 ? players[idx + 1] : null;
  })();
  return (
    <div style={{
      borderTop: '1px solid #c9a35b',
      background: 'linear-gradient(180deg, #1a1310 0%, #0e0a07 100%)',
      boxShadow: '0 -8px 24px rgba(0,0,0,0.6), inset 0 1px 0 rgba(240,200,105,0.18)',
      position: 'relative',
      flexShrink: 0,
    }}>
      {/* Tiny context bar */}
      <div style={{
        display: 'flex', justifyContent: 'space-between',
        padding: '4px 12px 0',
        fontFamily: 'Cinzel, serif',
        fontSize: 8, letterSpacing: '0.18em',
        color: '#6b5430',
        textTransform: 'uppercase',
      }}>
        <span>Your Standing</span>
        <span>
          {above && <>↑ #{above.rank} {above.name.slice(0, 10)} &nbsp;·&nbsp; </>}
          {below && <>↓ #{below.rank} {below.name.slice(0, 10)}</>}
        </span>
      </div>
      <PlayerRow p={me} cols={cols} density={density} alt={false} isMeRow={true} />
    </div>
  );
}

Object.assign(window, { LeaderboardBoard, ALL_COLS });
