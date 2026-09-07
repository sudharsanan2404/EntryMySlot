package com.entrymyslot.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.entrymyslot.app.screens.auth.AuthScreenViewModel
import com.entrymyslotbe.app.auth.AuthScope
import com.entrymyslotbe.app.auth.SessionStore
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Run each method separately to verify a real process restart. Credentials are runtime arguments only. */
class FunctionalFixesTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app get() = instrumentation.targetContext.applicationContext as EntryMySlotApp
    private val backend get() = app.appContainer.backend
    private fun isolatedOnly() = check(app.packageName.endsWith(".integration"))
    private fun waitFor(text: String) = compose.waitUntil(45000) {
        compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    }

    @Test fun loginPersistsSessionAndManualLocation() = runBlocking<Unit> {
        isolatedOnly()
        val args = InstrumentationRegistry.getArguments()
        val email = requireNotNull(args.getString("testEmail"))
        val password = requireNotNull(args.getString("testPassword"))
        backend.sessions.clearAll()
        app.getSharedPreferences("entry_my_slot_preferences", 0).edit().clear().commit()
        val vm = AuthScreenViewModel(app)
        instrumentation.runOnMainSync {
            vm.login(email, password)
            vm.login(email, password) // The synchronous in-flight guard must prevent a duplicate.
        }
        val state = withTimeout(45000) { vm.uiState.first { !it.isLoading } }
        assertTrue(state.errorMessage ?: "Login was not confirmed", state.isLoggedIn)
        val reopened = SessionStore(app)
        assertTrue(!reopened.accessToken(AuthScope.USER).isNullOrBlank())
        assertTrue(!reopened.refreshToken(AuthScope.USER).isNullOrBlank())
        assertTrue(reopened.user()?.email == email)
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor("Search cities across India")
            compose.onAllNodesWithText("Chennai").onFirst().performClick()
            waitFor("Popular Events")
            assertEquals("Chennai", app.getSharedPreferences("entry_my_slot_preferences", 0).getString("selected_city", null))
            compose.onAllNodesWithText("No featured promotions available.").assertCountEquals(0)
        }
    }

    @Test fun restoredSessionOpensHomeAndAuthenticates() = runBlocking {
        isolatedOnly()
        assertTrue(AuthScope.USER in backend.sessions.state.value.authenticatedScopes)
        assertNotNull(backend.sessions.user())
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor("Popular Events")
            compose.onAllNodesWithText("Search cities across India").assertCountEquals(0)
        }
        val profile = backend.gateway.data { getMe() }
        assertTrue((profile as? ApiResult.Failure)?.userMessage ?: "Profile request failed", profile is ApiResult.Success && profile.value != null)
        val refreshed = backend.auth.refreshActiveSession()
        assertTrue((refreshed as? ApiResult.Failure)?.userMessage ?: "Refresh failed", refreshed is ApiResult.Success)
        val authenticated = backend.gateway.execute { getMyBookings() }
        assertTrue((authenticated as? ApiResult.Failure)?.userMessage ?: "Authenticated request failed", authenticated is ApiResult.Success)
    }

    @Test fun logoutClearsPersistedSession() = runBlocking {
        isolatedOnly()
        val result = backend.auth.logout()
        val reopened = SessionStore(app)
        assertTrue("Access token remains after logout", reopened.accessToken(AuthScope.USER) == null)
        assertTrue("Refresh token remains after logout", reopened.refreshToken(AuthScope.USER) == null)
        assertNull(reopened.user())
        assertTrue((result as? ApiResult.Failure)?.userMessage ?: "Logout failed", result is ApiResult.Success)
    }

    @Test fun localSessionIsClearedAfterLogoutFailure() {
        isolatedOnly()
        val reopened = SessionStore(app)
        assertTrue("Access token survived logout/restart", reopened.accessToken(AuthScope.USER) == null)
        assertTrue("Refresh token survived logout/restart", reopened.refreshToken(AuthScope.USER) == null)
        assertNull(reopened.user())
        assertFalse(AuthScope.USER in reopened.state.value.authenticatedScopes)
    }
}
