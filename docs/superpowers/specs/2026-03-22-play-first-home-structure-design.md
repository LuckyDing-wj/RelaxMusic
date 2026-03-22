# RelaxMusic Play-First Home Structure Design

Date: 2026-03-22
Project: RelaxMusic Android app
Scope: Reorganize top-level navigation and playback-related surfaces around a play-first structure

## 1. Goal

The app should feel like a music player first, not a stack of category entry cards.

This design keeps bottom navigation, but changes the product structure so that:

- the home page becomes a playback-oriented dashboard
- library browsing moves into a dedicated library area
- user-owned collections move into a dedicated list area
- the mini player and now playing screen stop repeating the same information density

The target outcome is better information hierarchy on real devices, less visual crowding, and fewer repeated feature entrances across screens.

## 2. Current Problems

### 2.1 Home page is overloaded

The current home page mixes several different responsibilities in one screen:

- library status
- folder management
- search entry
- full library entry
- favorites
- recent playback
- playlists
- albums
- artists

This creates a page that reads like a collection of unrelated cards instead of a clear landing surface.

### 2.2 Feature entrances are duplicated across layers

The current product structure splits entry points between bottom navigation and home-page cards:

- bottom navigation exposes playlists, history, and settings
- home-page cards expose full library, playlists, favorites, albums, artists, and recent playback

This weakens user orientation because the same destination types are spread across both top-level and secondary layers.

### 2.3 Playback surfaces compete with each other

The bottom player bar currently carries several actions and multiple lines of metadata, while the now playing screen also exposes those same concepts.

This causes two product issues:

- the mini player feels visually heavy instead of quick
- the now playing page does not clearly own the “full playback control” role

### 2.4 Typography is too aggressive for dense mobile surfaces

On real devices, large type across multiple cards and playback sections makes the UI feel crowded and inconsistent. Too many elements read like primary content at the same time.

## 3. Chosen Approach

Recommended approach: keep bottom navigation, but reorganize the app into four clear top-level areas:

- 首页
- 曲库
- 列表
- 设置

This is the chosen structure because it preserves the user’s preferred bottom navigation pattern while fixing the current responsibility overlap.

## 4. Top-Level Information Architecture

### 4.1 首页

Home becomes a playback-first dashboard.

Its job is to answer:

- what am I listening to now?
- what did I listen to recently?
- what playback actions do I need right now?
- how do I jump into the library quickly?

Home is not responsible for exposing every category as a peer card.

### 4.2 曲库

Library becomes the dedicated browsing area for discovery inside the local music collection.

This area owns:

- 全部歌曲
- 专辑
- 艺术家
- 搜索

These items should stop competing for attention on the home page.

### 4.3 列表

List becomes the user-owned and history-oriented area.

This area owns:

- 歌单
- 收藏
- 历史

This gives the app a stable place for content the user curates or revisits, rather than scattering those entrances across home and bottom navigation.

### 4.4 设置

Settings continues to own system-level capabilities:

- music directories
- theme mode
- backup and restore
- development or maintenance status

## 5. Home Page Content Structure

The home page should be simplified into four primary modules.

Recommended section order:

1. Header
2. Continue playback
3. Recent playback
4. Playback shortcuts
5. Entry into library browsing

### 5.1 Header

The header should stay lightweight:

- page title
- minimal supporting status
- settings shortcut only if still needed for convenience

It should not try to summarize every module.

### 5.2 Continue playback

This becomes the main hero card on home.

It should show:

- current song title
- artist
- playback progress
- one strong action to enter now playing

This card replaces the need for home-page playback information to be repeated across several small cards.

### 5.3 Recent playback

Recent playback should remain a compact preview, not a full history screen.

Recommended behavior:

- show 3 to 5 recent songs
- allow tapping through to the full history page under 列表

### 5.4 Playback shortcuts

Home should expose only playback-related shortcuts that matter in the current listening context:

- sleep timer
- playback queue
- play mode

These are context actions, not collection-browsing actions.

### 5.5 Library entry

Home should include one clear entry into 曲库, with wording similar to:

- 浏览曲库
- 全部歌曲、专辑、艺术家

This replaces several separate category cards on the home page.

## 6. Home Page Content to Remove

To reduce duplication and restore hierarchy, the home page should stop showing these as peer summary cards:

- 完整曲库
- 专辑
- 艺术家
- 歌单
- 收藏

Recent playback may remain on home as a compact preview, but its full ownership belongs to 列表.

## 7. Bottom Player Bar Role

The bottom player bar should become a true mini player.

Its job is:

- quick awareness of what is playing
- quick play/pause
- single-tap entry into now playing

Recommended content:

- artwork or playback-state icon
- song title
- artist
- play/pause button

Recommended removals from the mini player:

- queue button
- sleep timer button
- extra metadata line such as local-file label and duration

This should reduce visual weight and avoid turning the bottom bar into a second control center.

## 8. Now Playing Role

The now playing page should become the clear owner of full playback control and immersive playback context.

It should own:

- detailed track metadata
- progress scrubbing
- previous / play-pause / next
- play mode
- queue access
- sleep timer
- artwork / lyrics immersive area

The page should feel layered, not flat.

Recommended layout hierarchy:

1. Header with back and one utility action
2. Central artwork or lyrics area
3. Track metadata
4. Progress section
5. Primary playback controls
6. Secondary actions such as queue, timer, and play mode

## 9. Typography and Visual Hierarchy

Typography should be calmed down across dense surfaces.

### 9.1 Home page

- only one card should feel like the primary card
- card titles should share one size level
- supporting text should share one smaller size level
- supporting text should usually stay to one line

### 9.2 Bottom player bar

- title should use a medium emphasis size
- artist should use a smaller supporting size
- no third text line

### 9.3 Now playing

- song title can remain prominent, but should not overpower the screen
- artist and album should be clearly secondary
- highlighted lyric size should be closer to normal lyric size
- empty lyric state should use restrained copy instead of a large attention-grabbing heading

## 10. Navigation and Screen Ownership Mapping

Recommended mapping from current destinations:

- `Home` keeps the simplified playback-first dashboard role
- `FullLibrary`, `Albums`, and `Artists` become children of the new 曲库 top-level area
- `Playlists`, `Favorites`, and `History` become children of the new 列表 top-level area
- `NowPlaying` remains a detail screen owned by playback flow, not a bottom tab

This means the app should stop using the home tab as the implicit parent for albums, artists, and favorites.

## 11. Component and Implementation Impact

Expected structural changes:

- `BottomNavigationBar` needs new top-level destinations and labels
- navigation mapping logic in `RelaxMusicDestination` and `RelaxMusicNavGraph` must reflect the new ownership model
- `LibraryScreen` should become a playback-first home dashboard
- full-library-related browsing UI should be grouped under 曲库 entry flow
- playlists, favorites, and history should be grouped under 列表 entry flow
- `PlayerBar` should be simplified to mini-player responsibilities
- `NowPlayingScreen` should split primary controls from secondary controls and reduce type pressure

## 12. Error Handling and Edge States

The simplified structure must still handle empty or partial data cleanly.

### 12.1 Empty library

Home should still explain how to start:

- choose music directory
- show empty-state guidance
- avoid showing dead-end cards for categories with no content

### 12.2 No active playback

The continue-playback card and mini player should both degrade cleanly:

- neutral title
- simple instruction to choose music
- no misleading progress or advanced controls

### 12.3 No history or favorites

Preview sections should either hide entirely or render a compact empty state, rather than keeping large empty containers.

### 12.4 No lyrics

Now playing should show a low-emphasis empty-lyrics state and keep focus on artwork or playback controls.

## 13. Validation Plan

### Information architecture checks

- home no longer acts as a catch-all feature board
- all library browsing destinations are reachable through 曲库
- playlists, favorites, and history are reachable through 列表
- bottom navigation labels and behavior match the new ownership model

### Home page checks

- the page feels lighter than before on a real device
- the continue-playback card is the obvious primary module
- recent playback and playback shortcuts read as supporting modules

### Mini player checks

- the bottom bar is visually smaller and easier to scan
- the bar exposes only quick playback affordances
- entering now playing from the bar feels obvious

### Now playing checks

- primary controls are visually distinct from secondary actions
- typography reads clearly on real devices without oversized emphasis
- lyrics, metadata, and controls no longer compete equally for attention

## 14. Risks and Mitigation

### Risk

Users accustomed to finding playlists or favorites on the home page may need to relearn where those features live.

### Mitigation

Use clear bottom-navigation labels and keep the home-to-library entry explicit during the transition.

### Risk

If the new 列表 label is unclear, users may not predict that favorites and history live there.

### Mitigation

Validate the wording during implementation and consider using concise supporting copy on first load if needed.

### Risk

Over-reducing the mini player could hide useful power-user actions.

### Mitigation

Keep those actions in now playing, where they remain available without crowding the persistent UI.

## 15. Out of Scope

- redesign of database or playback engine behavior
- background playback architecture changes
- artwork loading implementation changes
- lyrics parsing changes
- backup feature redesign

## 16. Expected Outcome

After implementation:

- the app feels more structured and less repetitive
- the home page supports listening instead of trying to host every collection entrance
- the bottom player bar becomes easier to read on real devices
- now playing becomes the unambiguous home for complete playback control
- the overall app hierarchy becomes easier to learn and maintain
