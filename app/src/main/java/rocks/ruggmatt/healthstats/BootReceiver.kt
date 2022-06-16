package rocks.ruggmatt.healthstats

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "BootReceiver";
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                MonitorStepsJob.setupJobScheduler(context);
            }
            else -> {
                Log.d(TAG, "Unknown intent ${intent.action}")
            }
        }
    }
}