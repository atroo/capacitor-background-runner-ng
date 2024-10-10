package de.atroo.backgroundrunnerng

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.getcapacitor.Bridge
import com.getcapacitor.Logger
import com.whl.quickjs.android.QuickJSLoader
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class RunnerWorker(private val context: Context,
                   workerParams: WorkerParameters,
                   private val bridge: Bridge
) : Worker(context, workerParams) {
    val TAG = "RunnerWorker"
    override fun doWork(): Result {
        Log.d(TAG, "doWork...")
        try {
            val label = this.inputData.getString("label") ?: ""
            val src = this.inputData.getString("src") ?: ""
            val event = this.inputData.getString("event") ?: ""

            if (label.isEmpty() || src.isEmpty() || event.isEmpty()) {
                throw Exception("label is empty")
            }

            val runnerConfigObj = JSONObject()
            runnerConfigObj.put("label", label)
            runnerConfigObj.put("src", src)
            runnerConfigObj.put("event", event)
            runnerConfigObj.put("autoStart", false)
            runnerConfigObj.put("repeats", false)
            runnerConfigObj.put("interval", 0)

            val config = RunnerConfig.fromJSON(runnerConfigObj)
            try {
                val impl = BackgroundRunner(this.context, this.bridge)
                impl.execute(config)
            } catch (ex: java.lang.Exception) {
                Logger.error("Serious error executing plugin", ex)
                throw RuntimeException(ex)
            }

            return Result.success()
        } catch (ex: Exception) {
            val label = this.inputData.getString("label") ?: ""
            Log.e("[RUNNER WORKER for $label]", ex.toString())
            ex.printStackTrace()
            val data = Data.Builder()
                .putString("error", ex.toString())
                .build()

            return Result.failure(data)
        }
    }
}