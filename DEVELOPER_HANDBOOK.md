# CareNest Developer Handbook

This handbook is the primary onboarding document for developers inheriting the CareNest Android codebase. It explains what the project does, why the current architecture looks the way it does, where key business logic lives, and which areas still require production hardening.

The intended reader is a new Android developer, backend integrator, QA engineer, or project team member who needs to become productive quickly.

---

## 1. Project Overview & Context

### What CareNest Is

CareNest is a family-centered health management application. Its core purpose is to help a household coordinate medical information, daily care tasks, reminders, and trusted health discussions in one place.

The current application covers these major product areas:

- **Family health tracking**
  - Families and members
  - Active family context
  - Active health profile context
  - Family member health records

- **Medical records and care workflows**
  - Medicine cabinet
  - Medicine schedules
  - Appointments
  - Vaccination tracking
  - OCR-assisted prescription entry
  - User medical profile details

- **Community and doctor interaction**
  - Community groups
  - Community wiki/articles
  - Doctor-created medical guidance content
  - Community group chat
  - Doctor role-based actions

- **Authentication and account management**
  - Onboarding
  - Login/register/forgot password
  - Token persistence
  - Current user profile
  - Role refresh

- **Doctor eKYC and administration**
  - Doctor verification submission
  - Admin approval/rejection/revocation
  - Dynamic role provisioning from `USER` to `DOCTOR`

- **Notifications and settings**
  - Notification center
  - Profile/settings area
  - Policy screen

### Migration History

CareNest was originally implemented as a React Native application. The legacy reference commit for the UI/UX migration is:

```text
c56a8b8ae3ad2477fd11273ffb5aabc3215f279b
```

The Android frontend has since been migrated into a fully native Kotlin application using Jetpack Compose.

This historical context matters because many UI and routing decisions are inherited from the React Native app:

- Some screens intentionally mirror legacy RN flows and wording.
- The bottom navigation, dashboard layout, onboarding flow, auth screens, and community/medical screens were restored with the RN app as the visual source of truth.
- Some API contracts were originally designed around RN Axios/fetch usage and were later normalized into Retrofit interfaces.
- Global state that used to live in RN Context/Redux-like patterns is now represented by `StateFlow`, ViewModels, and `SecureSessionManager`.

When future developers see code that seems unusually careful about naming, navigation keys, or role state, it is often because the Kotlin app is preserving parity with the legacy RN behavior while gradually improving type safety and native Android ergonomics.

---

## 2. Tech Stack & Core Libraries

### Language and Platform

- **Kotlin**
  - Primary language for the Android frontend.
  - JVM toolchain: Java 17.

- **Android Native**
  - Package: `com.example.carenest`
  - Minimum SDK: 24
  - Target SDK: 36
  - Compile SDK: 36

- **Jetpack Compose**
  - Declarative UI system for all migrated screens.
  - Material 3 component layer.
  - Compose previews/tooling enabled for debug builds.

### State and Asynchronous Work

- **Coroutines**
  - Network and storage work should run off the main thread.
  - ViewModels use `viewModelScope`.
  - IO work should use `Dispatchers.IO`.

- **StateFlow**
  - Replaces the legacy RN global Context/Redux-like state patterns.
  - ViewModels expose immutable `StateFlow` to UI.
  - Compose screens collect state with `collectAsState()`.

Example ViewModel pattern:

```kotlin
data class ExampleUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<Item> = emptyList()
)

class ExampleViewModel(
    private val repository: ExampleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExampleUiState())
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                withContext(Dispatchers.IO) { repository.loadItems() }
            }.onSuccess { items ->
                _uiState.update { it.copy(isLoading = false, items = items) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.localizedMessage)
                }
            }
        }
    }
}
```

### Networking

- **Retrofit**
  - Main REST client.
  - Created centrally through `core/data/network/RetrofitClient.kt`.

- **OkHttp**
  - HTTP transport.
  - Uses `AuthInterceptor` for token and family context headers.
  - Uses `TokenAuthenticator` for token refresh behavior.

- **Gson**
  - JSON converter for Retrofit.

- **Standard API envelope**
  - Kotlin frontend and Spring Boot backend use a shared response contract:

```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)
```

Every new Retrofit endpoint should return:

```kotlin
Response<ApiResponse<T>>
```

unless there is a documented exception.

### Secure Storage

- **EncryptedSharedPreferences**
  - Implemented in `core/data/storage/SecureSessionManager.kt`.
  - Stores access token, refresh token, active family/profile IDs, user role, and onboarding state.

Important: Do not introduce plain `SharedPreferences` or plaintext DataStore for auth/session values.

### Navigation

- **Navigation 3**
  - Uses `NavKey` classes/data objects in `NavigationKeys.kt`.
  - Routes are registered in `Navigation.kt`.
  - Screens receive callbacks like `onBack`, `onNavigateTo...`, and generally remain stateless with respect to routing.

### Realtime Chat

- **STOMP over WebSocket**
  - Library: `com.github.NaikSoftware:StompProtocolAndroid`
  - Community chat endpoint:

```text
WebSocket: /ws
Application prefix: /app
Community send: /app/group/{groupId}
Community topic: /topic/group/{groupId}
```

- REST fallback exists for community group chat so messages can still be saved when realtime is reconnecting.

### Hardware and Media

- **CameraX**
  - Intended for camera-related flows such as QR/OCR.

- **ML Kit Text Recognition**
  - Included for OCR productionization.
  - Current OCR flow is still partly simulated; see technical debt section.

- **Coil**
  - Image loading for Compose.

### Build and Verification Commands

Frontend:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat assembleDebug
```

Install debug APK:

```powershell
cd D:\DoAn_MB1\CareNest\frontend
.\gradlew.bat :app:installDebug
```

Backend compile:

```powershell
cd D:\DoAn_MB1\CareNest\backend
.\mvnw.cmd compile
```

---

## 3. Architectural Philosophy

### Feature-Based Modular Architecture

The Kotlin app is organized around feature ownership rather than technical layers alone.

Feature modules live under:

```text
frontend/app/src/main/java/com/example/carenest/feature/
```

Current feature areas include:

```text
appointment/
auth/
chat/
community/
dashboard/
ekyc/
family/
health/
main/
medical/
notifications/
onboarding/
profile/
```

This structure keeps related UI, data access, and business-specific models close together. A developer working on community chat should not need to hunt through unrelated global packages to understand that flow.

### Typical Feature Module Structure

Most feature modules follow this layout:

```text
feature/<name>/
├── data/
│   ├── remote/
│   └── repository/
├── domain/
│   └── model/
└── presentation/
    ├── *ViewModel.kt
    ├── *Screen.kt
    └── components/
```

Not every feature has all three layers yet. Small or transitional modules may currently have only `presentation/`, but new development should follow the full pattern when data or business logic is involved.

### `domain/`

The `domain/` layer contains models and business-specific types used by the feature.

Examples:

- `feature/community/domain/model/CommunityModels.kt`
- `feature/chat/domain/model/ChatMessage.kt`
- `feature/family/domain/model/FamilyModels.kt`
- `feature/medical/domain/model/...`

Guidelines:

- Keep domain models independent of Retrofit whenever possible.
- Prefer Kotlin types that match backend IDs accurately, especially `Long` for database IDs.
- Avoid leaking raw DTO quirks into UI components.

Example:

```kotlin
data class ChatMessage(
    val id: String,
    val text: String,
    val isMe: Boolean,
    val senderName: String,
    val senderId: Long? = null,
    val senderRole: String? = null,
    val replyPreview: String? = null,
    val timestamp: Long
)
```

### `data/`

The `data/` layer owns remote APIs, repositories, DTO mapping, and coordination between REST/WebSocket/media upload when needed.

Common responsibilities:

- Define Retrofit interfaces in `data/remote/`.
- Use `/api/v1/...` consistently for backend API endpoints.
- Return `Response<ApiResponse<T>>`.
- Unwrap and validate API responses in repositories.
- Map API responses into domain/UI models.
- Translate backend errors into useful exceptions.

Example Retrofit interface pattern:

```kotlin
interface ExampleApi {
    @GET("/api/v1/example/{id}")
    suspend fun getExample(
        @Path("id") id: Long
    ): Response<ApiResponse<ExampleResponse>>
}
```

Example repository pattern:

```kotlin
class ExampleRepository(
    private val api: ExampleApi
) {
    suspend fun load(id: Long): ExampleResponse {
        val response = api.getExample(id)
        if (!response.isSuccessful) {
            throw IllegalStateException(response.body()?.message ?: "Unable to load data")
        }
        return response.body()?.data ?: throw IllegalStateException("Missing response data")
    }
}
```

### `presentation/`

The `presentation/` layer owns ViewModels and Jetpack Compose screens.

Responsibilities:

- Hold screen state in ViewModels.
- Expose immutable `StateFlow` to UI.
- Trigger repository operations from ViewModels.
- Keep Compose screens declarative and mostly stateless.
- Receive navigation callbacks as parameters.
- Apply keyboard/insets handling for form/chat screens.

Screen pattern:

```kotlin
@Composable
fun ExampleScreen(
    viewModel: ExampleViewModel,
    onBack: () -> Unit,
    onOpenDetail: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Render state and call callbacks instead of owning navigation directly.
}
```

### The `core/` Module

The `core/` module contains cross-cutting application infrastructure.

Key directories:

```text
core/
├── data/
│   ├── network/
│   ├── result/
│   └── storage/
├── domain/
│   ├── error/
│   └── model/
└── presentation/
    ├── components/
    ├── navigation/
    └── theme/
```

Important files:

- `core/data/network/RetrofitClient.kt`
  - Creates Retrofit client.
  - Adds auth interceptor and token authenticator.

- `core/data/network/AuthInterceptor.kt`
  - Adds `Authorization: Bearer <token>`.
  - Adds `X-Family-Id` when active family context exists.

- `core/data/storage/SecureSessionManager.kt`
  - Secure source for auth/session/global context values.

- `core/presentation/navigation/NavigationKeys.kt`
  - Type-safe route declarations.

- `core/presentation/navigation/Navigation.kt`
  - Navigation graph and shared ViewModel scope.

- `core/presentation/theme/`
  - Color, typography, theme, and dimension tokens.

- `core/presentation/components/`
  - Reusable UI primitives such as icon wrappers and shared components.

### Dependency Creation

The app currently uses manual dependency creation in `CareNestApplication.kt`, not Hilt/Koin.

Example:

```kotlin
class CareNestApplication : Application() {
    lateinit var secureSessionManager: SecureSessionManager
    lateinit var authApi: AuthApi
    lateinit var familyRepository: FamilyRepository
    lateinit var chatRepository: ChatRepository

    override fun onCreate() {
        super.onCreate()
        secureSessionManager = SecureSessionManager(this)
        val retrofit = RetrofitClient.create(secureSessionManager)

        authApi = retrofit.create(AuthApi::class.java)
        familyApi = retrofit.create(FamilyApi::class.java)
        familyRepository = FamilyRepository(familyApi, secureSessionManager)
    }
}
```

Because dependencies are manually wired, any new ViewModel with constructor parameters must have a matching `ViewModelProvider.Factory`.

Common crash source:

```kotlin
val viewModel: SomeViewModel = viewModel()
```

This will crash if `SomeViewModel` has constructor dependencies and no factory is supplied.

Correct pattern:

```kotlin
val viewModel: SomeViewModel = viewModel(
    factory = SomeViewModelFactory(repository)
)
```

---

## 4. Core Business & Security Flows

### Authentication and Session

Authentication is managed through:

- `feature/auth/data/remote/AuthApi.kt`
- `feature/auth/presentation/AuthViewModel.kt`
- `core/data/storage/SecureSessionManager.kt`

On successful login/register, the app stores:

- Access token
- Refresh token
- Account-level user ID
- User email
- User display name
- User role
- Optional profile/family context when available

The secure session manager exposes both synchronous getters and `StateFlow`s:

```kotlin
val tokenFlow: StateFlow<String?>
val familyIdFlow: StateFlow<String?>
val activeProfileIdFlow: StateFlow<Long?>
val userRoleFlow: StateFlow<String?>
```

Use flows when UI must react to changes.

Use getters when building request headers or doing one-time reads.

### RBAC and eKYC

CareNest uses role-based access control with these roles:

```text
USER
DOCTOR
ADMIN
```

Role affects UI and actions in several areas:

- **Community Wiki**
  - Doctors/admins can create medical guidance articles.
  - Normal users can read, like, and comment.

- **Community Chat**
  - Doctors/admins can access moderation-style actions.
  - Normal users can participate but should not see privileged actions.

- **Admin Verification**
  - Admin users can approve, reject, and revoke doctor verification status.

### Doctor eKYC State Machine

The eKYC pipeline connects doctor verification to dynamic role changes.

Applicant flow:

```text
USER opens Doctor Verification screen
USER submits specialty, workplace, certificate/document URL
Backend creates PENDING doctor verification record
```

Admin flow:

```text
ADMIN opens Admin Verification screen
ADMIN approves/rejects/revokes doctor verification
Backend updates verification status and user role
Frontend updates local admin StateFlow immediately
```

Applicant role refresh:

```text
App foreground/resume
AuthViewModel calls GET /api/v1/auth/me
Backend returns latest role
SecureSessionManager persists role
userRoleFlow emits new role
Community/Chat UI unlocks doctor-specific actions
```

Important frontend pieces:

- `feature/ekyc/presentation/AdminVerificationViewModel.kt`
- `feature/ekyc/presentation/DoctorVerificationScreen.kt`
- `feature/auth/presentation/AuthViewModel.kt`
- `feature/main/presentation/MainScreen.kt`
- `core/data/storage/SecureSessionManager.kt`

### Global Family and Profile Context

CareNest is family-centric. Most medical data is scoped to either:

- an active family
- an active health profile/member
- or both

Global context is stored in `SecureSessionManager`:

```kotlin
FAMILY_ID_KEY
PROFILE_ID_KEY
ACTIVE_PROFILE_ID_KEY
USER_ID_KEY
USER_ROLE_KEY
```

The active context is shared across screens using:

- `SecureSessionManager`
- `DashboardViewModel`
- explicit navigation arguments where needed

Example flows:

```text
Dashboard member selected
-> activeProfileId is updated
-> Medicine/Vaccine/Appointment screens receive or read that profile ID
```

```text
Family selected
-> X-Family-Id is stored
-> AuthInterceptor attaches it to API calls
-> Backend returns family-scoped data
```

Important rule:

**Do not confuse account user ID with health profile ID.**

They are different concepts:

```text
userId       = login/account identity
profileId   = health profile/member identity
familyId    = household/group identity
```

This distinction is critical for:

- chat ownership
- family member display
- medical records
- eKYC role management

Community post ownership uses account-level `userId`, not health `profileId`.

### Community Chat Behavior

Community chat currently uses:

- REST history:

```text
GET /api/v1/communities/{id}/posts
```

- REST fallback sending:

```text
POST /api/v1/communities/{id}/posts
```

- STOMP realtime:

```text
CONNECT /ws
SUBSCRIBE /topic/group/{id}
SEND /app/group/{id}
```

The current implementation intentionally allows messages to be saved through REST if WebSocket realtime is reconnecting. This prevents the user from being blocked by socket instability.

Files:

- `feature/chat/data/remote/ChatWebSocketClient.kt`
- `feature/chat/data/repository/ChatRepository.kt`
- `feature/chat/presentation/ChatViewModel.kt`
- `feature/chat/presentation/ChatScreen.kt`
- backend `GroupChatStompController.java`
- backend `WebSocketConfig.java`
- backend `JwtChannelInterceptor.java`

### Secure Storage and Logout

`SecureSessionManager.clearAll()` clears sensitive session values but preserves onboarding completion.

This means:

- logout removes tokens and active context
- onboarding does not show again after logout
- user role/name/email flows are reset

---

## 5. Navigation & Routing Strategy

### Type-Safe Navigation Keys

Navigation keys live in:

```text
core/presentation/navigation/NavigationKeys.kt
```

They use `NavKey` and Kotlin serialization:

```kotlin
@Serializable data object Login : NavKey
@Serializable data object MainDashboard : NavKey
@Serializable data class UserMedical(val profileId: Long) : NavKey
@Serializable data class ChatRoom(val id: Long, val name: String) : NavKey
@Serializable data class FamilyChatRoom(val familyId: Long, val familyName: String) : NavKey
```

Use `Long` for backend database IDs. Avoid introducing `Int` IDs for server entities.

### Navigation Graph

The route graph lives in:

```text
core/presentation/navigation/Navigation.kt
```

This file:

- decides the start destination
- creates shared root-level ViewModels
- registers route entries
- passes dependencies and callbacks into screens

Start destination logic:

```text
if onboarding not completed -> Onboarding
else if access token exists -> MainDashboard
else -> Login
```

### Stateless Screen Navigation

Screens should not directly mutate the root back stack. Instead they receive navigation lambdas:

```kotlin
fun MainScreen(
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToMedicalRecord: (Long) -> Unit,
    onLogout: () -> Unit
)
```

This keeps UI testable and reduces hidden coupling.

### Main Shell and Full-Screen Routes

`MainScreen` owns the bottom navigation tabs:

```text
Trang chủ
Gia đình
Thuốc
Tin nhắn
Cộng đồng
Tôi
```

Any screen that should not show bottom navigation must be routed outside the tab shell.

Examples:

- Community chat room uses `ChatRoom`.
- Family chat room uses `FamilyChatRoom`.
- Medical details use route keys such as `UserMedical`, `VaccinationTracker`, `MedicalAppointment`.

Do not render full-screen chat or detail screens inside a bottom tab branch unless the design explicitly requires the bottom bar to remain visible.

### Route Callback Example

Correct pattern from a tab screen:

```kotlin
CommunityScreen(
    canCreateArticle = canAccessDoctorUi,
    onOpenGroup = { group ->
        onItemClick(ChatRoom(group.id, group.name))
    }
)
```

Then in `Navigation.kt`:

```kotlin
entry<ChatRoom> {
    val key = it as ChatRoom
    ChatScreen(
        groupId = key.id,
        groupName = key.name,
        onBack = { backStack.removeLastOrNull() }
    )
}
```

### Keyboard and Insets

Screens with text input must explicitly handle keyboard/insets.

For full-screen chat:

- Root should fill the screen.
- Composer/input should receive IME padding.
- Bottom navigation should not be present.

For forms:

- Use scrollable content.
- Apply `WindowInsets.ime` or equivalent where needed.

The app currently sets:

```xml
android:windowSoftInputMode="adjustResize"
```

for `MainActivity`.

---

## 6. Current Technical Debt & Next Steps

This section is intentionally direct. The app is functional and has undergone a large migration, but future teams should know what is complete, what is partially simulated, and what requires careful follow-up.

### Current Mocked or Partially Simulated Features

#### OCR Prescription Scanner

Current state:

- UI flow exists.
- Mock/simulated parsed medicine data may be used during development.
- The UX should clearly disclose when AI/OCR output is simulated.
- The full production CameraX + ML Kit pipeline is not yet fully hardened.

Next steps:

- Connect real image capture/gallery flow.
- Run ML Kit recognition over selected/captured images.
- Parse medication names, dosage, schedule, and notes reliably.
- Add review/edit step before persistence.
- Add test cases for noisy OCR input.

#### Family Chat

Current state:

- Family chat infrastructure exists in files such as:
  - `FamilyChatWebSocketClient.kt`
  - `FamilyChatRepository.kt`
  - `FamilyChatViewModel.kt`
  - `FamilyChatScreen.kt`
  - backend `FamilyChatStompController.java`
- Some flows have been stabilized but should still be treated as needing runtime QA.

Next steps:

- Verify backend contract for family chat history and STOMP broadcast.
- Confirm membership authorization.
- Test with two accounts on two devices/emulators.
- Ensure messages persist after restart.

#### Community Chat Realtime

Current state:

- REST history and REST fallback sending work.
- STOMP realtime exists and should continue to be hardened.
- The UI is designed to remain usable while realtime reconnects.

Next steps:

- Capture backend logs during STOMP connect/send.
- Verify `Authorization` native STOMP header reaches `JwtChannelInterceptor`.
- Confirm `/ws` handshake permit behavior in Spring Security.
- Test realtime on physical devices, not only emulator.

#### Growth Tracker

Current state:

- Legacy RN had growth-tracking functionality.
- Kotlin migration still needs a complete parity pass if this feature is in scope.

Next steps:

- Restore data/API layer.
- Add Compose screens.
- Wire into profile/health navigation.
- Validate charts, history, and growth metrics.

#### Support, Language, and Report Issue Settings

Current state:

- Some profile subentries may show safe "coming soon" behavior.

Next steps:

- Define product requirements.
- Add real support center routes.
- Add language selection if localization is required.
- Add report issue submission flow.

### Known Engineering Debt

#### Manual Dependency Injection

The current app wires dependencies manually in `CareNestApplication.kt` and passes them through factories.

This is workable for the current project size but has risks:

- Easy to forget a ViewModel factory.
- Harder to replace dependencies in tests.
- Larger `CareNestApplication` over time.

Recommended future improvement:

- Introduce Hilt or Koin once the feature migration stabilizes.
- Do not do this during active bug-fix stabilization unless the team can absorb a broad refactor.

#### Mixed Legacy and Native Patterns

Some modules still show traces of earlier migration stages:

- older packages outside `feature/`
- transitional ViewModels
- duplicate or legacy shell screens

Future cleanup should:

- keep feature-based modules as the source of truth
- remove unused old screens only after confirming no route references remain
- avoid large cleanup commits mixed with behavior fixes

#### Environment Configuration

`AppConfig.kt` currently controls backend URLs:

```kotlin
const val HOST_IP = "127.0.0.1"
const val BACKEND_URL = "http://$HOST_IP:8080"
const val WEBSOCKET_URL = "ws://$HOST_IP:8080/ws"
```

Physical devices cannot reach a developer machine through `127.0.0.1`. For USB/device testing, use the machine's LAN IP or a proper dev environment configuration strategy.

Recommended future improvement:

- Move host config to build variants or generated BuildConfig.
- Use separate debug profiles for emulator, USB physical device, and staging.

#### Logging of Sensitive Data

AI client logging currently uses BODY logging in `CareNestApplication.kt`.

Medical/AI payloads can contain sensitive information.

Recommended future improvement:

- Disable body logging by default.
- Gate verbose logging behind debug-only flags.
- Never log tokens or medical data in release builds.

### UTF-8 and Vietnamese Encoding Requirement

This project contains many Vietnamese user-facing strings. During migration, mojibake issues appeared multiple times, for example:

```text
ThÃ´ng tin
Gia Ä‘Ã¬nh
Cá»™ng Ä‘á»“ng
```

Strict rule:

**All source files containing Vietnamese strings must be saved as UTF-8.**

Recommended practices:

- Prefer Android string resources for user-facing text when possible.
- Keep source files UTF-8 encoded.
- Avoid copy/paste through tools that reinterpret encoding.
- During review, search for suspicious sequences:

```powershell
rg "Ã|Ä|Â|áº|á»|Æ" frontend/app/src/main/java
```

Note: Some search hits may be intentional replacement/sanitization code for backend legacy strings, so review context before deleting.

### Runtime QA Checklist for Future Changes

Build success is not enough. Use this checklist after meaningful frontend/backend changes:

```text
1. App launch
   - onboarding state
   - login state
   - logout/login as different user

2. Dashboard
   - family switcher
   - profile/member selector
   - medicine/vaccine/appointment shortcuts

3. Family
   - family list
   - family detail
   - member profile open/edit
   - active family/profile persistence

4. Medical
   - medicine cabinet load
   - add medicine
   - medicine schedule
   - appointment list
   - vaccination tracker
   - OCR flow

5. Community
   - wiki list
   - like/comment article
   - create article as DOCTOR/ADMIN
   - group list
   - open community chat room
   - send message
   - verify message appears after account switch

6. Profile/eKYC
   - profile load after account switch
   - update current user
   - doctor verification submit
   - admin approve/reject/revoke
   - role refresh unlocks UI
```

### Production Readiness Summary

The Kotlin Compose migration is now strong enough for serious internal QA and iterative stabilization. It is not yet a "zero-debt" production system.

Strong areas:

- Feature-based project structure
- Secure token/session storage
- Auth/eKYC role pipeline
- Main shell/dashboard/family/community/medical core flows
- REST API envelope normalization
- Type-safe navigation keys

Areas that need continued attention:

- Full OCR productionization
- Realtime WebSocket hardening on physical devices
- Growth Tracker parity
- Full settings/support/language flows
- Environment/build variant management
- Test coverage and UI automation
- Final UTF-8 sweep before release

---

## Appendix: Important Files

### App Entry

```text
frontend/app/src/main/java/com/example/carenest/MainActivity.kt
frontend/app/src/main/java/com/example/carenest/CareNestApplication.kt
frontend/app/src/main/java/com/example/carenest/AppConfig.kt
```

### Core Infrastructure

```text
core/data/network/RetrofitClient.kt
core/data/network/AuthInterceptor.kt
core/data/network/TokenAuthenticator.kt
core/data/storage/SecureSessionManager.kt
core/presentation/navigation/Navigation.kt
core/presentation/navigation/NavigationKeys.kt
core/presentation/theme/
```

### Authentication

```text
feature/auth/data/remote/AuthApi.kt
feature/auth/domain/model/AuthModels.kt
feature/auth/presentation/AuthViewModel.kt
feature/auth/presentation/LoginScreen.kt
feature/auth/presentation/RegisterScreen.kt
```

### Dashboard and Main Shell

```text
feature/main/presentation/MainScreen.kt
feature/main/presentation/HomeDashboardScreen.kt
feature/dashboard/presentation/DashboardViewModel.kt
```

### Family

```text
feature/family/data/remote/FamilyApi.kt
feature/family/data/repository/FamilyRepository.kt
feature/family/domain/model/FamilyModels.kt
feature/family/presentation/
```

### Medical and Health

```text
feature/medical/
feature/health/
feature/appointment/
feature/profile/presentation/UserMedicalScreen.kt
```

### Community and Chat

```text
feature/community/data/remote/CommunityApi.kt
feature/community/data/repository/CommunityRepository.kt
feature/community/presentation/CommunityScreen.kt
feature/community/presentation/CommunityWikiScreen.kt
feature/community/presentation/CommunityGroupsPane.kt
feature/chat/data/remote/ChatWebSocketClient.kt
feature/chat/data/repository/ChatRepository.kt
feature/chat/presentation/ChatScreen.kt
feature/chat/presentation/ChatViewModel.kt
```

### eKYC

```text
feature/ekyc/data/remote/EkycApi.kt
feature/ekyc/data/repository/EkycRepository.kt
feature/ekyc/presentation/DoctorVerificationScreen.kt
feature/ekyc/presentation/AdminVerificationScreen.kt
feature/ekyc/presentation/AdminVerificationViewModel.kt
```

---

## Final Notes for New Developers

When changing this codebase, optimize for correctness over speed:

- Keep feature boundaries clear.
- Do not mix account `userId`, health `profileId`, and `familyId`.
- Keep REST contracts wrapped in `ApiResponse<T>`.
- Keep ViewModels responsible for state, repositories responsible for data, and screens responsible for rendering.
- Treat physical-device runtime testing as mandatory for chat, keyboard, camera, and auth/session changes.
- Preserve the legacy RN user experience unless the team explicitly decides to redesign.

CareNest has already absorbed a large migration. The next phase should be disciplined stabilization, targeted productionization, and careful QA rather than broad rewrites.
