package os.rose.launcher.audio

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Optional UI feedback. Uses the platform's own generated tones, so Rose OS
 * bundles no audio assets at all and can never infringe on copyrighted sound
 * design. Off by default; released the moment it is disabled.
 */
class SoundFx {

    private var generator: ToneGenerator? = null
    private var enabled: Boolean = false

    fun setEnabled(value: Boolean) {
        if (enabled == value) return
        enabled = value
        if (!value) release()
    }

    private fun ensure(): ToneGenerator? {
        generator?.let { return it }
        return runCatching {
            ToneGenerator(AudioManager.STREAM_MUSIC, 28).also { generator = it }
        }.getOrNull()
    }

    fun move() {
        if (!enabled) return
        runCatching { ensure()?.startTone(ToneGenerator.TONE_PROP_BEEP2, 14) }
    }

    fun confirm() {
        if (!enabled) return
        runCatching { ensure()?.startTone(ToneGenerator.TONE_PROP_ACK, 26) }
    }

    fun release() {
        runCatching { generator?.release() }
        generator = null
    }
}
