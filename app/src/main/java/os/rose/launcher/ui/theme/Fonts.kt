package os.rose.launcher.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import os.rose.launcher.R

/**
 * Fonts are bundled (OFL licensed) so the launcher looks identical on every
 * device and never depends on a network font download.
 */
object RoseFonts {
    val Inter = FontFamily(Font(R.font.inter))
    val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk))
    val Doto = FontFamily(Font(R.font.doto))
    val SerifItalic = FontFamily(Font(R.font.instrument_serif_italic, style = FontStyle.Italic))
}
