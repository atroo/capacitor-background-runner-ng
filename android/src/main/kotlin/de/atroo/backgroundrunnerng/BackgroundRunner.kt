package de.atroo.backgroundrunnerng

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.getcapacitor.Bridge
import com.getcapacitor.PluginHandle
import de.atroo.backgroundrunnerng.runnerengine.JSRuntime
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.getcapacitor.JSObject as CapJSObject


class BackgroundRunner(val context: android.content.Context, val bridge: Bridge) {
    private val TAG = "BackgroundRunner"
    val config: RunnerConfig = RunnerConfig.fromJSON(bridge.config.getPluginConfiguration("BackgroundRunner").configJSON)
    private val handlerThread = HandlerThread("BackgroundRunner")
    private var runtime: JSRuntime
    val handler: Handler

    init {
        // Start our plugin execution threads and handlers
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        runtime = initContext(config, null)
        val currentThread = Thread.currentThread()
        Log.d(TAG, "Creating worker factory, thread: ${currentThread.name}")
//        workerFactory = RunnerWorkerFactory(bridge)
//        val config = Configuration.Builder()
//            .setWorkerFactory(workerFactory)
//            .build()
//
//        WorkManager.initialize(context, config)
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
            val result = JSONObject()

            this.handler.post {
                val args = this.runtime.cap2JSObject(dataArgs)
                Log.d(TAG, "execute...args: ${args.toString()}")
                for (element in dataArgs.keys()) {
                    Log.d(TAG, "element: ${element}")
                }

                this.runtime.dispatchEvent(config.event, args)
            }

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

        this.handler.post(Runnable {
            try {
                execute(config, inputArgs, callbackId)
            } catch (e: Exception) {
                err = e
                println("[${config.label}]: ${e.message}")
            }
            waitGroup.countDown()
        })

        waitGroup.await()

        err?.let { throw it }
    }

    private fun getRegisteredPlugins(): Map<String, PluginHandle> {
        val pluginIds = mutableListOf<String>()

        // Use reflection to access the private plugins field
        val bridgeClass = bridge.javaClass
        val pluginsField = bridgeClass.getDeclaredField("plugins")
        pluginsField.isAccessible = true

        // @Suppress("UNCHECKED_CAST")
        val plugins = pluginsField.get(bridge) as Map<String, PluginHandle>

        // TODO Filter on configured plugins
        return plugins
    }

    @Throws(Exception::class)
    private fun initContext(config: RunnerConfig, callbackId: String?): JSRuntime {
        //        val runner = runner ?: throw Exception("runner is not started")
        val contextName = callbackId?.let { "${config.label}-$it" } ?: config.label
        val srcFile = readAssetFile(context, "public/assets/", "${config.src}")

        val currentThread = Thread.currentThread()
        Log.d(TAG, "createContext, thread: ${currentThread.name}")

        val plugins = getRegisteredPlugins()
        for (plugin in plugins.values) {
            Log.d(TAG, "createContext...${plugin.id}")
        }

        var self = this

        this.handler.post(Runnable {
            val runtime = JSRuntime(context, contextName)
            // create runtime and messagehandler
            // create winow object for compat
            val globalObj = runtime.context.getGlobalObject();
            val win = runtime.context.createNewJSObject()
            globalObj.setProperty("window", win)
            // messagehandler injects itself into quickjs runtime
            val messageHandler = MessageHandler(bridge, runtime, this.handler)
            // inject native bridge and plugin definitions
            runtime.setupCapApi(plugins.values, true, true)
            // execute background.ts
            runtime.execute(code = srcFile)

            self.runtime = runtime
        })
        return runtime
    }

    private fun destroyContext(jsRuntime: JSRuntime) {
//        val runner = this.runner ?: throw Exception("runner is not started")

        destroyContext(jsRuntime)
    }
}
