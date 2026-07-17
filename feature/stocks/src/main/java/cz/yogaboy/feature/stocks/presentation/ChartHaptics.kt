package cz.yogaboy.feature.stocks.presentation

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/** Hardware-backed chart haptics with the Compose action API as a compatibility fallback. */
internal class ChartHaptics(
    context: Context,
    private val fallback: HapticFeedback,
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun longPress() {
        perform(
            effect = VibrationEffect.EFFECT_CLICK,
            fallbackType = HapticFeedbackType.LongPress,
        )
    }

    fun tick() {
        perform(
            effect = VibrationEffect.EFFECT_TICK,
            fallbackType = HapticFeedbackType.SegmentTick,
        )
    }

    private fun perform(effect: Int, fallbackType: HapticFeedbackType) {
        if (!vibrator.hasVibrator()) {
            fallback.performHapticFeedback(fallbackType)
            return
        }

        val vibration = VibrationEffect.createPredefined(effect)
        val succeeded = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(
                    vibration,
                    VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH),
                )
            } else {
                vibrator.vibrate(vibration)
            }
        }.isSuccess

        if (!succeeded) fallback.performHapticFeedback(fallbackType)
    }
}
