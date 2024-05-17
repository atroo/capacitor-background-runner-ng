import { registerPlugin } from '@capacitor/core';
const BackgroundRunner = registerPlugin('BackgroundRunner', {
    web: () => import('./web').then(m => new m.BackgroundRunnerWeb()),
});
export * from './definitions';
// export * from './apis';
export { BackgroundRunner };
//# sourceMappingURL=index.js.map