# RelaxMusic Home And Player Premium Redesign Design

Date: 2026-04-01
Project: RelaxMusic Android app
Scope: Redesign the home page and now playing page around a premium playback-first experience, with supporting performance and state-architecture changes

## 1. Goal

This design upgrades the product from a functional local-player prototype into a clearer, more premium music experience.

The work follows this priority order confirmed during brainstorming:

1. home page product feel
2. player-page immersion
3. global visual consistency

The redesign must improve visual hierarchy and product identity without making playback, scrolling, or large-library interactions feel heavier.

## 2. Problems To Solve

### 2.1 Home page lacks a clear protagonist

The current home page mixes:

- folder management
- scan status
- many equal-weight feature cards
- continue playback
- recent playback

As a result, the screen reads like a utility dashboard instead of a music-product landing page.

### 2.2 Playback is visually present but not emotionally central

The app already has:

- a now playing page
- artwork and lyrics modes
- progress and transport controls

But the playback page still feels more functional than immersive. It does not yet feel like the emotional center of the app.

### 2.3 The visual system is coherent but not distinctive enough

The current theme already has a soft green direction, but:

- typography is still close to default system styling
- cards are visually similar to each other
- navigation and playback surfaces do not share one strong product language

### 2.4 High-frequency playback state is too broadly observed

Playback progress and lyric-related state currently update on a repeated interval and can affect UI state above the actual playback surface.

If the redesign adds richer hero cards and more layered playback visuals without reducing state coupling first, the app risks becoming prettier but less smooth.

### 2.5 Large-library data shaping is too in-memory

Search, album grouping, and artist grouping currently depend heavily on in-memory transformation. This is acceptable at prototype scale but becomes costly as the library grows.

## 3. Chosen Approach

Recommended approach: a focused product redesign with matching performance support.

The app should move toward a playback-first premium structure:

- the home page becomes a playback-oriented landing page with one strong hero
- the now playing page becomes the immersive peak of the experience
- the visual language shifts to a dark premium glass style
- state ownership is tightened so high-frequency playback updates stay local

This approach is chosen because it gives the user-visible product jump first, while still protecting runtime smoothness.

## 4. Home Page Design

### 4.1 Home page role

Home is no longer a general-purpose feature grid.

Its job is to answer:

- what am I listening to now?
- what did I listen to recently?
- what are the fastest next actions?

Utility actions such as directory management and sync remain available, but they no longer compete for first-screen attention.

### 4.2 Chosen structure

Confirmed structure:

- A1 large hero first
- mixed second screen: one recent-play row plus four quick-entry cards

Recommended module order:

1. lightweight header
2. continue playback hero
3. recent playback preview
4. quick-entry row
5. library status and utility section

### 4.3 Header

The header stays lightweight and product-oriented.

It contains:

- brand title
- short contextual supporting text
- settings shortcut as a secondary action

It does not contain primary utility buttons like add-folder or rescan.

### 4.4 Continue playback hero

This is the visual and product protagonist of the screen.

It shows:

- current song title
- artist and album
- playback progress
- main transport action
- clear tap path into the full player page

The hero should feel visually linked to the player page so the transition from home to playback feels intentional rather than abrupt.

### 4.5 Recent playback section

Recent playback becomes the first supporting content below the hero.

Recommended behavior:

- show a compact horizontal preview
- keep the visible item count limited
- allow navigation to the full history area

This section supports quick return behavior without replacing the dedicated history destination.

### 4.6 Quick entries

Confirmed quick-entry set:

- 全部歌曲
- 歌单
- 收藏
- 历史

These entries should appear as compact, visually secondary cards under recent playback.

Albums and artists should no longer occupy equal homepage priority. They can remain accessible through library-focused destinations instead of first-screen tiles.

### 4.7 Library status and utility area

Library status, directory management, and scan actions move lower in the page.

This section should communicate:

- song-count summary
- last sync or scan state
- entry into library management

It should feel operational, not heroic.

## 5. Home Page Visual Direction

### 5.1 Chosen style

Confirmed visual direction: S2 premium dark glass.

This means:

- deep dark base surfaces
- soft green highlights
- translucent glass-like supporting cards
- one brighter, more luminous hero card
- restrained contrast outside the hero

### 5.2 Visual hierarchy rules

- The continue-playback hero is the brightest and most dimensional element on the screen
- Supporting cards use lower contrast and thinner borders
- Text hierarchy should clearly separate hero title, section title, metadata, and supporting text
- Empty states and utility sections should be calm and quiet rather than loud

### 5.3 Product intent

The home page should feel premium and music-centered, but still practical for a local library app.

It should not feel like:

- a generic settings dashboard
- a content feed
- an over-designed concept screen with weak utility

## 6. Now Playing Page Design

### 6.1 Player page role

The now playing page becomes the emotional and visual peak of the product.

Its role is to provide:

- immersive playback context
- artwork-first or lyrics-first focus
- stable transport and progress controls
- a clear sense that this is the app’s core screen

### 6.2 Chosen relationship to home

The player page should inherit the visual language of the home hero card, but become deeper, quieter, and more focused.

Home is the invitation.
Player is the destination.

### 6.3 Artwork mode

Artwork mode should center the artwork in a large, dimensional container with:

- soft glow and depth
- minimal surrounding noise
- large track metadata below
- a stable control dock

This mode should feel like a stage for the current track, not just an image above a slider.

### 6.4 Lyrics mode

Lyrics mode should keep the user’s existing mental model of tap-to-switch, but visually evolve from a flat list into a focused lyric stage.

The active line should feel clearly centered and elevated.
Nearby lines should remain visible for context, but visually de-emphasized.

The goal is better readability and atmosphere, not visual gimmicks.

### 6.5 Stable bottom control dock

The player page should keep one consistent control area across artwork and lyrics modes.

That dock contains:

- progress bar
- time labels
- previous / play-pause / next
- play mode
- sleep timer

This stability matters because the upper part of the screen changes mode, while the bottom part should anchor interaction.

### 6.6 Information density rules

- The player page should not stack too many small controls around the artwork
- Secondary actions should stay visually quieter than primary transport controls
- The play-pause control should remain the strongest action
- Metadata should remain readable without turning into a dense information block

## 7. Shared Visual System

### 7.1 Theme direction

The redesign should introduce a stronger semantic token system rather than relying only on a small set of raw colors.

Recommended token categories:

- app background layers
- hero surfaces
- secondary glass surfaces
- borders
- accent and accent-strong
- primary and secondary text
- success / warning / error roles for operational states such as scan, backup, and error feedback

### 7.2 Component language

The following components should share one visual language:

- home hero card
- recent playback cards
- quick-entry cards
- player control dock
- bottom navigation container

They should feel like parts of one product family, not unrelated Material defaults.

### 7.3 Typography direction

Typography should become more distinctive and more controlled:

- one stronger display/title style for hero and player titles
- calmer section titles
- compact metadata and supporting text
- less dependence on default platform font combinations

The typography should support premium feel without reducing readability in Chinese UI text.

## 8. Performance And State Architecture

### 8.1 Playback state layering

High-frequency playback state must stay close to the playback surface.

Top-level consumers should observe only low-frequency summary state such as:

- current song
- is playing
- lightweight playback summary

High-frequency state such as:

- progress updates
- current lyric index
- rapid lyric-scroll state
- sleep timer countdown

should be scoped to the player screen or other local consumers that truly need it.

### 8.2 Home-page playback summary

The home hero should use lightweight playback summary state rather than full player-screen state.

This prevents the home page, navigation graph, and unrelated destinations from redrawing on every progress tick.

### 8.3 Search and grouping

Search should move from in-memory filtering toward Room-backed querying.

Album and artist grouping should move toward database-side aggregation, starting with the dedicated album and artist browsing surfaces.

This keeps large-library cost from growing linearly inside Compose-facing state transformations.

### 8.4 Scan pipeline

Scanning should evolve from full repeated reads toward incremental behavior.

Recommended direction:

- detect unchanged files using stable file attributes
- skip unnecessary metadata re-read where safe
- surface real progress information
- keep valid persisted song state such as favorites and history

### 8.5 Artwork and lyrics performance

Artwork loading should use:

- constrained decode size
- caching by song id or uri
- no oversized raw bitmap decoding into the UI path

Lyrics rendering should:

- keep the current-line experience strong
- avoid unnecessary full-list animation cost
- preserve smoothness during repeated progress updates

### 8.6 Database support

The redesign should be accompanied by practical query support improvements such as:

- indexes for common sort and filter columns
- history-related indexes
- keep FTS out of the initial redesign pass; only consider it later if indexed Room-backed search is still not sufficient

The goal is measured scale readiness, not premature complexity.

## 9. Error Handling

- If artwork extraction fails, keep the player visually coherent with a strong fallback state
- If lyrics are missing, keep the immersive player layout intact rather than collapsing into a weak empty page
- If a scan partially fails, preserve the prior valid library state and surface a clear operational status
- If search or grouped queries are refactored, result ordering and current navigation behavior must remain predictable

## 10. Validation Plan

### 10.1 Product validation

- confirm the home page immediately communicates a primary action
- confirm the player page feels more immersive than the current version
- confirm home and player clearly belong to the same visual system

### 10.2 Interaction validation

- continue playback from home into now playing
- switch between artwork and lyrics modes repeatedly
- navigate from home into full library, playlists, favorites, and history
- interact with playback controls while remaining on the player page

### 10.3 Smoothness validation

- play a track for several minutes and verify the app feels stable during progress updates
- test lyrics on tracks with dense lyric timing
- scroll long song lists and recent history after the redesign
- interact with the app while scanning or rescanning directories

### 10.4 Regression validation

- favorites still persist across rescans
- playback history still records and displays correctly
- playlists still create, rename, delete, and add songs correctly
- theme mode switching still applies reliably

## 11. Out Of Scope

- changing product scope beyond the confirmed redesign areas
- remote streaming features
- recommendation systems
- cross-device sync
- full app-wide re-architecture beyond what is needed for playback-state isolation and data-query improvements

## 12. Expected Outcome

After this redesign:

1. the home page feels like a premium entry into playback, not a utility page
2. the now playing page feels immersive and product-defining
3. the visual language across home and player becomes more intentional and memorable
4. the supporting performance work keeps the richer UI from increasing runtime friction
