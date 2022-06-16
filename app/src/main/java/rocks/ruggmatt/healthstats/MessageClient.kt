package rocks.ruggmatt.healthstats

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

class MessageClient {
    companion object {
        private const val STEP_COUNTER_UPDATER = "step_counter_updater"
        private const val STEP_COUNTER_UPDATER_PATH = "/step_counter_updater"
        val STEP_URI = Uri.parse("content://com.mobvoi.ticwear.steps")
        private const val TAG = "MessageClient"
        private var stepCounterUpdaterNodeId: String? = null

        private fun updateStepCounterUpdaterCapability(capabilityInfo: CapabilityInfo) {
            Log.i(TAG, "Looking for clients");
            stepCounterUpdaterNodeId = pickBestNodeId(capabilityInfo.nodes)
            if (stepCounterUpdaterNodeId === null) {
                Log.i(TAG, "No clients found")
            }
        }

        private fun setupAndSendMessage(context: Context, steps: ByteArray) {
            Thread {
                Log.i(TAG, "Looking for clients")
                val capabilityInfo: CapabilityInfo = Tasks.await(
                    Wearable.getCapabilityClient(context)
                        .getCapability(
                            STEP_COUNTER_UPDATER,
                            CapabilityClient.FILTER_REACHABLE
                        )
                )
                Log.i(TAG, "Lookup finished")
                // capabilityInfo has the reachable nodes with the transcription capability
                updateStepCounterUpdaterCapability(capabilityInfo)
                sendUpdate(context, steps)
            }.start()
        }

        fun printCurrentSteps(context: Context) {
            val cursor = context.contentResolver.query(STEP_URI, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToNext()) {
                        val steps = cursor.getInt(0);
                        Log.i(TAG, "User has taken $steps steps")
                        val stepsAsBytes = steps.toString().toByteArray()
                        setupAndSendMessage(context, stepsAsBytes)
                    }
                } finally {
                    cursor.close()
                }
            }
        }


        private fun sendUpdate(context: Context, steps: ByteArray) {
            // if step counter node found, send an update
            stepCounterUpdaterNodeId?.also { nodeId ->
                Wearable.getMessageClient(context).sendMessage(
                    nodeId,
                    STEP_COUNTER_UPDATER_PATH,
                    steps
                ).apply {
                    addOnSuccessListener {
                        Log.i(TAG, "Message sent")
                    }
                    addOnFailureListener {
                        Log.i(TAG, "Message could not be sent")
                    }
                }
            }
        }


        private fun pickBestNodeId(nodes: Set<Node>): String? {
            // Find a nearby node or pick one arbitrarily
            return nodes.firstOrNull { it.isNearby }?.id ?: nodes.firstOrNull()?.id
        }
    }
}