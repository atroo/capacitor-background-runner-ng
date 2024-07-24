package de.atroo.backgroundrunnerng

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.getcapacitor.Bridge

class RunnerWorkerFactory(private val bridge: Bridge) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName) {
            RunnerWorker::class.java.name ->
                RunnerWorker(appContext, workerParameters, bridge)
            else ->
                null  // Let the default WorkerFactory handle other workers
        }
    }
}