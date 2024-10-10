package de.atroo.backgroundrunnerng;

import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;

import com.getcapacitor.Bridge;
import com.getcapacitor.Logger;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginResult;
import com.whl.quickjs.wrapper.JSCallFunction;
import com.whl.quickjs.wrapper.JSFunction;
import com.whl.quickjs.wrapper.JSObject;
import com.whl.quickjs.wrapper.QuickJSContext;

import org.apache.cordova.PluginManager;

import de.atroo.backgroundrunnerng.runnerengine.JSRuntime;

/**
 * MessageHandler handles messages from the WebView, dispatching them
 * to plugins.
 */
public class MessageHandler extends com.getcapacitor.MessageHandler {

    private Bridge bridge;
    private JSRuntime JSRuntime;
    private PluginManager cordovaPluginManager;
    private JSFunction caller;
    private Handler handler;

    public MessageHandler(Bridge bridge, JSRuntime JSRuntime, Handler handler) {
        // TODO: find a better way, i.e. empty constructor in Cap MessageHandler or Interface
        // super(bridge, new WebView(bridge.getContext()), null);
        super();

        this.bridge = bridge;
        this.JSRuntime = JSRuntime;
        this.handler = handler;
        // TODO: we need to figure out, how the plugin can access the cordovaPlugManager
        this.cordovaPluginManager = cordovaPluginManager;

        QuickJSContext context = JSRuntime.getContext();
        JSObject repository = context.createNewJSObject();

        repository.setProperty("postMessage", jsonString -> {
            postMessage((String) jsonString[0]);
            return null;
        });


        context.getGlobalObject().setProperty("androidBridge", repository);
        context.getGlobalObject().getJSObject("window").setProperty("androidBridge", repository);
        this.caller = caller;
    }

    /**
     * The main message handler that will be called from JavaScript
     * to send a message to the native bridge.
     * @param jsonStr
     */
    @Override
    public void postMessage(String jsonStr) {
        try {
            com.getcapacitor.JSObject postData = new com.getcapacitor.JSObject(jsonStr);

            String type = postData.getString("type");

            boolean typeIsNotNull = type != null;
            boolean isCordovaPlugin = typeIsNotNull && type.equals("cordova");
            boolean isJavaScriptError = typeIsNotNull && type.equals("js.error");

            String callbackId = postData.getString("callbackId");

            if (isCordovaPlugin) {
                String service = postData.getString("service");
                String action = postData.getString("action");
                String actionArgs = postData.getString("actionArgs");

                Logger.verbose(
                    Logger.tags("Plugin"),
                    "To native (Cordova plugin): callbackId: " +
                    callbackId +
                    ", service: " +
                    service +
                    ", action: " +
                    action +
                    ", actionArgs: " +
                    actionArgs
                );

                this.callCordovaPluginMethod(callbackId, service, action, actionArgs);
            } else if (isJavaScriptError) {
                Logger.error("JavaScript Error: " + jsonStr);
            } else {
                String pluginId = postData.getString("pluginId");
                String methodName = postData.getString("methodName");
                com.getcapacitor.JSObject methodData = postData.getJSObject("options", new com.getcapacitor.JSObject());

                Logger.verbose(
                    Logger.tags("Plugin"),
                    "To native (Capacitor plugin): callbackId: " + callbackId + ", pluginId: " + pluginId + ", methodName: " + methodName
                );

                this.callPluginMethod(callbackId, pluginId, methodName, methodData);
            }
        } catch (Exception ex) {
            Logger.error("Post message error:", ex);
        }
    }

    @Override
    public void sendResponseMessage(PluginCall call, PluginResult successResult, PluginResult errorResult) {
        this.handler.post(() -> {
            try {
                PluginResult data = new PluginResult();
                data.put("save", call.isKeptAlive());
                data.put("callbackId", call.getCallbackId());
                data.put("pluginId", call.getPluginId());
                data.put("methodName", call.getMethodName());

                boolean pluginResultInError = errorResult != null;
                if (pluginResultInError) {
                    data.put("success", false);
                    data.put("error", errorResult);
                    Logger.debug("Sending plugin error: " + data.toString());
                } else {
                    data.put("success", true);
                    if (successResult != null) {
                        data.put("data", successResult);
                    }
                }

                boolean isValidCallbackId = !call.getCallbackId().equals(PluginCall.CALLBACK_ID_DANGLING);
                if (isValidCallbackId) {
                    QuickJSContext context = JSRuntime.getContext();
                    JSFunction caller = context.getGlobalObject().getJSObject("window").getJSObject("androidBridge").getJSFunction("onmessage");
                    if(caller != null) {
                        Logger.verbose(
                                Logger.tags("Plugin"), data.toString() + " / " + this.JSRuntime.cap2JSObject(data.getWrappedResult()).toString());
                        String[] args = {data.toString()};
                        caller.call(args);
                        caller.release();
                        // caller.call(this.JSRuntime.cap2JSObject(data.getWrappedResult()).toString());
                    }

                } else {
                    // where does this come from?
                    // bridge.getApp().fireRestoredResult(data);
                }
            } catch (Exception ex) {
                Logger.error("sendResponseMessage error: " + ex);
            }
            if (!call.isKeptAlive()) {
                call.release(bridge);
            }
        });
    }

    private void callPluginMethod(String callbackId, String pluginId, String methodName, com.getcapacitor.JSObject methodData) {
        PluginCall call = new PluginCall(this, pluginId, callbackId, methodName, methodData);
        bridge.callPluginMethod(pluginId, methodName, call);
    }

    private void callCordovaPluginMethod(String callbackId, String service, String action, String actionArgs) {
        bridge.execute(
            () -> {
                cordovaPluginManager.exec(service, action, callbackId, actionArgs);
            }
        );
    }
}
