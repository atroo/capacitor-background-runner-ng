import { WebPlugin } from '@capacitor/core';
import type { BackgroundRunnerPlugin, PermissionStatus } from './definitions';
export declare class BackgroundRunnerWeb extends WebPlugin implements BackgroundRunnerPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    checkPermissions(): Promise<PermissionStatus>;
    requestPermissions(): Promise<PermissionStatus>;
    registerBackgroundTask(): Promise<void>;
    dispatchEvent(): Promise<void>;
}
