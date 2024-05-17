var capacitorBackgroundRunner = (function (exports, core) {
    'use strict';

    const BackgroundRunner = core.registerPlugin('BackgroundRunner', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.BackgroundRunnerWeb()),
    });

    class BackgroundRunnerWeb extends core.WebPlugin {
        async echo(options) {
            console.log('ECHO', options);
            return options;
        }
        checkPermissions() {
            throw new Error('not available on web');
        }
        requestPermissions() {
            throw new Error('not available on web');
        }
        registerBackgroundTask() {
            throw new Error('not available on web');
        }
        dispatchEvent() {
            throw new Error('not available on web');
        }
    }

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        BackgroundRunnerWeb: BackgroundRunnerWeb
    });

    exports.BackgroundRunner = BackgroundRunner;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
