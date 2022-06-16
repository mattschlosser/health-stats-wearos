package rocks.ruggmatt.healthstats

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.net.Uri

class MonitorStepsJob: JobService() {
    companion object {
        private const val JOB_ID = 1;
        fun setupJobScheduler(context: Context) {
            val jobScheduler = context.getSystemService(Activity.JOB_SCHEDULER_SERVICE) as JobScheduler;
            val URI = JobInfo.TriggerContentUri(MessageClient.STEP_URI, 0);
            val jobInfo = JobInfo.Builder(JOB_ID, ComponentName(context, MonitorStepsJob::class.java))
                .addTriggerContentUri(URI)
                .build()
            jobScheduler.schedule(jobInfo);
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        MessageClient.printCurrentSteps(this);
        setupJobScheduler(this);
        return false;
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false;
    }
}