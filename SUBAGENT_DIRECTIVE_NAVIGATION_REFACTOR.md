# [TECH LEAD DIRECTIVE: CORE NAVIGATION & INFORMATION ARCHITECTURE REFACTOR]

## [CONTEXT]
A critical UX/UI audit has revealed two architectural issues in the main user app:

1. **Bottom Nav Overload**
   - The app currently exposes 6 bottom tabs.
   - This creates cognitive overload and cramped UI.
   - We must reduce the main shell to **5 core pillars**.

2. **Information Architecture Conflict**
   - The current `Tin nhắn` area incorrectly mixes family management concepts with communication concepts.
   - The current `Tổ ấm` sub-tab in the inbox/chat hub creates a logical dead-end and conflicts with the separate `Gia đình` area.

This refactor must be implemented against the **actual current Kotlin codebase**, not against hypothetical file names.

## [MISSION]
Act as a Principal Android Architect.

You must:
- refactor the main user navigation down to **5 pillars**
- move the standalone `Thuốc` experience under the `Gia đình` area
- isolate `Tin nhắn` so that it contains communication channels only
- preserve existing working flows where possible
- avoid fake integrations
- avoid leaving dead routes, dead callbacks, or dangling navigation references

Do **not** redesign the whole app.
This is an **information architecture and navigation refactor**, not a visual reinvention.

---

## [STRICT EXECUTION PROTOCOL (STEP-BY-STEP)]

### Step 1: Enforce the 5-pillar bottom navigation

Modify the **real current shell**, primarily:
- `frontend/app/src/main/java/com/example/carenest/feature/main/presentation/MainScreen.kt`
- `frontend/app/src/main/java/com/example/carenest/core/presentation/navigation/Navigation.kt`
- `frontend/app/src/main/java/com/example/carenest/core/presentation/navigation/NavigationKeys.kt` if needed

#### Requirements
- Remove the standalone `Medicine` / `Thuốc` tab from the bottom navigation.
- Keep **exactly 5 tabs**, in this exact order:
  1. `Trang chủ`
  2. `Gia đình`
  3. `Cộng đồng`
  4. `Tin nhắn`
  5. `Tôi`

#### Hard constraints
- Do **not** break the existing route keys and navigation entries for:
  - `AddMedicine`
  - `MedicineSchedule`
  - `AddMedicineSchedule`
  - `OcrScanner`
- Those medicine flows must remain reachable after this refactor, but their entry point must move under the Family area.

---

### Step 2: Refactor the current Family flow into a Family hub

The current codebase does **not** use `FamilyScreen.kt` as the main family surface.
It currently uses:
- `frontend/app/src/main/java/com/example/carenest/feature/family/presentation/FamilyFlowScreen.kt`

Refactor the Family area so that the Family tab becomes a hub with top tabs:
- `Thành viên`
- `Tủ thuốc`

#### Requirements
- Preserve the current family/member management behavior already implemented through:
  - `FamilyFlowScreen.kt`
  - `FamilyPickerScreen.kt`
  - `FamilyManagementScreen.kt`
- Render the existing `MedicineScreen` content under the `Tủ thuốc` top tab.
- If a new shell screen is introduced, it must **wrap** the current family flow rather than blindly replacing its logic.
- Avoid creating duplicate family navigation hierarchies.

#### Hard constraints
- The member-management flow must remain functional.
- The medicine flows must remain reachable from the new embedded medicine tab.
- Do not regress any existing `FamilyViewModel`-driven behavior.

---

### Step 3: Purge Family logic from the current Inbox implementation

The current codebase does **not** use `InboxScreen.kt`.
It currently uses:
- `frontend/app/src/main/java/com/example/carenest/feature/main/presentation/ChatHubScreen.kt`

Refactor the current inbox/chat hub so that it contains **communication channels only**.

#### Requirements
- Remove the `Tổ ấm` tab and its empty-state messaging entirely.
- Replace the inbox top tabs with:
  - `AI Care`
  - `Bác sĩ`

#### Behavior rules
- `AI Care` must preserve the existing AI assistant experience.
- `Bác sĩ` must only be wired to **real** doctor/expert messaging if that flow truly exists in the current codebase.
- If real doctor direct messaging is **not** yet implemented, show a truthful placeholder/safe shell state.
- Do **not** fake a working doctor inbox.

---

### Step 4: Route and reference cleanup

Clean up all references made obsolete by the refactor.

#### Must remove or update
- standalone bottom-nav references to `MedicineScreen`
- family-chat-inside-inbox callbacks and dead hooks
- obsolete `onNavigateToChatRoom(...)` wiring if it only supported the removed family inbox pane
- dead empty states telling users to go to `Gia đình` from inside `Tin nhắn`
- any orphaned route references created by this refactor

#### Hard constraint
- Do **not** remove valid community chat routes or valid community room navigation.

---

### Step 5: UTF-8 cleanup in touched navigation files

While performing this refactor, clean mojibake in all touched files, especially:
- `MainScreen.kt`
- `ChatHubScreen.kt`
- any new or refactored Family hub screen

All Vietnamese UI strings in touched files must remain clean and human-readable.

---

### Step 6: Build verification

Before committing, verify the refactor compiles successfully:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

The batch is not acceptable without `BUILD SUCCESSFUL`.

---

### Step 7: Strict Git execution

After verification:

```powershell
git add .
git commit -m "refactor(navigation): enforce 5-tab bottom nav and merge Medicine into Family"
git commit -m "refactor(inbox): isolate Inbox for AI Care and doctor messaging only"
git push origin develop
```

If the exact work naturally lands better as one commit instead of two, explain why in the final report.
Do **not** create extra documentation/planning commits.

---

## [OUTPUT REQUIREMENT]
Return a concise implementation report containing:

1. **Files changed**
2. **How Family was restructured into a hub**
3. **How Inbox was isolated**
4. **What dead routes/references were removed**
5. **Build result**
6. **Any remaining truthful gaps** (especially if `Bác sĩ` is still a safe placeholder rather than real messaging)

Do **not** claim success if:
- the member flow is broken
- the medicine flow becomes unreachable
- inbox still contains family-management logic
- doctor messaging is fake but described as complete