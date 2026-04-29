# Handoff: Push UP RPG — Leaderboard Screen

## Overview
Online leaderboard screen for the **Push UP RPG** mobile app. Shows the top 500 players globally, with the player's own ranking pinned at the bottom. Combines period filtering (Day/Week/Month/All-time), scope filtering (Global/Country/Friends), search, and per-column sort. Visual direction: **dark fantasy / Diablo-vibe** (leather, tarnished gold, runic typography).

The selected design direction is **"War Council"** — the power-user variant that exposes every control. See `Leaderboard.html` (Variation E) for the source prototype, or `WarCouncil-standalone.html` for an isolated single-screen version that's easier to inspect.

## About the Design Files
The files in this bundle are **design references created in HTML** — interactive prototypes showing intended look, layout, and behavior. They are not production code to copy directly.

Your task is to **recreate this design in the target codebase's existing environment** (React Native, SwiftUI, Flutter, native iOS/Android, whatever the Push UP RPG app is built with) using the codebase's established patterns, design tokens, and component library. If no environment exists yet, pick the most appropriate framework for a mobile RPG game and implement there.

The `.jsx` files in this bundle use React + inline Babel just so the prototype runs in a browser — they are not meant to be the final implementation.

## Fidelity
**High-fidelity (hifi).** Final colors, typography, spacing, iconography, and interactions are settled. Match exactly.

The only thing left intentionally placeholder is **flag rendering** — the prototype draws geometric stripe-flags via CSS. In production, use a real flag asset library (e.g. Twemoji flags, country-flag-icons, or platform-native flag emoji).

## Screens / Views

### 1. Leaderboard — War Council
**Purpose:** Browse top 500 players, filter, sort, and find your own rank.

**Layout (top to bottom, mobile portrait, ~390×844 viewport):**

1. **Status bar** (system, leave to platform).
2. **Header** — 32px tall band, gold rune glyph + "LEADERBOARD" wordmark + rune glyph, centered. Subtitle line below: "WAR COUNCIL · FULL VIEW" in small uppercase letterspaced gold. Bottom edge: gold horizontal-fade rule (1px, gradient transparent→gold→transparent).
3. **Scope segmented control** — 3 equal segments: `Global` / `Country` / `Friends`. Active segment has gold border + slightly raised gradient fill.
4. **Period pill row** — 4 pills (rounded 14px): `Day` / `Week` / `Month` / `All Time`. Active pill has gold-tinted bg (10% gold) + gold border.
5. **Search box** — full-width input "Filter by name…" with gold magnifier icon. Dark fill, single 1px border.
6. **Column header row** — sticky. All-caps Cinzel labels, 9px, letterspaced. Tappable to sort. Active sort column is gold + has ▲/▼ indicator. Tiny gold stat icon precedes the label.
7. **Scrolling player list** — alternating rows (every other row gets a 1.2% white tint). Each row 30–36px tall depending on density.
8. **Sticky "You" row** at the bottom — gold top border, dark gradient bg, drop-shadow upward. Has a tiny "YOUR STANDING" label + "↑ #N PrevPlayer · ↓ #N NextPlayer" context line. Below it: the user's row with a 2px gold left rail + soft gold glow + bold gold name.

### Row anatomy (left → right)

| Column | Width | Align | Notes |
|---|---|---|---|
| `#` (rank) | 28px | center | Top-1/2/3 get a medal SVG glyph + colored numeral (gold/silver/bronze). Others: muted gold 11px tabular numeral. |
| Flag | 24px | center | 16.5×11px flag rectangle, 2px radius, inset dark border. |
| Name | flex | left | Avatar (20px circle, monogram on hashed-hue dark bg, gold ring) + Cinzel name 11px. Friends get a tiny gold dot suffix. |
| `RES` | 30px | right | Resets count. |
| `LVL` | 28px | right | Level. |
| `PUSH` | 50px | right | Total push-ups. Formatted: 4,500 → "4.5k", 180,000 → "180k", 1.2M → "1.2M". |
| `PWR` | 42px | right | Power. |
| `ARM` | 42px | right | Armor. |
| `HP` | 42px | right | Health. |
| `LCK` | 36px | right | Luck. |
| `AGE` | 44px | right | Character age. Format: <365 days → "23d", ≥365 → "2y 14d". |

Header labels carry a tiny stat icon (hammer/shield/heart/clover/hourglass/dumbbell/star/refresh) before the text.

## Interactions & Behavior

- **Tap column header** → sort by that column. Tap again to flip asc/desc. Default sort: `totalPushUps` desc.
- **Tap scope tab** (Global/Country/Friends) → filter list.
  - Global: all 500.
  - Country: only players matching the user's country.
  - Friends: only `isFriend === true` (plus self).
- **Tap period pill** → switch dataset for Day/Week/Month/All-time.
  *(In the prototype, period doesn't actually re-filter the data — it's a UI state only. In production, swap the dataset.)*
- **Type in search** → live filter by `name.toLowerCase().includes(query)`.
- **Tap row** → open player profile (out of scope here; wire to your existing profile navigation).
- **Self row** is always pinned at the bottom regardless of scrolling, sorting, or filtering. Even if you sort by Luck or scope to Country, the sticky-you row still shows the user's global rank and stats.
- **Empty state** — when filters return zero rows, show centered "No champions match." in muted Cinzel.
- **Top-3 medals** — rank cells 1/2/3 get medal SVG + tinted numeral (gold #f0c869, silver #cfd1d4, bronze #c8814a).
- **Friends marker** — friend rows get a small gold "•" after the name.

### Animations / transitions
The prototype is intentionally restrained — no row-reorder animation. If you want to add one in production:
- 150ms ease-out for row reordering when sort changes.
- Sticky-you row should NOT animate; it stays fixed.
- Tab/pill state changes: 150ms color/border transition.

### Loading state (not in prototype, recommended for production)
Show 12 skeleton rows with a subtle gold shimmer across them.

## State Management

```ts
type Scope = 'global' | 'country' | 'friends';
type Period = 'day' | 'week' | 'month' | 'all';
type SortKey = 'totalPushUps' | 'lvl' | 'res' | 'power' | 'armor' | 'hp' | 'luck' | 'age';
type SortDir = 'asc' | 'desc';

interface LeaderboardState {
  scope: Scope;       // default: 'global'
  period: Period;     // default: 'all'
  query: string;      // default: ''
  sort: { key: SortKey; dir: SortDir };  // default: { key: 'totalPushUps', dir: 'desc' }
}

interface Player {
  rank: number;       // 1..500, server-assigned global rank
  name: string;
  country: string;    // ISO 3166-1 alpha-2 (e.g. 'UA')
  isFriend: boolean;
  isMe: boolean;
  res: number;        // resets
  lvl: number;        // level
  totalPushUps: number;
  power: number;
  armor: number;
  hp: number;
  luck: number;
  ageDays: number;    // character age in days
}
```

Data fetching (out of scope for design but a sketch):
- `GET /leaderboard?scope=global&period=all` → `{ players: Player[], me: Player }`
- Refetch when scope or period changes.
- Sort and search are client-side over the returned 500.

## Design Tokens

### Colors
| Token | Hex | Usage |
|---|---|---|
| `--bg-deep` | `#0e0a07` | Page background |
| `--bg-card` | `#1a1310` | Header bands, search bg, sort dropdowns |
| `--bg-card-2` | `#14100c` | Header gradient end |
| `--border-leather` | `#2a1f17` | Default borders, dividers |
| `--border-leather-strong` | `#3a2c20` | Pill borders |
| `--gold` | `#c9a35b` | Primary accent (rules, icons) |
| `--gold-bright` | `#f0c869` | Active state, self highlight, top-1 medal |
| `--gold-dim` | `#8b6f3d` | Subdued labels, inactive icons |
| `--gold-darker` | `#6b5430` | Faintest labels |
| `--gold-deepest` | `#5a4525` | Avatar inner ring |
| `--silver` | `#cfd1d4` | Top-2 medal |
| `--bronze` | `#c8814a` | Top-3 medal |
| `--blood` | `#d4242a` | Reserved for HP-low states (future) |
| `--parchment` | `#e8d9b3` | Default body text |
| `--parchment-bright` | `#f5e8c8` | Self body text |
| `--parchment-dim` | `#bba27a` | Stat numerals (non-self) |

Row alt-stripe overlay: `rgba(255,255,255,0.012)`.
Self-row bg gradient: `linear-gradient(90deg, rgba(240,200,105,0.10) 0%, rgba(240,200,105,0.04) 100%)`.

### Typography
- **Cinzel** (Google Font, weights 400/500/600/700) — headings, labels, names, medals, all-caps UI.
- **Inter** (Google Font, weights 400/500/600/700) — numerals, search input, body text.

| Element | Font | Size | Weight | Letter-spacing | Transform |
|---|---|---|---|---|---|
| H1 "LEADERBOARD" | Cinzel | 16px | 700 | 0.34em | uppercase |
| Header subtitle | Cinzel | 8.5px | 400 | 0.32em | uppercase |
| Scope tab | Cinzel | 9.5px | 500 | 0.18em | uppercase |
| Period pill | Inter | 10px | 600 | 0.05em | none |
| Column header | Cinzel | 9px | 500 | 0.12em | uppercase |
| Player name | Cinzel | 11px | 500 (700 if self) | normal | none |
| Stat numeral | Inter | 11px | 400 | normal | none, tabular-nums |
| Search input | Inter | 12px | 400 | normal | none |
| "Your Standing" label | Cinzel | 8px | 400 | 0.18em | uppercase |
| Empty state | Cinzel | 12px | 400 | normal | none |

### Spacing
Padding inside rows: 4px (compact) / 8px (comfy) vertical, 8px outer + 4px per cell horizontal.
Header band: 6/16/8 padding (top/horizontal/bottom).
Pills/segmented gap: 4–6px.

### Border radius
- 2px on most rectangles (segments, search box, sort dropdown).
- 14px on period pills.
- 50% on avatars and the JP/KR-style flag dots.

### Shadows
- Sticky-you separator: `0 -8px 24px rgba(0,0,0,0.6), inset 0 1px 0 rgba(240,200,105,0.18)`.
- Self-row gold rail glow: `box-shadow: 0 0 8px #f0c869` on a 2px-wide left strip.
- Avatar self-glow: `inset 0 0 0 1.5px #f0c869, 0 0 6px rgba(240,200,105,0.5)`.
- Sort dropdown: `0 8px 24px rgba(0,0,0,0.7)`.

### Density
Two presets, surfaced as a setting (we exposed it via the prototype's Tweaks panel; in production this can be a profile/settings toggle):
- **Compact** — row vertical pad 4px, avatar 16px.
- **Comfy** — row vertical pad 8px, avatar 20px.

### Column visibility
Power-user toggle for showing/hiding RES, LVL, Push-ups, Power, Armor, HP, Luck, Age. The Rank, Flag, and Name columns are always visible.

## Assets

- **Stat icons** (8 SVGs, 16×16 viewBox each): hammer (Power), shield (Armor), heart (HP), clover (Luck), hourglass (Age), dumbbell (Push-ups), star (LVL), refresh (RES). Source SVG paths are in `primitives.jsx` under `StatIcon`. They're tuned for tiny sizes (9–11px); replace with your in-game icon set if you have a matching one.
- **Decorative glyphs** in the header: a "rune" (zigzag), a "medal" (bordered circle with stem). Same `StatIcon` source.
- **Flags**: prototype draws ~70 country flags as CSS shapes. **Replace in production** with a real flag asset library — Twemoji flags, country-flag-icons npm package, or platform native flag emoji are all fine.
- **Fonts**: Google Fonts — Cinzel and Inter. Self-host or use Google Fonts CDN as your codebase prefers.

## Files in this bundle

- `README.md` — this file.
- `Leaderboard.html` — full design canvas with all 5 variations side-by-side. Use to compare directions or revisit them.
- `WarCouncil-standalone.html` — **the chosen direction**, isolated. Open this to see the exact target screen at full size with no canvas chrome.
- `data.js` — mock data generator. 500 deterministic players. Useful as a fixture for visual regression tests.
- `primitives.jsx` — Flag, StatIcon, Avatar, RankCell, fmt, fmtAge. Re-implement these in your target framework.
- `leaderboard.jsx` — `LeaderboardBoard`, `ColumnHeader`, `PlayerRow`, `StickyMeRow`, `applyFilters`, `applySort`, `ALL_COLS`. The core logic that renders the table and handles the sticky-you row.
- `variations.jsx` — All 5 variations including `VariationWarCouncil` (the chosen one). The chrome (header, scope tabs, period pills, search) is in here.
- `ios-frame.jsx` — iOS device bezel used for the prototype. Not part of the implementation; for reference only.

## Notes for the implementer

1. **Sticky-you row must always show the user's GLOBAL rank**, not their position within the currently-filtered list. (i.e. if you filter to Friends and the user is rank #364 globally but #4 among friends, sticky still says #364.)
2. **Top-3 medals** apply only to the global rank — they're earned, not contextual to filters.
3. **Self row visual treatment is layered**: it appears once inline (in its natural sorted position in the list) AND once pinned at the bottom. Both show the gold rail. This gives the user visual continuity when they scroll up to find themselves.
4. **Number formatting** for the Push-ups column is critical — the row is dense and full-precision integers blow out the layout. Use `fmt()` from `primitives.jsx` as a reference.
5. **Sort order arrows** (▲/▼) are inline in the column header next to the active label — don't put them in a separate cell.
6. **Don't try to make all the chrome scrollable**. The header, scope tabs, period pills, search, and column header are all in fixed position above the scrolling list. Only the player list itself scrolls.
