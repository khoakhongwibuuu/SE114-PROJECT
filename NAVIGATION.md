[TECH LEAD DIRECTIVE: NAVIGATION ROUTING & BACK-STACK REPAIR]

[CONTEXT]
We are refining the Jetpack Compose navigation architecture. An audit revealed two critical routing regressions compared to our legacy React Native app:

Aggressive Back-Stack Popping: Pressing the hardware back button or top app bar back arrow on certain deep screens incorrectly routes the user entirely back to the Dashboard/Home, rather than just popping the current screen off the stack.

Missing Double-Tap to Reload: The Bottom Navigation Bar lost the ability to refresh the current tab's content when tapping an already active tab.

You are operating as an Autonomous CI/CD Agent under strict GitHub workflow rules.

[MISSION]
Audit and repair the Navigation graph, fix onBack lambdas, implement the Bottom Nav double-tap refresh mechanism, and push the code directly to the repository.

[STRICT EXECUTION PROTOCOL (STEP-BY-STEP)]

Step 1: Back-Stack Audit & Fix (Navigation.kt & Screen Components)

Scan all navController.navigate() and onBack lambda calls.

Remove aggressive popUpTo blocks on standard deep links (e.g., from Family List to Family Details, or Medicine List to Add Medicine). Standard back navigation MUST only execute navController.popBackStack().

Ensure TopAppBar back arrows in ALL screens properly trigger the corrected onBack callback.

Step 2: Double-Tap to Refresh Implementation (MainScreen.kt & BottomNavigationBar.kt)

In the Bottom Navigation item onClick listener, detect if currentDestination?.hierarchy?.any { it.route == item.route } == true.

If false, navigate normally.

If true (user tapped the already active tab), fire a global refresh event (e.g., using a SharedFlow in a scoped ViewModel, or passing a onTabReselected callback down to the current screen's ViewModel to trigger data refetching).

Step 3: Strict GitHub Execution

You must execute the following commands exactly. Do not use generic commit messages.

Execute: git add .

Execute: git commit -m "fix(navigation): repair back-stack popping behavior and implement bottom nav double-tap refresh"

Execute: git push origin develop

[OUTPUT REQUIREMENT]
Acknowledge this directive. Execute all 3 steps autonomously. Report back ONLY when the push is successful, providing the exact Commit Hash and a brief, 3-bullet-point summary of the specific routes that were repaired.