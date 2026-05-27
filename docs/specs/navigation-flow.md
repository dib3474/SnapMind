# Navigation Flow

## Routes

| Route | Parameters | Purpose |
| --- | --- | --- |
| `home` | none | Default memory grid |
| `favorites` | none | Favorited memories |
| `tagBrowser` | optional `tagId: Long` | Browse generated/user tags and tag-filtered photos |
| `settings` | none | App/API/debug settings |
| `memoryDetail/{memoryId}` | `memoryId: Long` | Memory detail and memo edit |
| `importPreview` | shared/import session ID | Confirm imported images |
| `search` | optional query/filter args | Search and filters |
| `trash` | none | Restore or permanently delete trashed memories |
| `pdfExport` | optional selected memory IDs | Export selected memories as PDF |
| `developerInfo` | none | Developer info page |
| `tagManage` | optional `tagId: Long` | Create, rename, or delete user-managed tags |

## Main Pager Navigation

The root app experience uses `ViewPager2` plus `BottomNavigationView`.

| Page Index | Bottom Nav ID | Route | Swipe Enabled | Notes |
| ---: | --- | --- | --- | --- |
| 0 | `nav_home` | `home` | Yes | Default launch page and memory grid |
| 1 | `nav_favorites` | `favorites` | Yes | Favorited memories |
| 2 | `nav_tags` | `tagBrowser` | Yes | Tag/category browsing |
| 3 | `nav_settings` | `settings` | Yes | Settings, API toggles, storage/debug info |

Search is opened from the home toolbar and is not a bottom-navigation tab. Detail, import preview, trash, PDF export, developer info, and tag management are overlay/secondary destinations above the pager.

### Synchronization Rules

- When `ViewPager2` page changes, update `BottomNavigationView.selectedItemId`.
- When bottom navigation item is selected, update `ViewPager2.currentItem`.
- Re-selecting the current bottom navigation item should keep the current page and may scroll that page to top if implemented.
- Invalid page index or item ID falls back to `home`.
- Detail and import destinations are not pager pages.
- The left-side `DrawerLayout` belongs to `MainActivity` and is opened from the toolbar or left edge. Implement it as a `START` drawer (`layout_gravity="start"`) so it slides from left to right in LTR layouts.

## Left Drawer Navigation

| Drawer Item Type | Action | Destination/Result |
| --- | --- | --- |
| Trash | Open trash | `trash` |
| PDF export | Open export flow | `pdfExport` |
| Developer info | Open info page | `developerInfo` |
| Popular tag | Apply tag filter | Switch to `tagBrowser` with selected `tagId` |
| Category shortcut | Apply category filter | Switch to `home` with category filter if implemented |
| Manage tags | Open tag management | `tagManage` |

## Primary Flows

### Share Import

```text
External App
  -> Android Share Sheet
  -> SnapMind importPreview
  -> Save
  -> memoryDetail/{memoryId}
```

### Search

```text
home
  -> toolbar search
  -> search
  -> memoryDetail/{memoryId}
  -> back to search results
```

### Main Feature Swipe

```text
home
  -> swipe left
  -> favorites
  -> swipe left
  -> tagBrowser
  -> tap Settings bottom nav item
  -> settings
```

### Drawer Popular Tag Shortcut

```text
home
  -> toolbar menu or left-edge swipe
  -> drawer opens
  -> tap popular tag
  -> drawer closes
  -> tagBrowser filtered by selected tag
```

### Drawer Utility Item

```text
home
  -> toolbar menu
  -> drawer opens
  -> tap Trash / PDF export / Developer info
  -> secondary destination opens above main pager
```

### Tag Management

```text
tagBrowser or left drawer
  -> tagManage
  -> create/rename/delete tag
  -> back to previous page with filter state restored when possible
```

### Memo Edit

```text
memoryDetail/{memoryId}
  -> edit memo inline
  -> save
  -> updated detail state
```

## Back Behavior

- Import cancel returns to previous app or home.
- Detail back returns to list/search source.
- Delete from detail navigates back to previous list.
- Search clear keeps user on search screen.
- Back from a detail screen returns to the originating pager page.
- Back on the main pager should exit the app unless a child page has its own back stack or active search/filter state.
- Back while drawer is open closes the drawer.
- Back from tag management returns to the previous page with the previous filter state restored when possible.
