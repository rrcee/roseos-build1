package os.rose.launcher.ui.components

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * A clock that wakes up exactly once a minute. No ticking seconds, no
 * permanent timer, no wake locks.
 */
@Composable
fun rememberNow(): State<Calendar> {
    val state = remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            state.value = now
            val remaining = (60 - now.get(Calendar.SECOND)) * 1000L - now.get(Calendar.MILLISECOND)
            delay(remaining.coerceAtLeast(250L))
        }
    }
    return state
}

@Composable
fun formatClock(now: Date): String {
    val context = LocalContext.current
    val is24 = remember { DateFormat.is24HourFormat(context) }
    val fmt = remember(is24) {
        SimpleDateFormat(if (is24) "HH:mm" else "h:mm", Locale.getDefault())
    }
    return remember(now, fmt) { fmt.format(now) }
}

@Composable
fun formatDate(now: Date): String {
    val fmt = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }
    return remember(now, fmt) { fmt.format(now) }
}
