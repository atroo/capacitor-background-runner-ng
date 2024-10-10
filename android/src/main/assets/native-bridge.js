"use strict";
(() => {
  // node_modules/@capacitor/core/dist/index.js
  var createCapacitorPlatforms = (win) => {
    const defaultPlatformMap = /* @__PURE__ */ new Map();
    defaultPlatformMap.set("web", { name: "web" });
    const capPlatforms = win.CapacitorPlatforms || {
      currentPlatform: { name: "web" },
      platforms: defaultPlatformMap
    };
    const addPlatform2 = (name, platform) => {
      capPlatforms.platforms.set(name, platform);
    };
    const setPlatform2 = (name) => {
      if (capPlatforms.platforms.has(name)) {
        capPlatforms.currentPlatform = capPlatforms.platforms.get(name);
      }
    };
    capPlatforms.addPlatform = addPlatform2;
    capPlatforms.setPlatform = setPlatform2;
    return capPlatforms;
  };
  var initPlatforms = (win) => win.CapacitorPlatforms = createCapacitorPlatforms(win);
  var CapacitorPlatforms = /* @__PURE__ */ initPlatforms(typeof globalThis !== "undefined" ? globalThis : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : typeof global !== "undefined" ? global : {});
  var addPlatform = CapacitorPlatforms.addPlatform;
  var setPlatform = CapacitorPlatforms.setPlatform;
  var ExceptionCode;
  (function(ExceptionCode2) {
    ExceptionCode2["Unimplemented"] = "UNIMPLEMENTED";
    ExceptionCode2["Unavailable"] = "UNAVAILABLE";
  })(ExceptionCode || (ExceptionCode = {}));
  var CapacitorException = class extends Error {
    constructor(message, code, data) {
      super(message);
      this.message = message;
      this.code = code;
      this.data = data;
    }
  };
  var getPlatformId = (win) => {
    var _a, _b;
    if (win === null || win === void 0 ? void 0 : win.androidBridge) {
      return "android";
    } else if ((_b = (_a = win === null || win === void 0 ? void 0 : win.webkit) === null || _a === void 0 ? void 0 : _a.messageHandlers) === null || _b === void 0 ? void 0 : _b.bridge) {
      return "ios";
    } else {
      return "web";
    }
  };
  var createCapacitor = (win) => {
    var _a, _b, _c, _d, _e;
    const capCustomPlatform = win.CapacitorCustomPlatform || null;
    const cap = win.Capacitor || {};
    const Plugins2 = cap.Plugins = cap.Plugins || {};
    const capPlatforms = win.CapacitorPlatforms;
    const defaultGetPlatform = () => {
      return capCustomPlatform !== null ? capCustomPlatform.name : getPlatformId(win);
    };
    const getPlatform = ((_a = capPlatforms === null || capPlatforms === void 0 ? void 0 : capPlatforms.currentPlatform) === null || _a === void 0 ? void 0 : _a.getPlatform) || defaultGetPlatform;
    const defaultIsNativePlatform = () => getPlatform() !== "web";
    const isNativePlatform = ((_b = capPlatforms === null || capPlatforms === void 0 ? void 0 : capPlatforms.currentPlatform) === null || _b === void 0 ? void 0 : _b.isNativePlatform) || defaultIsNativePlatform;
    const defaultIsPluginAvailable = (pluginName) => {
      const plugin = registeredPlugins.get(pluginName);
      if (plugin === null || plugin === void 0 ? void 0 : plugin.platforms.has(getPlatform())) {
        return true;
      }
      if (getPluginHeader(pluginName)) {
        return true;
      }
      return false;
    };
    const isPluginAvailable = ((_c = capPlatforms === null || capPlatforms === void 0 ? void 0 : capPlatforms.currentPlatform) === null || _c === void 0 ? void 0 : _c.isPluginAvailable) || defaultIsPluginAvailable;
    const defaultGetPluginHeader = (pluginName) => {
      var _a2;
      return (_a2 = cap.PluginHeaders) === null || _a2 === void 0 ? void 0 : _a2.find((h) => h.name === pluginName);
    };
    const getPluginHeader = ((_d = capPlatforms === null || capPlatforms === void 0 ? void 0 : capPlatforms.currentPlatform) === null || _d === void 0 ? void 0 : _d.getPluginHeader) || defaultGetPluginHeader;
    const handleError = (err) => win.console.error(err);
    const pluginMethodNoop = (_target, prop, pluginName) => {
      return Promise.reject(`${pluginName} does not have an implementation of "${prop}".`);
    };
    const registeredPlugins = /* @__PURE__ */ new Map();
    const defaultRegisterPlugin = (pluginName, jsImplementations = {}) => {
      const registeredPlugin = registeredPlugins.get(pluginName);
      if (registeredPlugin) {
        console.warn(`Capacitor plugin "${pluginName}" already registered. Cannot register plugins twice.`);
        return registeredPlugin.proxy;
      }
      const platform = getPlatform();
      const pluginHeader = getPluginHeader(pluginName);
      let jsImplementation;
      const loadPluginImplementation = async () => {
        if (!jsImplementation && platform in jsImplementations) {
          jsImplementation = typeof jsImplementations[platform] === "function" ? jsImplementation = await jsImplementations[platform]() : jsImplementation = jsImplementations[platform];
        } else if (capCustomPlatform !== null && !jsImplementation && "web" in jsImplementations) {
          jsImplementation = typeof jsImplementations["web"] === "function" ? jsImplementation = await jsImplementations["web"]() : jsImplementation = jsImplementations["web"];
        }
        return jsImplementation;
      };
      const createPluginMethod = (impl, prop) => {
        var _a2, _b2;
        if (pluginHeader) {
          const methodHeader = pluginHeader === null || pluginHeader === void 0 ? void 0 : pluginHeader.methods.find((m) => prop === m.name);
          if (methodHeader) {
            if (methodHeader.rtype === "promise") {
              return (options) => cap.nativePromise(pluginName, prop.toString(), options);
            } else {
              return (options, callback) => cap.nativeCallback(pluginName, prop.toString(), options, callback);
            }
          } else if (impl) {
            return (_a2 = impl[prop]) === null || _a2 === void 0 ? void 0 : _a2.bind(impl);
          }
        } else if (impl) {
          return (_b2 = impl[prop]) === null || _b2 === void 0 ? void 0 : _b2.bind(impl);
        } else {
          throw new CapacitorException(`"${pluginName}" plugin is not implemented on ${platform}`, ExceptionCode.Unimplemented);
        }
      };
      const createPluginMethodWrapper = (prop) => {
        let remove;
        const wrapper = (...args) => {
          const p = loadPluginImplementation().then((impl) => {
            const fn = createPluginMethod(impl, prop);
            if (fn) {
              const p2 = fn(...args);
              remove = p2 === null || p2 === void 0 ? void 0 : p2.remove;
              return p2;
            } else {
              throw new CapacitorException(`"${pluginName}.${prop}()" is not implemented on ${platform}`, ExceptionCode.Unimplemented);
            }
          });
          if (prop === "addListener") {
            p.remove = async () => remove();
          }
          return p;
        };
        wrapper.toString = () => `${prop.toString()}() { [capacitor code] }`;
        Object.defineProperty(wrapper, "name", {
          value: prop,
          writable: false,
          configurable: false
        });
        return wrapper;
      };
      const addListener = createPluginMethodWrapper("addListener");
      const removeListener = createPluginMethodWrapper("removeListener");
      const addListenerNative = (eventName, callback) => {
        const call = addListener({ eventName }, callback);
        const remove = async () => {
          const callbackId = await call;
          removeListener({
            eventName,
            callbackId
          }, callback);
        };
        const p = new Promise((resolve) => call.then(() => resolve({ remove })));
        p.remove = async () => {
          console.warn(`Using addListener() without 'await' is deprecated.`);
          await remove();
        };
        return p;
      };
      const proxy = new Proxy({}, {
        get(_, prop) {
          switch (prop) {
            // https://github.com/facebook/react/issues/20030
            case "$$typeof":
              return void 0;
            case "toJSON":
              return () => ({});
            case "addListener":
              return pluginHeader ? addListenerNative : addListener;
            case "removeListener":
              return removeListener;
            default:
              return createPluginMethodWrapper(prop);
          }
        }
      });
      Plugins2[pluginName] = proxy;
      registeredPlugins.set(pluginName, {
        name: pluginName,
        proxy,
        platforms: /* @__PURE__ */ new Set([
          ...Object.keys(jsImplementations),
          ...pluginHeader ? [platform] : []
        ])
      });
      return proxy;
    };
    const registerPlugin2 = ((_e = capPlatforms === null || capPlatforms === void 0 ? void 0 : capPlatforms.currentPlatform) === null || _e === void 0 ? void 0 : _e.registerPlugin) || defaultRegisterPlugin;
    if (!cap.convertFileSrc) {
      cap.convertFileSrc = (filePath) => filePath;
    }
    cap.getPlatform = getPlatform;
    cap.handleError = handleError;
    cap.isNativePlatform = isNativePlatform;
    cap.isPluginAvailable = isPluginAvailable;
    cap.pluginMethodNoop = pluginMethodNoop;
    cap.registerPlugin = registerPlugin2;
    cap.Exception = CapacitorException;
    cap.DEBUG = !!cap.DEBUG;
    cap.isLoggingEnabled = !!cap.isLoggingEnabled;
    cap.platform = cap.getPlatform();
    cap.isNative = cap.isNativePlatform();
    return cap;
  };
  var initCapacitorGlobal = (win) => win.Capacitor = createCapacitor(win);
  var Capacitor = /* @__PURE__ */ initCapacitorGlobal(typeof globalThis !== "undefined" ? globalThis : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : typeof global !== "undefined" ? global : {});
  var registerPlugin = Capacitor.registerPlugin;
  var Plugins = Capacitor.Plugins;
  var WebPlugin = class {
    constructor(config) {
      this.listeners = {};
      this.retainedEventArguments = {};
      this.windowListeners = {};
      if (config) {
        console.warn(`Capacitor WebPlugin "${config.name}" config object was deprecated in v3 and will be removed in v4.`);
        this.config = config;
      }
    }
    addListener(eventName, listenerFunc) {
      let firstListener = false;
      const listeners = this.listeners[eventName];
      if (!listeners) {
        this.listeners[eventName] = [];
        firstListener = true;
      }
      this.listeners[eventName].push(listenerFunc);
      const windowListener = this.windowListeners[eventName];
      if (windowListener && !windowListener.registered) {
        this.addWindowListener(windowListener);
      }
      if (firstListener) {
        this.sendRetainedArgumentsForEvent(eventName);
      }
      const remove = async () => this.removeListener(eventName, listenerFunc);
      const p = Promise.resolve({ remove });
      return p;
    }
    async removeAllListeners() {
      this.listeners = {};
      for (const listener in this.windowListeners) {
        this.removeWindowListener(this.windowListeners[listener]);
      }
      this.windowListeners = {};
    }
    notifyListeners(eventName, data, retainUntilConsumed) {
      const listeners = this.listeners[eventName];
      if (!listeners) {
        if (retainUntilConsumed) {
          let args = this.retainedEventArguments[eventName];
          if (!args) {
            args = [];
          }
          args.push(data);
          this.retainedEventArguments[eventName] = args;
        }
        return;
      }
      listeners.forEach((listener) => listener(data));
    }
    hasListeners(eventName) {
      return !!this.listeners[eventName].length;
    }
    registerWindowListener(windowEventName, pluginEventName) {
      this.windowListeners[pluginEventName] = {
        registered: false,
        windowEventName,
        pluginEventName,
        handler: (event) => {
          this.notifyListeners(pluginEventName, event);
        }
      };
    }
    unimplemented(msg = "not implemented") {
      return new Capacitor.Exception(msg, ExceptionCode.Unimplemented);
    }
    unavailable(msg = "not available") {
      return new Capacitor.Exception(msg, ExceptionCode.Unavailable);
    }
    async removeListener(eventName, listenerFunc) {
      const listeners = this.listeners[eventName];
      if (!listeners) {
        return;
      }
      const index = listeners.indexOf(listenerFunc);
      this.listeners[eventName].splice(index, 1);
      if (!this.listeners[eventName].length) {
        this.removeWindowListener(this.windowListeners[eventName]);
      }
    }
    addWindowListener(handle) {
      window.addEventListener(handle.windowEventName, handle.handler);
      handle.registered = true;
    }
    removeWindowListener(handle) {
      if (!handle) {
        return;
      }
      window.removeEventListener(handle.windowEventName, handle.handler);
      handle.registered = false;
    }
    sendRetainedArgumentsForEvent(eventName) {
      const args = this.retainedEventArguments[eventName];
      if (!args) {
        return;
      }
      delete this.retainedEventArguments[eventName];
      args.forEach((arg) => {
        this.notifyListeners(eventName, arg);
      });
    }
  };
  var encode = (str) => encodeURIComponent(str).replace(/%(2[346B]|5E|60|7C)/g, decodeURIComponent).replace(/[()]/g, escape);
  var decode = (str) => str.replace(/(%[\dA-F]{2})+/gi, decodeURIComponent);
  var CapacitorCookiesPluginWeb = class extends WebPlugin {
    async getCookies() {
      const cookies = document.cookie;
      const cookieMap = {};
      cookies.split(";").forEach((cookie) => {
        if (cookie.length <= 0)
          return;
        let [key, value] = cookie.replace(/=/, "CAP_COOKIE").split("CAP_COOKIE");
        key = decode(key).trim();
        value = decode(value).trim();
        cookieMap[key] = value;
      });
      return cookieMap;
    }
    async setCookie(options) {
      try {
        const encodedKey = encode(options.key);
        const encodedValue = encode(options.value);
        const expires = `; expires=${(options.expires || "").replace("expires=", "")}`;
        const path = (options.path || "/").replace("path=", "");
        const domain = options.url != null && options.url.length > 0 ? `domain=${options.url}` : "";
        document.cookie = `${encodedKey}=${encodedValue || ""}${expires}; path=${path}; ${domain};`;
      } catch (error) {
        return Promise.reject(error);
      }
    }
    async deleteCookie(options) {
      try {
        document.cookie = `${options.key}=; Max-Age=0`;
      } catch (error) {
        return Promise.reject(error);
      }
    }
    async clearCookies() {
      try {
        const cookies = document.cookie.split(";") || [];
        for (const cookie of cookies) {
          document.cookie = cookie.replace(/^ +/, "").replace(/=.*/, `=;expires=${(/* @__PURE__ */ new Date()).toUTCString()};path=/`);
        }
      } catch (error) {
        return Promise.reject(error);
      }
    }
    async clearAllCookies() {
      try {
        await this.clearCookies();
      } catch (error) {
        return Promise.reject(error);
      }
    }
  };
  var CapacitorCookies = registerPlugin("CapacitorCookies", {
    web: () => new CapacitorCookiesPluginWeb()
  });
  var readBlobAsBase64 = async (blob) => new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const base64String = reader.result;
      resolve(base64String.indexOf(",") >= 0 ? base64String.split(",")[1] : base64String);
    };
    reader.onerror = (error) => reject(error);
    reader.readAsDataURL(blob);
  });
  var normalizeHttpHeaders = (headers = {}) => {
    const originalKeys = Object.keys(headers);
    const loweredKeys = Object.keys(headers).map((k) => k.toLocaleLowerCase());
    const normalized = loweredKeys.reduce((acc, key, index) => {
      acc[key] = headers[originalKeys[index]];
      return acc;
    }, {});
    return normalized;
  };
  var buildUrlParams = (params, shouldEncode = true) => {
    if (!params)
      return null;
    const output = Object.entries(params).reduce((accumulator, entry) => {
      const [key, value] = entry;
      let encodedValue;
      let item;
      if (Array.isArray(value)) {
        item = "";
        value.forEach((str) => {
          encodedValue = shouldEncode ? encodeURIComponent(str) : str;
          item += `${key}=${encodedValue}&`;
        });
        item.slice(0, -1);
      } else {
        encodedValue = shouldEncode ? encodeURIComponent(value) : value;
        item = `${key}=${encodedValue}`;
      }
      return `${accumulator}&${item}`;
    }, "");
    return output.substr(1);
  };
  var buildRequestInit = (options, extra = {}) => {
    const output = Object.assign({ method: options.method || "GET", headers: options.headers }, extra);
    const headers = normalizeHttpHeaders(options.headers);
    const type = headers["content-type"] || "";
    if (typeof options.data === "string") {
      output.body = options.data;
    } else if (type.includes("application/x-www-form-urlencoded")) {
      const params = new URLSearchParams();
      for (const [key, value] of Object.entries(options.data || {})) {
        params.set(key, value);
      }
      output.body = params.toString();
    } else if (type.includes("multipart/form-data") || options.data instanceof FormData) {
      const form = new FormData();
      if (options.data instanceof FormData) {
        options.data.forEach((value, key) => {
          form.append(key, value);
        });
      } else {
        for (const key of Object.keys(options.data)) {
          form.append(key, options.data[key]);
        }
      }
      output.body = form;
      const headers2 = new Headers(output.headers);
      headers2.delete("content-type");
      output.headers = headers2;
    } else if (type.includes("application/json") || typeof options.data === "object") {
      output.body = JSON.stringify(options.data);
    }
    return output;
  };
  var CapacitorHttpPluginWeb = class extends WebPlugin {
    /**
     * Perform an Http request given a set of options
     * @param options Options to build the HTTP request
     */
    async request(options) {
      const requestInit = buildRequestInit(options, options.webFetchExtra);
      const urlParams = buildUrlParams(options.params, options.shouldEncodeUrlParams);
      const url = urlParams ? `${options.url}?${urlParams}` : options.url;
      const response = await fetch(url, requestInit);
      const contentType = response.headers.get("content-type") || "";
      let { responseType = "text" } = response.ok ? options : {};
      if (contentType.includes("application/json")) {
        responseType = "json";
      }
      let data;
      let blob;
      switch (responseType) {
        case "arraybuffer":
        case "blob":
          blob = await response.blob();
          data = await readBlobAsBase64(blob);
          break;
        case "json":
          data = await response.json();
          break;
        case "document":
        case "text":
        default:
          data = await response.text();
      }
      const headers = {};
      response.headers.forEach((value, key) => {
        headers[key] = value;
      });
      return {
        data,
        headers,
        status: response.status,
        url: response.url
      };
    }
    /**
     * Perform an Http GET request given a set of options
     * @param options Options to build the HTTP request
     */
    async get(options) {
      return this.request(Object.assign(Object.assign({}, options), { method: "GET" }));
    }
    /**
     * Perform an Http POST request given a set of options
     * @param options Options to build the HTTP request
     */
    async post(options) {
      return this.request(Object.assign(Object.assign({}, options), { method: "POST" }));
    }
    /**
     * Perform an Http PUT request given a set of options
     * @param options Options to build the HTTP request
     */
    async put(options) {
      return this.request(Object.assign(Object.assign({}, options), { method: "PUT" }));
    }
    /**
     * Perform an Http PATCH request given a set of options
     * @param options Options to build the HTTP request
     */
    async patch(options) {
      return this.request(Object.assign(Object.assign({}, options), { method: "PATCH" }));
    }
    /**
     * Perform an Http DELETE request given a set of options
     * @param options Options to build the HTTP request
     */
    async delete(options) {
      return this.request(Object.assign(Object.assign({}, options), { method: "DELETE" }));
    }
  };
  var CapacitorHttp = registerPlugin("CapacitorHttp", {
    web: () => new CapacitorHttpPluginWeb()
  });

  // native-bridge.ts
  var dummy = {};
  var readFileAsBase64 = (file) => new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onloadend = () => {
      const data = reader.result;
      resolve(btoa(data));
    };
    reader.onerror = reject;
    reader.readAsBinaryString(file);
  });
  var convertFormData = async (formData) => {
    const newFormData = [];
    for (const pair of formData.entries()) {
      const [key, value] = pair;
      if (value instanceof File) {
        const base64File = await readFileAsBase64(value);
        newFormData.push({
          key,
          value: base64File,
          type: "base64File",
          contentType: value.type,
          fileName: value.name
        });
      } else {
        newFormData.push({ key, value, type: "string" });
      }
    }
    return newFormData;
  };
  var convertBody = async (body, contentType) => {
    if (body instanceof ReadableStream || body instanceof Uint8Array) {
      let encodedData;
      if (body instanceof ReadableStream) {
        const reader = body.getReader();
        const chunks = [];
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          chunks.push(value);
        }
        const concatenated = new Uint8Array(
          chunks.reduce((acc, chunk) => acc + chunk.length, 0)
        );
        let position = 0;
        for (const chunk of chunks) {
          concatenated.set(chunk, position);
          position += chunk.length;
        }
        encodedData = concatenated;
      } else {
        encodedData = body;
      }
      let data = new TextDecoder().decode(encodedData);
      let type;
      if (contentType === "application/json") {
        try {
          data = JSON.parse(data);
        } catch (ignored) {
        }
        type = "json";
      } else if (contentType === "multipart/form-data") {
        type = "formData";
      } else if (contentType?.startsWith("image")) {
        type = "image";
      } else if (contentType === "application/octet-stream") {
        type = "binary";
      } else {
        type = "text";
      }
      return {
        data,
        type,
        headers: { "Content-Type": contentType || "application/octet-stream" }
      };
    } else if (body instanceof URLSearchParams) {
      return {
        data: body.toString(),
        type: "text"
      };
    } else if (body instanceof FormData) {
      const formData = await convertFormData(body);
      return {
        data: formData,
        type: "formData"
      };
    } else if (body instanceof File) {
      const fileData = await readFileAsBase64(body);
      return {
        data: fileData,
        type: "file",
        headers: { "Content-Type": body.type }
      };
    }
    return { data: body, type: "json" };
  };
  var CAPACITOR_HTTP_INTERCEPTOR = "/_capacitor_http_interceptor_";
  var CAPACITOR_HTTP_INTERCEPTOR_URL_PARAM = "u";
  var isRelativeOrProxyUrl = (url) => !url || !(url.startsWith("http:") || url.startsWith("https:")) || url.indexOf(CAPACITOR_HTTP_INTERCEPTOR) > -1;
  var createProxyUrl = (url, win) => {
    if (isRelativeOrProxyUrl(url)) return url;
    const bridgeUrl = new URL(win.Capacitor?.getServerUrl() ?? "");
    bridgeUrl.pathname = CAPACITOR_HTTP_INTERCEPTOR;
    bridgeUrl.searchParams.append(CAPACITOR_HTTP_INTERCEPTOR_URL_PARAM, url);
    return bridgeUrl.toString();
  };
  var initBridge = (w) => {
    const getPlatformId2 = (win) => {
      if (win?.androidBridge) {
        return "android";
      } else if (win?.webkit?.messageHandlers?.bridge) {
        return "ios";
      } else {
        return "web";
      }
    };
    const convertFileSrcServerUrl = (webviewServerUrl, filePath) => {
      if (typeof filePath === "string") {
        if (filePath.startsWith("/")) {
          return webviewServerUrl + "/_capacitor_file_" + filePath;
        } else if (filePath.startsWith("file://")) {
          return webviewServerUrl + filePath.replace("file://", "/_capacitor_file_");
        } else if (filePath.startsWith("content://")) {
          return webviewServerUrl + filePath.replace("content:/", "/_capacitor_content_");
        }
      }
      return filePath;
    };
    const initEvents = (win, cap) => {
      cap.addListener = (pluginName, eventName, callback) => {
        const callbackId = cap.nativeCallback(
          pluginName,
          "addListener",
          {
            eventName
          },
          callback
        );
        return {
          remove: async () => {
            win?.console?.debug("Removing listener", pluginName, eventName);
            cap.removeListener?.(pluginName, callbackId, eventName, callback);
          }
        };
      };
      cap.removeListener = (pluginName, callbackId, eventName, callback) => {
        cap.nativeCallback(
          pluginName,
          "removeListener",
          {
            callbackId,
            eventName
          },
          callback
        );
      };
      cap.createEvent = (eventName, eventData) => {
        const doc = win.document;
        if (doc) {
          const ev = doc.createEvent("Events");
          ev.initEvent(eventName, false, false);
          if (eventData && typeof eventData === "object") {
            for (const i in eventData) {
              if (eventData.hasOwnProperty(i)) {
                ev[i] = eventData[i];
              }
            }
          }
          return ev;
        }
        return null;
      };
      cap.triggerEvent = (eventName, target, eventData) => {
        const doc = win.document;
        const cordova = win.cordova;
        eventData = eventData || {};
        const ev = cap.createEvent?.(eventName, eventData);
        if (ev) {
          if (target === "document") {
            if (cordova?.fireDocumentEvent) {
              cordova.fireDocumentEvent(eventName, eventData);
              return true;
            } else if (doc?.dispatchEvent) {
              return doc.dispatchEvent(ev);
            }
          } else if (target === "window" && win.dispatchEvent) {
            return win.dispatchEvent(ev);
          } else if (doc?.querySelector) {
            const targetEl = doc.querySelector(target);
            if (targetEl) {
              return targetEl.dispatchEvent(ev);
            }
          }
        }
        return false;
      };
      win.Capacitor = cap;
    };
    const initLegacyHandlers = (win, cap) => {
      win.cordova = win.cordova || {};
      const doc = win.document;
      const nav = win.navigator;
      if (nav) {
        nav.app = nav.app || {};
        nav.app.exitApp = () => {
          if (!cap.Plugins?.App) {
            win.console?.warn("App plugin not installed");
          } else {
            cap.nativeCallback("App", "exitApp", {});
          }
        };
      }
      if (doc) {
        const docAddEventListener = doc.addEventListener;
        doc.addEventListener = (...args) => {
          const eventName = args[0];
          const handler = args[1];
          if (eventName === "deviceready" && handler) {
            Promise.resolve().then(handler);
          } else if (eventName === "backbutton" && cap.Plugins.App) {
            if (!cap.Plugins?.App) {
              win.console?.warn("App plugin not installed");
            } else {
              cap.Plugins.App.addListener("backButton", () => {
              });
            }
          }
          return docAddEventListener.apply(doc, args);
        };
      }
      cap.platform = cap.getPlatform();
      cap.isNative = cap.isNativePlatform();
      win.Capacitor = cap;
    };
    const initVendor = (win, cap) => {
      const Ionic = win.Ionic = win.Ionic || {};
      const IonicWebView = Ionic.WebView = Ionic.WebView || {};
      const Plugins2 = cap.Plugins;
      IonicWebView.getServerBasePath = (callback) => {
        Plugins2?.WebView?.getServerBasePath().then((result) => {
          callback(result.path);
        });
      };
      IonicWebView.setServerAssetPath = (path) => {
        Plugins2?.WebView?.setServerAssetPath({ path });
      };
      IonicWebView.setServerBasePath = (path) => {
        Plugins2?.WebView?.setServerBasePath({ path });
      };
      IonicWebView.persistServerBasePath = () => {
        Plugins2?.WebView?.persistServerBasePath();
      };
      IonicWebView.convertFileSrc = (url) => cap.convertFileSrc(url);
      win.Capacitor = cap;
      win.Ionic.WebView = IonicWebView;
    };
    const initLogger = (win, cap) => {
      const BRIDGED_CONSOLE_METHODS = [
        "debug",
        "error",
        "info",
        "log",
        "trace",
        "warn"
      ];
      const createLogFromNative = (c) => (result) => {
        if (isFullConsole(c)) {
          const success = result.success === true;
          const tagStyles = success ? "font-style: italic; font-weight: lighter; color: gray" : "font-style: italic; font-weight: lighter; color: red";
          c.groupCollapsed(
            "%cresult %c" + result.pluginId + "." + result.methodName + " (#" + result.callbackId + ")",
            tagStyles,
            "font-style: italic; font-weight: bold; color: #444"
          );
          if (result.success === false) {
            c.error(result.error);
          } else {
            c.dir(result.data);
          }
          c.groupEnd();
        } else {
          if (result.success === false) {
            c.error?.("LOG FROM NATIVE", result.error);
          } else {
            c.log?.("LOG FROM NATIVE", result.data);
          }
        }
      };
      const createLogToNative = (c) => (call) => {
        if (isFullConsole(c)) {
          c.groupCollapsed(
            "%cnative %c" + call.pluginId + "." + call.methodName + " (#" + call.callbackId + ")",
            "font-weight: lighter; color: gray",
            "font-weight: bold; color: #000"
          );
          c.dir(call);
          c.groupEnd();
        } else {
          c.log?.("LOG TO NATIVE: ", call);
        }
      };
      const isFullConsole = (c) => {
        if (!c) {
          return false;
        }
        return typeof c.groupCollapsed === "function" || typeof c.groupEnd === "function" || typeof c.dir === "function";
      };
      const serializeConsoleMessage = (msg) => {
        try {
          if (typeof msg === "object") {
            msg = JSON.stringify(msg);
          }
          return String(msg);
        } catch (e) {
          return "";
        }
      };
      const platform = getPlatformId2(win);
      if (platform == "android" || platform == "ios") {
        const doPatchCookies = false;
        if (doPatchCookies) {
          Object.defineProperty(document, "cookie", {
            get: function() {
              if (platform === "ios") {
                const payload = {
                  type: "CapacitorCookies.get"
                };
                const res = prompt(JSON.stringify(payload));
                return res;
              } else if (typeof win.CapacitorCookiesAndroidInterface !== "undefined") {
                return win.CapacitorCookiesDescriptor?.get?.call(document) ?? "";
              }
            },
            set: function(val) {
              const cookiePairs = val.split(";");
              const domainSection = val.toLowerCase().split("domain=")[1];
              const domain = cookiePairs.length > 1 && domainSection != null && domainSection.length > 0 ? domainSection.split(";")[0].trim() : "";
              if (platform === "ios") {
                const payload = {
                  type: "CapacitorCookies.set",
                  action: val,
                  domain
                };
                prompt(JSON.stringify(payload));
              } else if (typeof win.CapacitorCookiesAndroidInterface !== "undefined") {
                win.CapacitorCookiesAndroidInterface.setCookie(domain, val);
              }
            }
          });
        }
        win.CapacitorWebFetch = window.fetch;
        let doPatchHttp = false;
        if (platform === "ios") {
          const payload = {
            type: "CapacitorHttp"
          };
          const isHttpEnabled = prompt(JSON.stringify(payload));
          if (isHttpEnabled === "true") {
            doPatchHttp = true;
          }
        } else if (typeof win.CapacitorHttpAndroidInterface !== "undefined") {
          const isHttpEnabled = win.CapacitorHttpAndroidInterface.isEnabled();
          if (isHttpEnabled === true) {
            doPatchHttp = true;
          }
        }
        if (doPatchHttp) {
          window.fetch = async (resource, options) => {
            const request = new Request(resource, options);
            if (request.url.startsWith(`${cap.getServerUrl()}/`)) {
              return win.CapacitorWebFetch(resource, options);
            }
            const { method } = request;
            if (method.toLocaleUpperCase() === "GET" || method.toLocaleUpperCase() === "HEAD" || method.toLocaleUpperCase() === "OPTIONS" || method.toLocaleUpperCase() === "TRACE") {
              if (typeof resource === "string") {
                return await win.CapacitorWebFetch(
                  createProxyUrl(resource, win),
                  options
                );
              } else if (resource instanceof Request) {
                const modifiedRequest = new Request(
                  createProxyUrl(resource.url, win),
                  resource
                );
                return await win.CapacitorWebFetch(modifiedRequest, options);
              }
            }
            const tag = `CapacitorHttp fetch ${Date.now()} ${resource}`;
            console.time(tag);
            try {
              const { body } = request;
              const optionHeaders = Object.fromEntries(request.headers.entries());
              const {
                data: requestData,
                type,
                headers
              } = await convertBody(
                options?.body || body || void 0,
                optionHeaders["Content-Type"] || optionHeaders["content-type"]
              );
              const nativeResponse = await cap.nativePromise(
                "CapacitorHttp",
                "request",
                {
                  url: request.url,
                  method,
                  data: requestData,
                  dataType: type,
                  headers: {
                    ...headers,
                    ...optionHeaders
                  }
                }
              );
              const contentType = nativeResponse.headers["Content-Type"] || nativeResponse.headers["content-type"];
              let data = contentType?.startsWith("application/json") ? JSON.stringify(nativeResponse.data) : nativeResponse.data;
              if (nativeResponse.status === 204) {
                data = null;
              }
              const response = new Response(data, {
                headers: nativeResponse.headers,
                status: nativeResponse.status
              });
              Object.defineProperty(response, "url", {
                value: nativeResponse.url
              });
              console.timeEnd(tag);
              return response;
            } catch (error) {
              console.timeEnd(tag);
              return Promise.reject(error);
            }
          };
          window.XMLHttpRequest = function() {
            const xhr = new win.CapacitorWebXMLHttpRequest.constructor();
            Object.defineProperties(xhr, {
              _headers: {
                value: {},
                writable: true
              },
              _method: {
                value: xhr.method,
                writable: true
              }
            });
            const prototype = win.CapacitorWebXMLHttpRequest.prototype;
            const isProgressEventAvailable = () => typeof ProgressEvent !== "undefined" && ProgressEvent.prototype instanceof Event;
            prototype.abort = function() {
              if (isRelativeOrProxyUrl(this._url)) {
                return win.CapacitorWebXMLHttpRequest.abort.call(this);
              }
              this.readyState = 0;
              setTimeout(() => {
                this.dispatchEvent(new Event("abort"));
                this.dispatchEvent(new Event("loadend"));
              });
            };
            prototype.open = function(method, url) {
              this._method = method.toLocaleUpperCase();
              this._url = url;
              if (!this._method || this._method === "GET" || this._method === "HEAD" || this._method === "OPTIONS" || this._method === "TRACE") {
                if (isRelativeOrProxyUrl(url)) {
                  return win.CapacitorWebXMLHttpRequest.open.call(
                    this,
                    method,
                    url
                  );
                }
                this._url = createProxyUrl(this._url, win);
                return win.CapacitorWebXMLHttpRequest.open.call(
                  this,
                  method,
                  this._url
                );
              }
              Object.defineProperties(this, {
                readyState: {
                  get: function() {
                    return this._readyState ?? 0;
                  },
                  set: function(val) {
                    this._readyState = val;
                    setTimeout(() => {
                      this.dispatchEvent(new Event("readystatechange"));
                    });
                  }
                }
              });
              setTimeout(() => {
                this.dispatchEvent(new Event("loadstart"));
              });
              this.readyState = 1;
            };
            prototype.setRequestHeader = function(header, value) {
              if (isRelativeOrProxyUrl(this._url)) {
                return win.CapacitorWebXMLHttpRequest.setRequestHeader.call(
                  this,
                  header,
                  value
                );
              }
              this._headers[header] = value;
            };
            prototype.send = function(body) {
              if (isRelativeOrProxyUrl(this._url)) {
                return win.CapacitorWebXMLHttpRequest.send.call(this, body);
              }
              const tag = `CapacitorHttp XMLHttpRequest ${Date.now()} ${this._url}`;
              console.time(tag);
              try {
                this.readyState = 2;
                Object.defineProperties(this, {
                  response: {
                    value: "",
                    writable: true
                  },
                  responseText: {
                    value: "",
                    writable: true
                  },
                  responseURL: {
                    value: "",
                    writable: true
                  },
                  status: {
                    value: 0,
                    writable: true
                  }
                });
                convertBody(body).then(({ data, type, headers }) => {
                  const otherHeaders = this._headers != null && Object.keys(this._headers).length > 0 ? this._headers : void 0;
                  cap.nativePromise("CapacitorHttp", "request", {
                    url: this._url,
                    method: this._method,
                    data: data !== null ? data : void 0,
                    headers: {
                      ...headers,
                      ...otherHeaders
                    },
                    dataType: type
                  }).then((nativeResponse) => {
                    if (this.readyState == 2) {
                      if (isProgressEventAvailable()) {
                        this.dispatchEvent(
                          new ProgressEvent("progress", {
                            lengthComputable: true,
                            loaded: nativeResponse.data.length,
                            total: nativeResponse.data.length
                          })
                        );
                      }
                      this._headers = nativeResponse.headers;
                      this.status = nativeResponse.status;
                      if (this.responseType === "" || this.responseType === "text") {
                        this.response = typeof nativeResponse.data !== "string" ? JSON.stringify(nativeResponse.data) : nativeResponse.data;
                      } else {
                        this.response = nativeResponse.data;
                      }
                      this.responseText = (nativeResponse.headers["Content-Type"] || nativeResponse.headers["content-type"])?.startsWith("application/json") ? JSON.stringify(nativeResponse.data) : nativeResponse.data;
                      this.responseURL = nativeResponse.url;
                      this.readyState = 4;
                      setTimeout(() => {
                        this.dispatchEvent(new Event("load"));
                        this.dispatchEvent(new Event("loadend"));
                      });
                    }
                    console.timeEnd(tag);
                  }).catch((error) => {
                    this.status = error.status;
                    this._headers = error.headers;
                    this.response = error.data;
                    this.responseText = JSON.stringify(error.data);
                    this.responseURL = error.url;
                    this.readyState = 4;
                    if (isProgressEventAvailable()) {
                      this.dispatchEvent(
                        new ProgressEvent("progress", {
                          lengthComputable: false,
                          loaded: 0,
                          total: 0
                        })
                      );
                    }
                    setTimeout(() => {
                      this.dispatchEvent(new Event("error"));
                      this.dispatchEvent(new Event("loadend"));
                    });
                    console.timeEnd(tag);
                  });
                });
              } catch (error) {
                this.status = 500;
                this._headers = {};
                this.response = error;
                this.responseText = error.toString();
                this.responseURL = this._url;
                this.readyState = 4;
                if (isProgressEventAvailable()) {
                  this.dispatchEvent(
                    new ProgressEvent("progress", {
                      lengthComputable: false,
                      loaded: 0,
                      total: 0
                    })
                  );
                }
                setTimeout(() => {
                  this.dispatchEvent(new Event("error"));
                  this.dispatchEvent(new Event("loadend"));
                });
                console.timeEnd(tag);
              }
            };
            prototype.getAllResponseHeaders = function() {
              if (isRelativeOrProxyUrl(this._url)) {
                return win.CapacitorWebXMLHttpRequest.getAllResponseHeaders.call(
                  this
                );
              }
              let returnString = "";
              for (const key in this._headers) {
                if (key != "Set-Cookie") {
                  returnString += key + ": " + this._headers[key] + "\r\n";
                }
              }
              return returnString;
            };
            prototype.getResponseHeader = function(name) {
              if (isRelativeOrProxyUrl(this._url)) {
                return win.CapacitorWebXMLHttpRequest.getResponseHeader.call(
                  this,
                  name
                );
              }
              return this._headers[name];
            };
            Object.setPrototypeOf(xhr, prototype);
            return xhr;
          };
          Object.assign(
            window.XMLHttpRequest,
            win.CapacitorWebXMLHttpRequest.fullObject
          );
        }
      }
      const isIos = getPlatformId2(win) === "ios";
      if (win.console && isIos) {
        Object.defineProperties(
          win.console,
          BRIDGED_CONSOLE_METHODS.reduce((props, method) => {
            const consoleMethod = win.console[method].bind(win.console);
            props[method] = {
              value: (...args) => {
                const msgs = [...args];
                cap.toNative?.("Console", "log", {
                  level: method,
                  message: msgs.map(serializeConsoleMessage).join(" ")
                });
                return consoleMethod(...args);
              }
            };
            return props;
          }, {})
        );
      }
      cap.logJs = (msg, level) => {
        switch (level) {
          case "error":
            win.console.error(msg);
            break;
          case "warn":
            win.console.warn(msg);
            break;
          case "info":
            win.console.info(msg);
            break;
          default:
            win.console.log(msg);
        }
      };
      cap.logToNative = createLogToNative(win.console);
      cap.logFromNative = createLogFromNative(win.console);
      cap.handleError = (err) => win.console.error(err);
      win.Capacitor = cap;
    };
    function initNativeBridge(win) {
      const cap = win.Capacitor || {};
      const callbacks = /* @__PURE__ */ new Map();
      const webviewServerUrl = typeof win.WEBVIEW_SERVER_URL === "string" ? win.WEBVIEW_SERVER_URL : "";
      cap.getServerUrl = () => webviewServerUrl;
      cap.convertFileSrc = (filePath) => convertFileSrcServerUrl(webviewServerUrl, filePath);
      let callbackIdCount = Math.floor(Math.random() * 134217728);
      let postToNative = () => null;
      const isNativePlatform = () => true;
      const getPlatform = () => getPlatformId2(win);
      cap.getPlatform = getPlatform;
      cap.isPluginAvailable = (name) => Object.prototype.hasOwnProperty.call(cap.Plugins, name);
      cap.isNativePlatform = isNativePlatform;
      if (getPlatformId2(win) === "android") {
        postToNative = (data) => {
          try {
            console.log("before androidBridge");
            win.androidBridge?.postMessage(JSON.stringify(data));
          } catch (e) {
            win?.console?.error(e);
          }
        };
      } else if (getPlatformId2(win) === "ios") {
        postToNative = (data) => {
          try {
            data.type = data.type ? data.type : "message";
            win.webkit?.messageHandlers?.bridge.postMessage(data);
          } catch (e) {
            win?.console?.error(e);
          }
        };
      }
      cap.handleWindowError = (msg, url, lineNo, columnNo, err) => {
        const str = msg.toLowerCase();
        if (str.indexOf("script error") > -1) {
        } else {
          const errObj = {
            type: "js.error",
            error: {
              message: msg,
              url,
              line: lineNo,
              col: columnNo,
              errorObject: JSON.stringify(err)
            }
          };
          if (err) {
            cap.handleError(err);
          }
          postToNative(errObj);
        }
        return false;
      };
      if (cap.DEBUG) {
        window.onerror = cap.handleWindowError;
      }
      initLogger(globalThis, cap);
      cap.toNative = (pluginName, methodName, options, storedCallback) => {
        try {
          if (typeof postToNative === "function") {
            let callbackId = "-1";
            if (storedCallback && (typeof storedCallback.callback === "function" || typeof storedCallback.resolve === "function")) {
              callbackId = String(++callbackIdCount);
              callbacks.set(callbackId, storedCallback);
            }
            const callData = {
              callbackId,
              pluginId: pluginName,
              methodName,
              options: options || {}
            };
            console.log("notNative before logToNative");
            if (cap.isLoggingEnabled && pluginName !== "Console") {
              cap.logToNative(callData);
            }
            console.log("notNative before postToNative");
            postToNative(callData);
            return callbackId;
          } else {
            win.console.warn(`implementation unavailable for: ${pluginName}`);
          }
        } catch (e) {
          win.console.error(e);
        }
        return "";
      };
      if (win?.androidBridge) {
        win.androidBridge.onmessage = function(event) {
          console.log("onmessage", event);
          returnResult(JSON.parse(event));
        };
      }
      cap.fromNative = (result) => {
        returnResult(result);
      };
      const returnResult = (result) => {
        if (cap.isLoggingEnabled && result.pluginId !== "Console") {
          cap.logFromNative(result);
        }
        try {
          const storedCall = callbacks.get(result.callbackId);
          if (storedCall) {
            if (result.error) {
              result.error = Object.keys(result.error).reduce((err, key) => {
                err[key] = result.error[key];
                return err;
              }, new cap.Exception(""));
            }
            if (typeof storedCall.callback === "function") {
              if (result.success) {
                storedCall.callback(result.data);
              } else {
                storedCall.callback(null, result.error);
              }
            } else if (typeof storedCall.resolve === "function") {
              if (result.success) {
                storedCall.resolve(result.data);
              } else {
                storedCall.reject(result.error);
              }
              callbacks.delete(result.callbackId);
            }
          } else if (!result.success && result.error) {
            win?.console?.warn(result.error);
          }
          if (result.save === false) {
            callbacks.delete(result.callbackId);
          }
        } catch (e) {
          win?.console?.error(e);
        }
        delete result.data;
        delete result.error;
      };
      cap.nativeCallback = (pluginName, methodName, options, callback) => {
        if (typeof options === "function") {
          console.warn(
            `Using a callback as the 'options' parameter of 'nativeCallback()' is deprecated.`
          );
          callback = options;
          options = void 0;
        }
        console.log("nativeCallback");
        return cap.toNative?.(pluginName, methodName, options, { callback }) || "";
      };
      cap.nativePromise = (pluginName, methodName, options) => {
        console.log("nativePromise");
        return new Promise((resolve, reject) => {
          cap.toNative?.(pluginName, methodName, options, {
            resolve,
            reject
          });
        });
      };
      cap.withPlugin = (_pluginId, _fn) => dummy;
      cap.Exception = CapacitorException;
      initEvents(win, cap);
      initLegacyHandlers(win, cap);
      initVendor(win, cap);
      win.Capacitor = cap;
    }
    initNativeBridge(w);
  };
  initBridge(
    typeof window !== "undefined" ? window : typeof globalThis !== "undefined" ? globalThis : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : typeof global !== "undefined" ? global : {}
  );
})();
/*! Bundled license information:

@capacitor/core/dist/index.js:
  (*! Capacitor: https://capacitorjs.com/ - MIT License *)
*/
