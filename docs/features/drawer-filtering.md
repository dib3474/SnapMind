# Drawer Filtering

## Goal

Use `DrawerLayout` as a left-side utility and quick navigation menu for the main app shell. Users can open the drawer with the toolbar menu button or a left-edge swipe, then jump to trash, PDF export, developer info, top tags, or category/tag filters.

This behaves like folder navigation in email apps: selecting a drawer item changes the visible list scope immediately.

## User Scenario

1. User opens SnapMind on the Memory page.
2. User taps the toolbar drawer button or swipes from the left edge.
3. App opens the left-side drawer.
4. Drawer shows utility actions plus top tags/category shortcuts.
5. User taps a popular tag or `Code` category.
6. Drawer closes.
7. App navigates to the relevant tab/list with the selected filter applied.
8. User can manage tags from the drawer or detail screen.

## Functional Requirements

- Use `DrawerLayout` around the main app shell.
- Open drawer by tapping the toolbar menu button or swiping from the left screen edge.
- Implement the drawer with `layout_gravity="start"` so it opens left-to-right in LTR layouts.
- Provide a visible drawer/menu button for accessibility and discoverability.
- Provide utility actions for trash, PDF export, developer info, and top-3 tag shortcuts.
- Show saved categories in the drawer.
- Show popular tags in the drawer.
- Show item counts for categories/tags where available.
- Selecting a category applies a category filter to the Memory list.
- Selecting a tag applies a tag filter to the Memory list.
- Drawer selection state must reflect the active filter.
- The active filter must be clearable.
- Filtered list must update immediately from Room `Flow`.
- Provide tag/category management entry points from the drawer.
- Allow user-created tags to be assigned to memories from memory detail.
- Allow tag rename/delete where safe.

## Drawer Sections

| Section | Items | Source |
| --- | --- | --- |
| Utility Actions | Trash, PDF export, developer info | Navigation routes |
| Quick Filters | All, Unprocessed, Favorites, Trash | `memory_items` status fields |
| Categories | Chat, Receipt, Code, Shopping, Travel, Food, Document, YouTube, Unknown | `classifications.label` and category mapping |
| Popular Tags | Most-used generated/user tags | `tags` and `memory_tag_cross_refs` |
| Tag Management | Create tag, Manage tags | tag management screen/dialog |

## Non-Functional Requirements

- Drawer should open smoothly without forcing Memory list reload.
- Category/tag counts should be loaded asynchronously.
- Drawer content should remain usable with at least 200 tags.
- Popular tags should be capped to avoid an overloaded drawer.
- Tag management actions must not block list filtering.

## UI Structure

- `DrawerLayout`
  - main content:
    - main toolbar
    - `ViewPager2` content
    - `BottomNavigationView`
  - drawer content:
    - Utility Actions section
    - Quick Filters section
    - Categories section
    - Popular Tags section
    - Tag Management actions

## Processing Flow

```text
User taps toolbar menu or swipes from left edge
  -> DrawerLayout opens
  -> User taps utility/category/tag item
  -> Navigation or ViewModel updates selected destination/filter
  -> DrawerLayout closes
  -> Room Flow emits filtered list when a filter was selected
  -> Target screen renders filtered results
```

## Data Flow

- Input: drawer item selection, active filter state, saved category/tag data.
- Output: updated `MemoryFilters`, selected drawer item state, filtered memory list.

## Database Interaction

- Read categories from `classifications.label`.
- Read tags from `tags`.
- Read tag usage counts from `memory_tag_cross_refs`.
- Query `memory_items` with selected category/tag filters.
- Update tag rows for create/rename/delete operations.
- Update `memory_tag_cross_refs` when user assigns or removes tags.

## API Interaction

No API dependency.

## AI/OCR Logic

- Categories come from TFLite classification output.
- Tags may come from auto-tagging rules, OCR keyword extraction, or user-created tags.
- Drawer filtering does not run AI processing directly.

## Error Handling

| Error | Handling |
| --- | --- |
| Category count query fails | Show category names without counts and keep drawer usable |
| Tag list query fails | Show drawer error row with retry |
| Selected tag was deleted | Clear active tag filter and show message |
| Rename creates duplicate tag | Block save and show duplicate tag message |
| Drawer gesture conflicts with pager | Left edge swipe opens drawer; non-edge horizontal swipe remains pager navigation |

## Edge Cases

- User selects a tag with zero visible memories after other filters are applied.
- Auto-tagging creates a new tag while drawer is open.
- User deletes the currently selected tag.
- User opens drawer from any bottom tab.
- Left edge swipe conflicts with horizontal pager gestures and Android back gesture.
- Very long tag names need truncation.

## Dependencies

- AndroidX DrawerLayout.
- Material components for navigation drawer/list items.
- Room.
- Kotlin Flow.
- ViewModel saved state.
- Search/filter feature.
- Auto-tagging feature.

## TODO Checklist

- [ ] Add `androidx.drawerlayout:drawerlayout` dependency if not already available transitively.
- [ ] Define drawer layout around `MainActivity`.
- [ ] Add drawer open button in Memory toolbar.
- [ ] Add left-edge swipe behavior.
- [ ] Define `DrawerFilterItem` UI model.
- [ ] Query category list and counts.
- [ ] Query popular tags and counts.
- [ ] Apply category filter from drawer selection.
- [ ] Apply tag filter from drawer selection.
- [ ] Show active filter chip and clear action.
- [ ] Add tag creation action.
- [ ] Add tag rename/delete management action.
- [ ] Add tag assignment/removal support in memory detail.
- [ ] Handle deleted selected tag.
- [ ] Add UI test for selecting `Code` category from drawer.
- [ ] Add UI test for selecting popular tag from drawer.

## Current Status

Not Started

## Progress Notes

- Drawer filtering should be implemented after base Memory list state exists.
- Gesture priority must be tested with `ViewPager2` because both components use horizontal gestures.

## Future Improvements

- Add custom drawer sorting.
- Add favorite tag shortcuts.
- Add saved filter presets.
- Add drag-and-drop tag organization.
