import { WebPlugin } from '@capacitor/core';

import type { BackgroundRunnerPlugin, PermissionStatus } from './definitions';

export class BackgroundRunnerWeb
  extends WebPlugin
  implements BackgroundRunnerPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  checkPermissions(): Promise<PermissionStatus> {
    throw new Error('not available on web');
  }

  requestPermissions(): Promise<PermissionStatus> {
    throw new Error('not available on web');
  }

  registerBackgroundTask(): Promise<void> {
    throw new Error('not available on web');
  }

  dispatchEvent(): Promise<void> {
    throw new Error('not available on web');
  }
}
