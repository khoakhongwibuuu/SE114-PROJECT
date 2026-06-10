# [TECH LEAD DIRECTIVE: FAMILY HUB 3-TAB REFACTOR + CHAT/COMMUNITY UX BUGFIX PASS]

## [CONTEXT]
Recent code review and user-perspective QA found that the current communication and community architecture has regressed into an incoherent state:

- `Tin nhắn` currently mixes multiple unrelated concepts:
  - family chat
  - AI chat
  - community/group chat directory
- `Gia đình` currently contains only:
  - `Thành viên`
  - `Tủ thuốc`
- This leaves family chat in the wrong place and makes the mental model confusing.

We are correcting the information architecture:

### Family hub must become 3 tabs
- `Thành viên`
- `Tủ thuốc`
- `Trò chuyện`

### Inbox must be communication-only
- `AI Care`
- `Bác sĩ`

### Community must own community groups
- community group discovery and entry must live under `Cộng đồng`
- not inside `Tin nhắn`

In addition, several real UX / logic regressions were found and must be fixed in the same pass.

This is not a greenfield rewrite.
You must refactor the current Kotlin codebase carefully and preserve working flows wherever possible.

---

## [MISSION]
Act as a Senior Android UX Stabilization Engineer.

You must:
1. refactor the Family hub into **3 tabs**
2. cleanly isolate Inbox to only `AI Care` and `Bác sĩ`
3. keep community group chat entry inside `Cộng đồng`
4. fix the specific chat/community/social UX bugs listed below
5. clean UTF-8 / mojibake in all touched files

Do not fake missing flows.
Do not claim closure if the IA is still mixed or the interaction states remain misleading.

---

## [STRICT EXECUTION PROTOCOL (STEP-BY-STEP)]

### Step 1: Refactor Family hub to 3 tabs

Modify:
- `frontend/app/src/main/java/com/example/carenest/feature/family/presentation/FamilyFlowScreen.kt`

#### Requirements
Family hub must contain exactly these top tabs:
- `Thành viên`
- `Tủ thuốc`
- `Trò chuyện`

#### Behavior
- `Thành viên`
  - preserve the existing family flow:
    - `FamilyPickerScreen`
    - `FamilyManagementScreen`
- `Tủ thuốc`
  - preserve the current `MedicineScreen` integration and its callbacks
- `Trò chuyện`
  - this is now the correct home for **family chat**
  - move current family-chat-related UI/shell here

#### Hard constraints
- do not break the existing member-management flow
- do not break medicine navigation:
  - `AddMedicine`
  - `MedicineSchedule`
  - `AddMedicineSchedule`
  - `OcrScanner`
- if family chat is not fully production-ready, it must still be shown here as a truthful shell/placeholder rather than hidden in Inbox

---

### Step 2: Restore Inbox to communication-only

Modify:
- `frontend/app/src/main/java/com/example/carenest/feature/main/presentation/ChatHubScreen.kt`

#### Requirements
`Tin nhắn` must contain only:
- `AI Care`
- `Bác sĩ`

#### Must remove
- `FAMILY`
- `CHAT_GROUPS`
- any embedded family chat pane
- any community group directory pane from Inbox

#### Behavior
- `AI Care`
  - preserve the current AI assistant chat experience
- `Bác sĩ`
  - keep as truthful placeholder unless real doctor direct messaging exists
  - if there is a valid appointment/consultation route already in app, CTA may point there
  - do not fake direct doctor chat

---

### Step 3: Keep community groups inside Community, and make group entry actually work

Modify:
- `frontend/app/src/main/java/com/example/carenest/feature/community/presentation/CommunityScreen.kt`
- `frontend/app/src/main/java/com/example/carenest/feature/community/presentation/SocialGroupsPane.kt`

#### Current defect to fix
`SocialGroupsPane` already supports `onOpenGroup(group)`, but `CommunityScreen` currently throws it away by passing an empty callback.

#### Required fix
- wire `CommunityScreen -> SocialGroupsPane -> onOpenGroup` correctly
- clicking a community group card or its CTA button must actually open the correct community chat room
- do not leave no-op click handlers

#### Hard constraint
- community group chat must remain owned by `Cộng đồng`, not `Tin nhắn`

---

### Step 4: Fix misleading or broken chat-state UX in community chat

Modify:
- `frontend/app/src/main/java/com/example/carenest/feature/chat/presentation/ChatViewModel.kt`
- `frontend/app/src/main/java/com/example/carenest/feature/chat/data/repository/ChatRepository.kt`
- `frontend/app/src/main/java/com/example/carenest/feature/chat/presentation/ChatScreen.kt`

#### Current defects to fix
1. fallback-success strings are mojibake and fail to match UI logic
2. chat can show a red error-like state even when REST fallback actually saved the message successfully
3. several default/fallback sender labels and message strings are still mojibake

#### Required fix
- clean all fallback/success/error strings to proper UTF-8 Vietnamese
- ensure “saved via fallback / realtime reconnecting” is shown as a **non-error** state
- only real failures should appear in red/error styling
- ensure sender labels, reply previews, and repository fallback messages are human-readable

---

### Step 5: Fix social reaction state truthfulness

Modify:
- `frontend/app/src/main/java/com/example/carenest/feature/social/domain/model/SocialModels.kt`
- any DTO / mapping / repository files needed in the social feature
- `frontend/app/src/main/java/com/example/carenest/feature/social/presentation/SocialFeedScreen.kt`
- `frontend/app/src/main/java/com/example/carenest/feature/social/presentation/PostDetailScreen.kt`
- `frontend/app/src/main/java/com/example/carenest/feature/social/presentation/components/PostCard.kt`

#### Current defect to fix
The current social UI does not reflect server-truth liked state correctly:
- feed cards default to unliked
- post detail initializes local liked state to false
- there is no reliable `likedByMe` source in the current social `Post` domain model

#### Required fix
- add or wire a truthful `likedByMe`-style field through the social model/data pipeline
- make feed cards render correct initial liked state from server data
- make post detail initialize from real server state rather than default false
- preserve the existing failure rollback behavior for post detail like

#### Hard constraint
- do not fake liked state locally without a real backing field

---

### Step 6: UTF-8 / mojibake cleanup in all touched files

Mandatory cleanup scope includes all touched files, especially:
- `FamilyFlowScreen.kt`
- `ChatHubScreen.kt`
- `FamilyChatPane.kt`
- `CommunityScreen.kt`
- `SocialGroupsPane.kt`
- `ChatViewModel.kt`
- `ChatRepository.kt`
- any touched social files

All Vietnamese UI strings must be clean and readable.
Do not claim encoding is fixed unless the actual source is clean.

---

## [VERIFICATION REQUIREMENT]

### Build verification
Required:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

Batch is not acceptable without `BUILD SUCCESSFUL`.

### Reasoned runtime verification checklist
Your report must explicitly cover:

1. `Gia đình`
   - tab `Thành viên` still works
   - tab `Tủ thuốc` still works
   - tab `Trò chuyện` now hosts family chat

2. `Tin nhắn`
   - contains only `AI Care` and `Bác sĩ`
   - no family chat
   - no community group directory

3. `Cộng đồng`
   - community group cards are clickable
   - clicking a group actually navigates to/open the group chat room

4. `ChatScreen`
   - fallback save state is no longer presented as an error

5. `Social`
   - initial liked state now reflects server-truth in both feed and detail

---

## [OUTPUT REQUIREMENT]
Return a concise implementation report with exactly:

1. **Files changed**
2. **How Family was refactored to 3 tabs**
3. **How Inbox was isolated**
4. **How Community group entry was fixed**
5. **How chat fallback UX was corrected**
6. **How social liked-state truthfulness was fixed**
7. **Build result**
8. **Any remaining truthful gaps**

Do not mark this batch complete if:
- Inbox still mixes family/community concepts
- Family chat still lives primarily in Inbox
- Community group cards still no-op
- chat fallback success still looks like an error
- social liked state still defaults to false without server truth
