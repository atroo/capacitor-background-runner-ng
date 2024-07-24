package de.atroo.backgroundrunnerng

import androidx.work.Data
import android.content.res.AssetManager
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.getcapacitor.Bridge
import com.getcapacitor.JSObject as CapJSObject
import de.atroo.backgroundrunnerng.runnerengine.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class BackgroundRunner(val context: android.content.Context, val bridge: Bridge) {
    val TAG = "BackgroundRunner"
    val config: RunnerConfig

    init {
        config = loadRunnerConfig(context.assets)
        val currentThread = Thread.currentThread()
        Log.d(TAG, "Creating worker factory, thread: ${currentThread.name}")
//        workerFactory = RunnerWorkerFactory(bridge)
//        val config = Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()
//
//        WorkManager.initialize(context, config)
    }

    fun createContext(androidContext: android.content.Context, name: String): CapacitorContext {
        val currentThread = Thread.currentThread()
        Log.d(TAG, "createContext, thread: ${currentThread.name}")
        Log.d(TAG, "createContext...${bridge == null}")
        Log.d(TAG, "createContext...${this}")

        val capContext = CapacitorContext(androidContext, name, bridge)
        return capContext
    }

    fun destroyContext(name: String) {
        Log.d(TAG, "unnecessary destroyContext: $name") ///???
    }

    fun start() {
    }

    fun shutdown() {
    }

    fun scheduleBackgroundTask(androidContext: android.content.Context) {
        Log.d(TAG, "scheduleBackgroundTask...")
        config ?: throw Exception("...no runner config to schedule")

        val interval = config.interval ?: throw Exception("cannot register background task without a configured interval")

        if (!config.autoSchedule) {
            return
        }

        val data = Data.Builder()
            .putString("label", config.label)
            .putString("src", config.src)
            .putString("event", config.event)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        if (!config.repeats) {
            val work = OneTimeWorkRequest.Builder(RunnerWorker::class.java)
                .setInitialDelay(interval.toLong(), TimeUnit.MINUTES)
                .setInputData(data)
                .addTag(config.label ?: "")
                .setConstraints(constraints)
                .build()
            Log.d(TAG, "Enqueueing task...")
            WorkManager.getInstance(androidContext).enqueueUniqueWork(config.label ?: "", ExistingWorkPolicy.REPLACE, work)
        } else {
            val work = PeriodicWorkRequest.Builder(RunnerWorker::class.java, interval.toLong(), TimeUnit.MINUTES)
                .setInitialDelay(interval.toLong(), TimeUnit.MINUTES)
                .setInputData(data)
                .addTag(config.label ?:"")
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(androidContext).enqueueUniquePeriodicWork(config.label ?: "", ExistingPeriodicWorkPolicy.UPDATE, work)
        }
    }

    @Throws(Exception::class)
    fun registerBackgroundTask() {
        val config = config ?: throw BackgroundRunnerPluginError.NoRunnerConfig

//        BGTaskScheduler.shared.register(config.label, null) { task ->
//            try {
//                task.expirationHandler = {
//                    println("task timed out")
//                }
//
//                BackgroundRunner.shared.execute(config)
//
//                task.setTaskCompleted(true)
//            } catch (e: Exception) {
//                println("background task error: ${e.message}")
//                task.setTaskCompleted(false)
//            }
//        }
    }

    @Throws(Exception::class)
    fun execute(config: RunnerConfig, dataArgs: CapJSObject = CapJSObject(), callbackId: String? = null): JSONObject {
        Log.d(TAG, "execute...dataArgs: ${dataArgs.toString()}")
        try {
            val context = initContext(config, callbackId)
            val result = JSONObject()
            val args = context.cap2JSObject(dataArgs)
            Log.d(TAG, "execute...args: ${args.toString()}")
            for (element in dataArgs.keys()) {
                Log.d(TAG, "element: ${element}")
            }

            context.dispatchEvent(config.event, args)

            return result
        } catch (e: Exception) {
            println("error executing task: ${e.message}")
            throw e
        }
    }

    @Throws(Exception::class)
    fun dispatchEvent(event: String, inputArgs: CapJSObject = CapJSObject(), callbackId: String? = null) {
        val config = config ?: throw Exception("No configuration set for BackgroundRunner")

        val waitGroup = CountDownLatch(1)
        var err: Exception? = null

        Thread {
            try {
                execute(config, inputArgs, callbackId)
            } catch (e: Exception) {
                err = e
                println("[${config.label}]: ${e.message}")
            }
            waitGroup.countDown()
        }.start()

        waitGroup.await()

        err?.let { throw it }
    }

    private fun loadRunnerConfig(assetManager: AssetManager): RunnerConfig {
        BufferedReader(InputStreamReader(assetManager.open("capacitor.config.json"))).use { reader ->
            val buffer = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                buffer.append(line).append("\n")
            }
             val fileContents = buffer.toString();

            val configObject = JSONObject(fileContents)
            val plugins = configObject.getJSONObject("plugins") ?: throw Exception("could not read config file")
            val runnerConfigObj = plugins.getJSONObject("BackgroundRunner") ?: throw Exception("could not read config file")

            return  RunnerConfig.fromJSON(runnerConfigObj)
        }
    }

    @Throws(Exception::class)
    private fun initContext(config: RunnerConfig, callbackId: String?): CapacitorContext {
//        val runner = runner ?: throw Exception("runner is not started")
        val contextName = callbackId?.let { "${config.label}-$it" } ?: config.label
        val srcFile = readAssetFile(context, "public/assets/", "${config.src}")
        val context = createContext(context, contextName)
        context.setupCapacitorAPI()
        context.execute(code = srcFile)

        return context
    }

    private fun destroyContext(context: Context) {
//        val runner = this.runner ?: throw Exception("runner is not started")

        destroyContext(context.name)
    }
}
