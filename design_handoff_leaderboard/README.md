# Handoff: Leaderboard Redesign (Variant A)

## Overview
This is a redesign of the **Leaderboard** screen for a fitness/RPG-style mobile app (push-up tracker with gamification, levels, and competitive scoring). The new design follows the existing app's dark, golden-accent visual language and presents a compact ranked list with medal styling for the top 3 players, scoped tabs, time filters, search, and a sticky "Your Standing" card pinned to the bottom.

## About the Design Files
The files in `reference/` are **design references created in HTML/JSX** — they are prototypes showing the intended look and behavior, **not production code to copy directly**. Your task is to **recreate this design in the existing app's codebase** (whatever framework it uses — SwiftUI, React Native, Flutter, native Android, etc.) using the codebase's established patterns, components, and libraries. The HTML mock uses React + Babel only because that's the prototyping environment; do not assume the target app is React.

## Fidelity
**High-fidelity (hifi)**. Colors, typography, spacing, and interactions are all final. Recreate the UI pixel-perfectly using the codebase's existing libraries and patterns.

## Screen: Leaderboard

### Purpose
Let the user see how they rank globally / by country / among friends, filter by time period, search by name, and always see their own standing.

### Layout (top to bottom, in a 390×844 iPhone viewport)
1. **Status bar / safe area** — 54px top inset (handled by device chrome).
2. **Header bar** — centered title `LEADERBOARD` flanked by gold `✦` decorative glyphs, with a back chevron `‹` on the left.
3. **Scope tabs** — segmented control: `Global` · `Country` · `Friends`. Active tab uses an orange gradient pill.
4. **Time tabs** — smaller segmented control directly below: `Day` · `Week` · `Month` · `All Time`. Active = orange pill.
5. **Search bar** — full-width, rounded (10px), with a magnifier icon and placeholder "Filter by name…".
6. **Column header strip** — `# · NAME · LVL · PUSH · PWR` (uppercase, 9.5px, letter-spaced, muted).
7. **Scrollable list of player rows** — fills remaining height.
8. **Sticky "Your Standing" card** — pinned at the bottom above the home-indicator inset; orange-tinted to highlight the current user.

### Components

#### Header
- Padding: `14px 16px 10px`
- Border-bottom: `1px solid rgba(255,255,255,0.06)`
- Background: subtle gradient `linear-gradient(180deg, transparent, #0e0d10)`
- Title `LEADERBOARD`:
  - Font: **Cinzel**, 700, 16px
  - Letter-spacing: `0.22em`
  - Color: `#f3c969` (gold)
  - Text-shadow: `0 0 12px rgba(243,201,105,0.25)` (subtle gold glow)
- Decorative `✦` glyphs on each side of the title, gold `#f3c969`, 12px, 85% opacity
- Back chevron `‹`: positioned left, 18px, color `#9b9389`

#### Scope Tabs (Global / Country / Friends)
- Container: flex row, `gap: 6px`, padding `12px 14px 8px`
- Each tab: `flex: 1`, padding `9px 0`, border-radius `10px`, font 12px / 600 / `letter-spacing 0.04em`
- **Inactive**: bg `rgba(255,255,255,0.03)`, border `1px solid rgba(255,255,255,0.06)`, color `#9b9389`
- **Active**: gradient `linear-gradient(180deg, #ffb152, #ff8a2a)`, border `rgba(255,138,42,0.6)`, color `#15110a`, shadow `0 4px 14px -4px rgba(255,138,42,0.55)`

#### Time Tabs (Day / Week / Month / All Time)
- Same as scope tabs but smaller: padding `6px 0`, font 11px, border-radius `8px`
- Container padding `4px 14px 10px`

#### Search bar
- Margin: `6px 14px 10px`
- Background: `rgba(255,255,255,0.04)`
- Border: `1px solid rgba(255,255,255,0.06)`
- Border-radius: `10px`
- Padding: `8px 10px`
- Magnifier icon (50% opacity), then `<input>` with placeholder color `#6e675f`, text 12px

#### Column Header
- Grid: `36px 1fr 44px 38px 50px` (gap 6px)
- Padding: `8px 14px`
- Font: 9.5px / 700 / uppercase / `letter-spacing: 0.14em`
- Default color: `#6e675f`
- `PUSH` column label: orange `#ff8a2a`
- `PWR` column label: green `#34c759`
- Top + bottom 1px borders `rgba(255,255,255,0.06)`
- Background: `rgba(255,255,255,0.015)`

#### Player Row (default)
- Same 5-column grid as header, padding `9px 14px`
- Border-bottom: `1px solid rgba(255,255,255,0.035)`
- Hover: bg `rgba(255,255,255,0.03)`
- **Rank number**: JetBrains Mono, 13px / 600, color `#6e675f`, centered
- **Avatar**: 26×26 circle, gradient `linear-gradient(135deg, #2a2428, #1a1719)`, border `rgba(255,255,255,0.10)`, emoji or image 12px inside
- **Name**: Inter, 13px / 500, color `#ece6d8`, truncate with ellipsis
- **Level**: JetBrains Mono, 12.5px / 600, right-aligned, color `#9b9389`
- **PUSH** value: JetBrains Mono, 12.5px / 600, color `#ffb152` (orange-2)
- **PWR** (wins) value: JetBrains Mono, 12.5px / 600, color `#5fdf7a` (green-2)

#### Top-3 Medal Styling
First three rows get gold/silver/bronze treatment on **rank number and avatar**:

- **1st (gold)**:
  - Rank color `#f3c969`, text-shadow `0 0 10px rgba(243,201,105,0.45)`
  - Avatar bg `radial-gradient(circle at 30% 20%, #6c4d18, #1a1719)`
  - Avatar border `rgba(243,201,105,0.4)`, glow `0 0 10px -2px rgba(243,201,105,0.4)`
- **2nd (silver)**:
  - Rank color `#cfd2d6`
  - Avatar bg `radial-gradient(circle at 30% 20%, #4a4d54, #1a1719)`
  - Avatar border `rgba(207,210,214,0.35)`
- **3rd (bronze)**:
  - Rank color `#cd8c4a`
  - Avatar bg `radial-gradient(circle at 30% 20%, #5a3a1c, #1a1719)`
  - Avatar border `rgba(205,140,74,0.4)`

#### Sticky "Your Standing" card (bottom)
- Padding: `10px 14px 14px`
- Border-top: `1px solid rgba(255,255,255,0.06)`
- Background: `linear-gradient(180deg, rgba(255,138,42,0.05), rgba(0,0,0,0.4))`
- Backdrop blur: `6px`
- Above the row, a tiny label:
  - Text `YOUR STANDING`, 9.5px / 700, letter-spacing `0.18em`, color `#ff8a2a`
- The row itself uses the same grid but is wrapped in a card:
  - Background: `linear-gradient(180deg, rgba(255,138,42,0.13), rgba(255,138,42,0.04))`
  - Border: `1px solid rgba(255,138,42,0.4)`
  - Border-radius: `10px`
  - Padding: `9px 8px`
  - Rank: orange `#ffb152` / 700
  - Avatar border: `rgba(255,138,42,0.55)`, bg `radial-gradient(circle at 30% 20%, #4a2a10, #1a1108)`
  - Name: pure white `#fff` / 600

## Interactions & Behavior
- **Tap scope tab** → reload list scoped to Global / Country / Friends
- **Tap time tab** → reload list filtered by Day / Week / Month / All Time
- **Type in search** → live client-side filter on the loaded list (case-insensitive substring on name)
- **Tap a row** → open player profile/detail (navigation target — design out of scope here)
- **Scroll** → list area scrolls; header, tabs, search, column-head, and "Your Standing" remain visually pinned
- All tab transitions: 180ms ease on bg/color/shadow
- Row hover (or pressed-state on touch): bg fades to `rgba(255,255,255,0.03)`

## State Management
- `scope`: `'Global' | 'Country' | 'Friends'` (default `'Global'`)
- `timeFilter`: `'Day' | 'Week' | 'Month' | 'All Time'` (default `'All Time'`)
- `searchQuery`: string
- `players`: array of `{ rank, name, avatar, level, push, reps, wins, country }` from API
- `self`: `{ rank, name, avatar, level, push, reps, wins }` (current user)

Data fetch should re-run when `scope` or `timeFilter` changes. Search is local.

## Design Tokens

### Colors
| Token | Value | Use |
|---|---|---|
| `--bg` | `#100f12` | Base background |
| `--bg-2` | `#181519` | Slightly elevated |
| `--panel` | `#1c1a1e` | Card surfaces |
| `--line` | `rgba(255,255,255,0.06)` | Subtle dividers |
| `--line-2` | `rgba(255,255,255,0.10)` | Stronger dividers / borders |
| `--text` | `#ece6d8` | Primary text |
| `--text-dim` | `#9b9389` | Secondary text |
| `--text-mute` | `#6e675f` | Tertiary / labels |
| `--orange` | `#ff8a2a` | Primary accent |
| `--orange-2` | `#ffb152` | Lighter accent / values |
| `--green` | `#34c759` | Power / positive |
| `--green-2` | `#5fdf7a` | Power values |
| `--gold` | `#f3c969` | 1st place / brand decoration |
| `--silver` | `#cfd2d6` | 2nd place |
| `--bronze` | `#cd8c4a` | 3rd place |

Background of the screen uses a layered gradient:
```
radial-gradient(1200px 600px at 50% -200px, rgba(255,138,42,0.06), transparent 60%),
linear-gradient(180deg, #131115 0%, #0c0b0e 100%)
```

### Typography
- **Display / brand title**: `Cinzel` 700 — used only for the screen title
- **UI / body**: `Inter` 400/500/600/700
- **Numbers / monospace**: `JetBrains Mono` 400/600

### Spacing
Common values used: `4, 6, 8, 9, 10, 12, 14, 16` px.
Row vertical padding: `9px`. Section side padding: `14px`.

### Border-radius
- Tabs: `10px` (large), `8px` (small)
- Cards / search bar: `10px`
- Avatar: `50%`

### Shadows / glows
- Active tab: `0 4px 14px -4px rgba(255,138,42,0.55)`
- Gold avatar: `0 0 10px -2px rgba(243,201,105,0.4)`
- Gold rank number: text-shadow `0 0 10px rgba(243,201,105,0.45)`

## Assets
- No bitmap assets — emoji are used as avatar placeholders. Replace with the app's existing avatar component / image URLs.
- The `✦` character is a Unicode glyph (U+2726).
- Icons (back chevron, magnifier) — use the codebase's existing icon set; the prototype uses Unicode/emoji.

## Files
All in `reference/`:
- **`leaderboard-variants.html`** — entry HTML (loads React, Babel, and the JSX files). Open this in a browser to see the design live.
- **`leaderboard-app.jsx`** — the actual leaderboard UI (`VariantA` component is the chosen design; `VariantB` is included for context but **not** the chosen direction).
- **`ios-frame.jsx`** — iPhone device chrome wrapper (used only for the prototype; ignore in implementation).
- **`design-canvas.jsx`** — pan/zoom canvas wrapper (used only for the prototype; ignore in implementation).

The CSS for the design lives in the `<style>` block of `leaderboard-variants.html`. The classes you care about are: `.screen`, `.app-header`, `.seg-tabs`, `.search`, `.col-head`, `.list`, `.row` (+ `.gold` / `.silver` / `.bronze` modifiers), `.standing`. Variant B's classes (`.podium`, `.pod`, `.row-b`, `.section-divider`) can be ignored.

## Mock data shape (for reference)
```ts
type Player = {
  name: string;
  avatar: string;   // emoji in mock — real app should use image URL
  level: number;
  push: number;     // total push-ups (primary metric)
  reps: number;
  wins: number;     // shown as "PWR"
  country: string;  // emoji flag in mock
};
```
