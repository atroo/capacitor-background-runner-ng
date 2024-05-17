import { WebPlugin } from '@capacitor/core';
export class BackgroundRunnerWeb extends WebPlugin {
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
//# sourceMappingURL=web.js.map