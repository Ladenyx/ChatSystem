# Design System – Java Real-Time Chat System

Visual language shared by the **User Client** and the **Admin Console** (JavaFX). The implementation schedule and screen list live in [UI_IMPLEMENTATION_PLAN.md](UI_IMPLEMENTATION_PLAN.md).

## 1. Sources and scope

| Used for | Source | What we take |
|---|---|---|
| Components (buttons, tables, combo boxes, date pickers...) | AtlantaFX (PrimerLight theme) and the AtlantaFX Sampler | Ready-made controls; accent color overridden through the `-color-accent-*` variables |
| Design system and chat layout (User Client) | Dribbble shot "Web Chat UI" (<https://dribbble.com/shots/23280048-Web-Chat-UI>) | Palette, Poppins font, 4-zone layout, bubble style, rounded shapes |
| Admin layout | Library Management System (GitHub, KGM8) | Sidebar navigation with a content area, statistics panels, tabbed report screens |

Rules:
- Take the **visual language only**, not the features. The template also shows video calls, voice messages, file sharing, News/Archive sections, reactions and view counters. None of these are in the course specification, so they are skipped.
- References are for layout and organization only. The team writes its own code.

**Accuracy of the values below**
- Hex codes and the font name for `dark`, `accent`, `warn`, `bg` and `Poppins` are printed on the template's own style board, so they are **exact**.
- Light shades, radii, font sizes and zone proportions are **estimated by eye** from the template images. Verify them against the running app and adjust.

## 2. Color tokens

| Token | Value | Used for | Source |
|---|---|---|---|
| `dark` | `#202022` | left navigation rail, outer frame, primary dark buttons, main text | template |
| `accent` | `#7678ed` | sent-message bubbles (white text), send button, sender names in groups, read ticks, pin icon, selected items | template |
| `warn` | `#ff7a55` | unread-count badge, warning / delete / lock buttons | template |
| `bg` | `#f9fafc` | content area background | template |
| `accent-subtle` | about `#ebebfc` | received-message bubbles, selected conversation row, search box, secondary cards | estimated |
| `muted` | about `#8a8a95` | timestamps, message previews, secondary text | estimated |
| `online` | green, e.g. `#2ecc71` | online / offline dot (not clearly shown in the template) | team choice |
| `offline` | gray | offline dot | team choice |
| `danger` | red | locked account, error state | team choice |

Theme: **light** (AtlantaFX PrimerLight) with a dark navigation rail.

## 3. Typography

Font family: **Poppins** (Google Fonts, SIL Open Font License). Bundle the font files in the app resources and load them with `Font.loadFont`.

| Role | Size and weight (estimated) |
|---|---|
| Screen title / conversation name | 22px, SemiBold |
| Person name in lists, sender name in bubbles | 13px, SemiBold |
| Message text, previews | 12–13px, Regular |
| Timestamps, meta text, small rail labels | 10–11px, Regular |

**Vietnamese text risk.** Poppins may not cover all stacked Vietnamese diacritics (for example ế, ộ, ữ). UI labels are in English, but sample user names contain diacritics (for example "Nguyễn Thế Quân"). On day 1:
1. Load Poppins and render a few names with diacritics.
2. If glyphs are missing, keep Poppins for the English labels and add a Vietnamese-capable fallback font (for example Be Vietnam Pro) to `-fx-font-family`.

## 4. Shape, spacing and window sizes

| Element | Value |
|---|---|
| Spacing grid | 8px (use 8 / 16 / 24) |
| Outer frame | dark `#202022`; inner panels with large radius, about 20–24px |
| Conversation rows, search box, message input | radius about 12–16px; `accent-subtle` background when selected |
| Message bubbles | radius about 12px; 1–3 lines of text; small meta line (time) |
| Small controls (tags, chips) | radius 8px |
| Avatars | rounded square, radius about 10px; group avatar is a dark tile with initials (for example "DC") |
| Unread badge | circle in `warn`, white text |
| Icons | thin line icons, one consistent set (Ikonli offers Feather / Material style packs) |
| Window sizes | User Client 1100×700; Admin Console 1280×760; login / register 420×560 (keep fixed when taking screenshots so images are consistent) |
| UI language | English |

## 5. Layout: four zones (User Client)

Proportions are estimated from the template.

```
+------+--------------+---------------------------+-------------+
| Rail | Conversation | Chat window               | Info panel  |
| ~6%  | list  ~23%   | ~48%                      | ~20%        |
| dark |              | header: name, members,    | Group info  |
| icons| [Search]     |   search / more           | Members     |
| +    | [item]       | bubbles (received left,   | (toggle)    |
| label| [item]       |   sent right in accent)   |             |
|      | [item] ...   | [attach] [message] [send] |             |
| Prof |              |                           |             |
| Exit |              |                           |             |
+------+--------------+---------------------------+-------------+
```

## 6. Mapping to JavaFX and to project screens

| Template element | JavaFX | Project screen |
|---|---|---|
| Dark left rail (icon + small label, Profile / Log out at the bottom) | `VBox` with style class `rail`; `ToggleButton`s in one `ToggleGroup` | User main window: Chats, Friends, Online, Requests, Find people, Groups; bottom: Profile, Log out |
| Conversation list with Search box | `VBox` with `TextField` + `ListView` using a custom `ListCell` | Chat list; the same style is reused for Friends, Online friends, Friend requests and Find people |
| Chat window | `BorderPane`: header, `ScrollPane` + `VBox` of bubbles, input bar at the bottom | Chat window, group chat |
| Received / sent bubbles | `HBox` containing a `Label` with class `bubble-received` / `bubble-sent` | `MessageBubble` component |
| Right panel (Group info, Members, close button) | rounded card `VBox`, toggled from the header | AI assistant panel and Manage group (members, admins, rename, encryption); hidden when closed |
| Member card with "admin" tag | `ListView` + small label | Group member list |
| Unread badge, pin icon | round `Label` / icon | Optional (unread messages, spam reports) |

**Admin Console.** The layout follows Library Management System (sidebar + content area + statistics panels), but it **reuses the same palette, font and shapes** as above so both apps look consistent: dark rail on the left, rounded cards, accent `#7678ed`.

## 7. Starter `theme.css`

Check the exact variable names in the AtlantaFX Sampler / documentation before relying on them.

```css
.root {
    -fx-font-family: "Poppins";          /* add a Vietnamese-capable fallback if needed */
    -color-accent-fg: #7678ed;
    -color-accent-emphasis: #7678ed;
    -color-accent-subtle: #ebebfc;
    -color-bg-default: #f9fafc;
    -color-fg-default: #202022;
}
.rail            { -fx-background-color: #202022; }
.bubble-received { -fx-background-color: #ebebfc; -fx-background-radius: 12; }
.bubble-sent     { -fx-background-color: #7678ed; -fx-background-radius: 12; -fx-text-fill: white; }
.badge-unread    { -fx-background-color: #ff7a55; -fx-background-radius: 99; -fx-text-fill: white; }
.card            { -fx-background-color: #ebebfc; -fx-background-radius: 20; }
```

All theme overrides live in this one file, so changing the accent color later means editing a single place.

## 8. Open items

- Confirm that Poppins renders Vietnamese names correctly (day 1).
- Compare the estimated shades, radii and sizes with the running app and adjust the tokens.
- Choose the final `online`, `offline` and `danger` colors.
