# Frontend TODO

Frontend means Android UI, navigation, ViewModels, UI models, and user-visible state.

## Navigation

- [ ] Define routes for home, favorites, tags, settings, detail, import, search, trash, PDF export, and developer info.
- [ ] Add `ViewPager2` main app shell.
- [ ] Add `BottomNavigationView` main navigation.
- [ ] Add left-side `DrawerLayout` around the main app shell for utilities and quick filters.
- [ ] Define drawer gesture priority with `ViewPager2`.
- [ ] Define stable bottom navigation item IDs.
- [ ] Synchronize pager swipes with bottom navigation selection.
- [ ] Synchronize bottom navigation taps with pager page changes.
- [ ] Preserve selected pager page across configuration changes.
- [ ] Handle share intent entry route.
- [ ] Add detail navigation by memory ID.
- [ ] Add back behavior for import cancel.

## Screens

- [ ] Memory list screen.
- [ ] Import confirmation screen.
- [ ] Memory detail screen.
- [ ] Search/filter screen.
- [ ] Tag browser screen.
- [ ] Left drawer utility/filter menu.
- [ ] Tag management screen/dialog.
- [ ] Favorites screen.
- [ ] Trash screen.
- [ ] Settings/debug processing screen if needed.

## UI State

- [ ] Define `MemoryListUiState`.
- [ ] Define `MemoryDetailUiState`.
- [ ] Define `ImportUiState`.
- [ ] Define `SearchUiState`.
- [ ] Add loading, empty, error, and content states.

## User Interactions

- [ ] Import save/cancel.
- [ ] Retry OCR/classification.
- [ ] Search query input.
- [ ] Tag/category/date filter selection.
- [ ] Drawer category/tag filter selection.
- [ ] Active drawer filter clear action.
- [ ] User tag create/rename/delete.
- [ ] Manual tag assignment/removal from memory detail.
- [ ] Memo edit/save.
- [ ] Favorite/trash/permanent delete.

## Accessibility and UX

- [ ] Content descriptions for image/action buttons.
- [ ] Large text layout review.
- [ ] Loading indicators for processing.
- [ ] Clear destructive action confirmation.

## Status

Current Status: Not Started

Progress: 0%
