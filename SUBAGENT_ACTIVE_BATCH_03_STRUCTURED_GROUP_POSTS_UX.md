# BATCH-03-STRUCTURED-GROUP-POSTS-UX

## Status
`Deferred pending navigation/back-stack stabilization`

## Date
`2026-06-09`

## Owner
`Subagent`

## PM Direction
Moderated group posts must stop looking like one-line chat messages.

However, this batch is now **lower priority than navigation/back-stack and room-loading stabilization**.
Do not execute this batch before the blocking navigation/room issues are addressed.

They should feel like **structured community articles** in the same product family as the wiki feed:
- clear author header
- clear post title
- readable body preview
- tags / topic chips
- optional image
- visible status treatment where relevant

This is a **product UX/structure upgrade**, not a moderation redesign.

---

# Product Goal

Turn community group posts from:
- flat text rows

into:
- **structured post cards** that feel like real health-community posts

The user should immediately understand:
- who wrote it
- what the post is about
- what the main message is
- whether it is approved / pending / rejected when relevant

---

# Why This Batch Exists

Current moderated group posts are technically functional, but visually they still read like:
- a message list
- a moderation queue dump
- a temporary prototype

That is not good enough for long-term product quality.

The wiki feed already sets a better standard:
- title-first hierarchy
- richer card structure
- clearer content scanning

Group posts should move much closer to that standard.

---

# Non-Negotiable Product Requirements

## 1. A group post is not a chat message
Do not present it like a plain message bubble or plain text row.

## 2. A structured post must have a title
For this batch:
- `title` becomes a first-class field
- `content` remains the body

## 3. Cards must be scannable
At minimum, each visible post card must show:
- author identity
- created time/date
- post title
- body preview
- tags if present

## 4. Moderation state must remain obvious
In `BÃ i cá»§a tÃ´i` and `Chá» duyá»‡t`, status display must still be clear:
- `Chá» duyá»‡t`
- `ÄÃ£ duyá»‡t`
- `Bá»‹ tá»« chá»‘i`
- rejection reason where applicable

## 5. Do not break the approved/pending/rejected lifecycle
This batch improves structure and UX.
It must not weaken moderation rules.

---

# Scope

This batch includes:

1. Structured data shape for moderated group posts
2. Structured create-post form
3. Structured public feed cards
4. Structured my-post cards
5. Structured moderation queue cards
6. Backward-compatible rendering for old posts with missing title/tags

This batch does **not** include:
- comments/replies for moderated posts
- likes/reactions for moderated posts
- rich text editor
- markdown renderer
- poll/event/live features
- full media gallery system

---

# Required Data Model Direction

## Backend

Extend group post data to support:

- `title: String` **required for new posts**
- `content: String` body text
- `tags: String?` optional, comma-separated is acceptable for this batch
- `imageUrl: String?` keep existing optional support if already present

If a schema migration is required, keep it minimal and pragmatic.

### Backward compatibility rule
Old rows that have body but no title must still render safely.

Use fallback behavior such as:
- derive title from the first meaningful sentence or first line of content
- keep the rest as preview/body text

Do not let existing data disappear just because it predates the structured model.

## Frontend

`GroupPost` must expose enough structured fields to support the new UI:
- title
- content
- tags
- imageUrl
- author metadata already present
- moderation status metadata already present

---

# Create Post UX Requirements

## Replace the current flat composer with a structured form

The create post screen must contain:

1. `TiÃªu Ä‘á»`
   - required
   - short, clear subject

2. `Ná»™i dung`
   - required
   - multiline body

3. `Chá»§ Ä‘á» / tháº»`
   - optional
   - simple comma-separated entry is acceptable
   - render later as chips

4. `áº¢nh minh há»a`
   - optional if already supported

5. clear moderation note:
   - `BÃ i viáº¿t sáº½ Ä‘Æ°á»£c gá»­i chá» duyá»‡t trÆ°á»›c khi hiá»ƒn thá»‹ cÃ´ng khai`

## Validation rules
- cannot submit without title
- cannot submit without content
- whitespace-only title/content must be rejected

---

# Feed Card UX Requirements

## Public Feed (`BÃ i viáº¿t`)

Each approved post card must look like a **real article card**, not a message row.

### Required layout hierarchy
1. Header row
   - avatar / initial
   - author name
   - doctor/admin visual trust marker if applicable
   - created date/time

2. Title
   - prominent
   - bold
   - clearly separated from body

3. Body preview
   - 3 to 5 lines max in feed context
   - trimmed elegantly

4. Optional image
   - show if available

5. Tags row
   - chip-style tags if present

### Important
This must visually feel closer to `ArticleFeedCard` than to the current `GroupPostCard`.

You do **not** need to clone the wiki UI 1:1, but the information hierarchy must be similarly rich.

---

## My Posts (`BÃ i cá»§a tÃ´i`)

Use the same structured card, plus:
- moderation status chip
- rejection reason block when status is rejected

Status must not dominate the whole card, but it must be clearly visible.

---

## Pending Queue (`Chá» duyá»‡t`)

Use the same structured card, plus:
- moderation action row
  - `Duyá»‡t`
  - `Tá»« chá»‘i`

The moderator should be able to scan:
- title
- author
- short preview
- tags

without opening a separate detail screen first.

---

# Visual Rules

## Required improvements
- title must be visually stronger than body
- body preview must have readable spacing
- cards should feel like content cards, not chat list cells
- tags should be rendered as chips
- empty states should remain clean and honest

## Avoid
- giant unbroken blocks of text
- plain stacked text with no hierarchy
- moderation queue that looks like a database dump
- status text replacing actual content hierarchy

---

# Technical Pragmatism Rules

## Minimize destructive churn
Do not redesign the whole community module.

Prefer:
- extending existing `GroupPost`
- extending current DTOs / requests
- replacing the current flat `GroupPostCard` with a richer version
- upgrading `CreateGroupPostScreen`

## Preserve existing flow
Do not break:
- `Tháº£o luáº­n`
- `BÃ i cá»§a tÃ´i`
- `Chá» duyá»‡t`
- approve/reject
- approved-only feed

---

# Acceptance Criteria

This batch is successful only if:

1. New group posts require a `title` and `content`
2. Public group posts render as structured content cards
3. My posts render as structured content cards with visible status
4. Pending posts render as structured moderation cards with action buttons
5. Old posts without title still render safely using fallback logic
6. Moderation lifecycle still works exactly as before
7. Frontend build succeeds
8. Backend build succeeds

---

# Required Report Format

Return in this exact format:

## 1. Batch Status
Choose one:
- `Code-level complete`
- `Partially complete`
- `Blocked`

## 2. Files Changed
List every touched file

## 3. Data Model Changes
Explain exactly what fields were added or changed

## 4. Create Post UX Changes
Describe the new form structure

## 5. Feed Card Changes
Describe how the public feed, my-posts view, and moderation queue now differ from the old flat layout

## 6. Backward Compatibility Handling
Explain how old posts without title are rendered

## 7. Remaining Gaps
Be honest and specific

## 8. Build Result
Include:
- frontend build result
- backend build result

---

# Hard Rules

1. Do not turn this into a rich text editor project
2. Do not redesign moderation logic
3. Do not remove backward compatibility for old posts
4. Do not keep the UI as flat text rows and call it done
5. Do not broaden into unrelated community features

---

# Final Expectation

After this batch, a moderated group post should feel like:

**a lightweight community article**

not:

**a line of chat text with a date next to it**
