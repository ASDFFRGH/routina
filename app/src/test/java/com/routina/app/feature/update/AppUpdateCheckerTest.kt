package com.routina.app.feature.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppUpdateCheckerTest {
    @Test
    fun `finds a newer stable release APK`() {
        val update = AppUpdateChecker.parseAvailableUpdate(
            response = """
                {"tag_name":"v0.1.5","assets":[
                  {"name":"routina-v0.1.5.apk","browser_download_url":"https://github.com/ASDFFRGH/routina/releases/download/v0.1.5/routina-v0.1.5.apk"}
                ]}
            """.trimIndent(),
            currentVersion = "0.1.4",
        )

        assertEquals(AvailableUpdate("0.1.5", "https://github.com/ASDFFRGH/routina/releases/download/v0.1.5/routina-v0.1.5.apk"), update)
    }

    @Test
    fun `ignores non stable tags equal versions and untrusted assets`() {
        assertNull(AppUpdateChecker.parseAvailableUpdate("""{"tag_name":"v0.2.0-beta","assets":[]}""", "0.1.4"))
        assertNull(AppUpdateChecker.parseAvailableUpdate("""{"tag_name":"v0.1.4","assets":[]}""", "0.1.4"))
        assertNull(
            AppUpdateChecker.parseAvailableUpdate(
                """{"tag_name":"v0.2.0","assets":[{"name":"routina.apk","browser_download_url":"https://example.com/routina.apk"}]}""",
                "0.1.4",
            ),
        )
    }

    @Test
    fun `compares all numeric version components`() {
        assertEquals(StableVersion(1, 10, 0), StableVersion.parse("v1.10.0"))
        assertNull(StableVersion.parse("v1.2"))
        assertNull(StableVersion.parse("v1.2.3-rc1"))
    }
}
