package com.openfinds.app.core.ui.format

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.concurrent.TimeUnit

class TimeFormatTest {
    @Test
    fun `relativeTime buckets deltas into just-now, minutes, hours, days, weeks`() {
        val now = 1_000_000_000L
        assertThat(relativeTime(now - 5_000, now)).isEqualTo("just now")
        assertThat(relativeTime(now - TimeUnit.MINUTES.toMillis(5), now)).isEqualTo("5m ago")
        assertThat(relativeTime(now - TimeUnit.HOURS.toMillis(3), now)).isEqualTo("3h ago")
        assertThat(relativeTime(now - TimeUnit.DAYS.toMillis(2), now)).isEqualTo("2d ago")
        assertThat(relativeTime(now - TimeUnit.DAYS.toMillis(14), now)).isEqualTo("2w ago")
    }

    @Test
    fun `formatBytes scales to the right unit`() {
        assertThat(formatBytes(0)).isEqualTo("0 B")
        assertThat(formatBytes(512)).isEqualTo("512 B")
        assertThat(formatBytes(2048)).isEqualTo("2.0 KB")
        assertThat(formatBytes(5L * 1024 * 1024 * 1024)).isEqualTo("5.0 GB")
    }

    @Test
    fun `formatDuration renders days hours and minutes`() {
        val millis = TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(30)
        assertThat(formatDuration(millis)).isEqualTo("1d 2h 30m")
    }
}
