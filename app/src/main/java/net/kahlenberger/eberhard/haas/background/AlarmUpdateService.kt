package net.kahlenberger.eberhard.haas.background

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.support.annotation.RequiresApi
import net.kahlenberger.eberhard.haas.helpers.IHandleSeenPackages
import net.kahlenberger.eberhard.haas.helpers.MaxJobIdIncrementProvider
import net.kahlenberger.eberhard.haas.helpers.PackageNameSplitterResolver

@RequiresApi(Build.VERSION_CODES.M)
class AlarmUpdateService : JobService() {
    private val jobIdProvider = MaxJobIdIncrementProvider()
    private var runningRequest:AsyncOpenHabRequest?  = null
    private val packageHandler: IHandleSeenPackages = PackageNameSplitterResolver()

    override fun onStartJob(param: JobParameters?): Boolean {
        val extras = param!!.extras
        val url = extras.getString("url")
        val username = extras.getString("username")
        val encryptedPassword = extras.getString("encryptedPassword")
        val item = extras.getString("item")
        val alarm = extras.getLong("defaultAlarm")
        runningRequest = AsyncOpenHabRequest(jobIdProvider,packageHandler)
        runningRequest!!.execute(OpenHabRequestData(url, username, encryptedPassword, item, alarm ,applicationContext,param,this))
        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        if (runningRequest == null) return true
        runningRequest!!.cancel(true)
        runningRequest = null
        return true
    }
}