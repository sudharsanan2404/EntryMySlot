package com.entrymyslot.app

import androidx.test.platform.app.InstrumentationRegistry
import com.entrymyslotbe.app.auth.AuthScope
import com.entrymyslotbe.app.auth.SessionStore
import com.entrymyslotbe.app.network.model.User
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Simulates delayed responses using local dummy tokens; no network requests are made. */
class SessionConcurrencyTest {
    private val app: EntryMySlotApp
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as EntryMySlotApp
    private val sessions: SessionStore get() = app.appContainer.backend.sessions

    private fun requireIsolatedPackage() {
        check(app.packageName.endsWith(".integration")) { "Session tests require the isolated integration application." }
    }

    @Before
    fun resetSession() {
        requireIsolatedPackage()
        sessions.clearAll()
    }

    @After
    fun removeDummySession() {
        requireIsolatedPackage()
        sessions.clearAll()
    }

    @Test
    fun delayedRefreshCannotRestoreLoggedOutSessionOrIdentity() {
        sessions.save(AuthScope.USER, "access-a", "refresh-a")
        sessions.saveUser(User(id = "user-a", name = "First user"))
        val pendingRefresh = sessions.snapshot(AuthScope.USER)

        sessions.clear(AuthScope.USER)

        assertFalse(sessions.refreshIfCurrent(pendingRefresh, "access-rotated", "refresh-rotated", User(id = "user-a")))
        assertNull(sessions.accessToken(AuthScope.USER))
        assertNull(sessions.refreshToken(AuthScope.USER))
        assertNull(sessions.user())
        assertFalse(AuthScope.USER in sessions.state.value.authenticatedScopes)
    }

    @Test
    fun delayedPreviousLoginCannotReplaceOrClearNewLogin() {
        sessions.save(AuthScope.USER, "access-a", "refresh-a")
        val oldLogin = sessions.snapshot(AuthScope.USER)

        sessions.save(AuthScope.USER, "access-b", "refresh-b")
        sessions.saveUser(User(id = "user-b", name = "Second user"))

        assertFalse(sessions.refreshIfCurrent(oldLogin, "access-old-response", "refresh-old-response", User(id = "user-a")))
        assertFalse(sessions.clearIfCurrent(oldLogin))
        assertEquals("access-b", sessions.accessToken(AuthScope.USER))
        assertEquals("refresh-b", sessions.refreshToken(AuthScope.USER))
        assertEquals("user-b", sessions.user()?.id)
    }

    @Test
    fun generationSeparatesLoginsEvenWhenTokenStringsAreReused() {
        sessions.save(AuthScope.USER, "same-access", "same-refresh")
        val oldLogin = sessions.snapshot(AuthScope.USER)

        sessions.clear(AuthScope.USER)
        sessions.save(AuthScope.USER, "same-access", "same-refresh")
        val newLogin = sessions.snapshot(AuthScope.USER)

        assertTrue(newLogin.generation > oldLogin.generation)
        assertFalse(sessions.refreshIfCurrent(oldLogin, "stale-access", "stale-refresh"))
        assertFalse(sessions.clearIfCurrent(oldLogin))
        assertEquals(newLogin, sessions.snapshot(AuthScope.USER))
    }

    @Test
    fun staleTokenFailureCannotClearAlreadyRotatedTokens() {
        sessions.save(AuthScope.USER, "access-a", "refresh-a")
        val original = sessions.snapshot(AuthScope.USER)

        assertTrue(sessions.refreshIfCurrent(original, "access-b", "refresh-b"))
        val rotated = sessions.snapshot(AuthScope.USER)

        assertEquals(original.generation, rotated.generation)
        assertFalse(sessions.clearIfCurrent(original))
        assertFalse(sessions.refreshIfCurrent(original, "competing-access", "competing-refresh"))
        assertEquals(rotated, sessions.snapshot(AuthScope.USER))
        assertTrue(sessions.clearIfCurrent(rotated))
        assertNull(sessions.accessToken(AuthScope.USER))
    }

    @Test
    fun refreshingInactiveUserScopePreservesActiveOrganizerScope() {
        sessions.save(AuthScope.USER, "user-access", "user-refresh")
        sessions.save(AuthScope.ORGANIZER, "organizer-access", "organizer-refresh")
        val userRequest = sessions.snapshot(AuthScope.USER)
        val organizer = sessions.snapshot(AuthScope.ORGANIZER)

        assertTrue(sessions.refreshIfCurrent(userRequest, "new-user-access", "new-user-refresh"))

        assertEquals(AuthScope.ORGANIZER, sessions.activeScope())
        assertEquals(organizer, sessions.snapshot(AuthScope.ORGANIZER))
        assertEquals("new-user-access", sessions.accessToken(AuthScope.USER))
        assertTrue(AuthScope.USER in sessions.state.value.authenticatedScopes)
        assertTrue(AuthScope.ORGANIZER in sessions.state.value.authenticatedScopes)
    }

    @Test
    fun clearAllInvalidatesPendingResponsesForEveryScope() {
        sessions.save(AuthScope.USER, "user-access", "user-refresh")
        sessions.save(AuthScope.ORGANIZER, "organizer-access", "organizer-refresh")
        val userRequest = sessions.snapshot(AuthScope.USER)
        val organizerRequest = sessions.snapshot(AuthScope.ORGANIZER)

        sessions.clearAll()

        assertFalse(sessions.refreshIfCurrent(userRequest, "stale-user-access", "stale-user-refresh"))
        assertFalse(sessions.refreshIfCurrent(organizerRequest, "stale-organizer-access", "stale-organizer-refresh"))
        assertTrue(sessions.state.value.authenticatedScopes.isEmpty())
    }

    @Test
    fun logoutCanClearRotatedTokensFromTheSameLogin() {
        sessions.save(AuthScope.USER, "access-a", "refresh-a")
        val logoutRequest = sessions.snapshot(AuthScope.USER)
        assertTrue(sessions.refreshIfCurrent(logoutRequest, "access-b", "refresh-b"))

        assertTrue(sessions.clearLoginIfCurrent(logoutRequest))

        assertNull(sessions.accessToken(AuthScope.USER))
        assertNull(sessions.refreshToken(AuthScope.USER))
    }

    @Test
    fun oldLogoutCannotClearANewerLogin() {
        sessions.save(AuthScope.USER, "access-a", "refresh-a")
        val logoutRequest = sessions.snapshot(AuthScope.USER)
        sessions.save(AuthScope.USER, "access-b", "refresh-b")

        assertFalse(sessions.clearLoginIfCurrent(logoutRequest))

        assertEquals("access-b", sessions.accessToken(AuthScope.USER))
        assertEquals("refresh-b", sessions.refreshToken(AuthScope.USER))
    }

    @Test
    fun delayedProfileCannotReplaceIdentityAfterAnAccountChange() {
        sessions.save(AuthScope.USER, "access-a", "refresh-a")
        val oldProfileRequest = sessions.snapshot(AuthScope.USER)
        sessions.save(AuthScope.USER, "access-b", "refresh-b")
        sessions.saveUser(User(id = "user-b"))

        assertFalse(sessions.saveUserIfCurrent(oldProfileRequest, User(id = "user-a")))
        assertEquals("user-b", sessions.user()?.id)
        sessions.clear(AuthScope.USER)
        assertFalse(sessions.saveUserIfCurrent(oldProfileRequest, User(id = "user-a")))
        assertNull(sessions.user())
    }

    @Test
    fun profileResponseFromSameLoginSurvivesTokenRotation() {
        sessions.save(AuthScope.USER, "access-a", "refresh-a")
        val profileRequest = sessions.snapshot(AuthScope.USER)
        assertTrue(sessions.refreshIfCurrent(profileRequest, "access-b", "refresh-b"))

        assertTrue(sessions.saveUserIfCurrent(profileRequest, User(id = "user-a", name = "Updated name")))

        assertEquals("Updated name", sessions.user()?.name)
        assertEquals("access-b", sessions.accessToken(AuthScope.USER))
    }
}
