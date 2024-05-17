# @atroo/background-runner-ng
Capacitor plugin to allow background execution of JavaScript

## Install

```bash
npm install @atroo/background-runner-ng
npx cap sync
```

## Config
### Plugin config
Modify capacitor.config.ts
```
...
  plugins: {
    BackgroundRunner: {
      label: "com.example.background.task",
      src: "background.js", //js file only. The folder is fixed. Check the location below
      event: "updateData", //The event corresponds to the script's addEventListener
      repeat: true, //Repeating event
      interval: 15, //Interval of repetition. It should be more than 10. The exact timing is not guaranteed.
      autoStart: true,
      sqlite: { //SQLite related settings
        ios: {
          iosDatabaseLocation: 'Library/CapacitorDatabase',
          iosIsEncryption: true,
          iosKeychainPrefix: 'angular-sqlite-app-starter',
          iosBiometric: {
            biometricAuth: false,
            biometricTitle : "Biometric login for capacitor sqlite"
          },
        },
        android: {
          androidIsEncryption: false, //If the DB uses encryption
          androidBiometric: {// Not implemented yet.
            biometricAuth : false,
            biometricTitle : "Biometric login for capacitor sqlite",
            biometricSubTitle : "Log in using your biometric"
          }
        }
      }
    },
  },
...
```
The src file should be placed here:
```
<<project root>>/src/assets/<<script name>>
```

## About SQLite API
It intends to implement the interface of it correspondent capacitor plugin.
Since neither iOs nor Android supports typscript, the implementation is in ES2023.


## API

<docgen-index>

* [`checkPermissions()`](#checkpermissions)
* [`requestPermissions(...)`](#requestpermissions)
* [`dispatchEvent(...)`](#dispatchevent)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### checkPermissions()

```typescript
checkPermissions() => Promise<PermissionStatus>
```

Check permissions for the various Capacitor device APIs.

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

**Since:** 1.0.0

--------------------


### requestPermissions(...)

```typescript
requestPermissions(options: RequestPermissionOptions) => Promise<PermissionStatus>
```

Request permission to display local notifications.

| Param         | Type                                                                          |
| ------------- | ----------------------------------------------------------------------------- |
| **`options`** | <code><a href="#requestpermissionoptions">RequestPermissionOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#permissionstatus">PermissionStatus</a>&gt;</code>

**Since:** 1.0.0

--------------------


### dispatchEvent(...)

```typescript
dispatchEvent(options: DispatchEventOptions) => Promise<void>
```

Dispatches an event to the configured runner.

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`options`** | <code><a href="#dispatcheventoptions">DispatchEventOptions</a></code> |

**Since:** 1.0.0

--------------------


### Interfaces


#### PermissionStatus

| Prop                | Type                                                        |
| ------------------- | ----------------------------------------------------------- |
| **`geolocation`**   | <code><a href="#permissionstate">PermissionState</a></code> |
| **`notifications`** | <code><a href="#permissionstate">PermissionState</a></code> |


#### RequestPermissionOptions

| Prop       | Type               |
| ---------- | ------------------ |
| **`apis`** | <code>API[]</code> |


#### DispatchEventOptions

| Prop          | Type                                 | Description                                | Since |
| ------------- | ------------------------------------ | ------------------------------------------ | ----- |
| **`label`**   | <code>string</code>                  | The runner label to dispatch the event to  | 1.0.0 |
| **`event`**   | <code>string</code>                  | The name of the registered event listener. | 1.0.0 |
| **`details`** | <code>{ [key: string]: any; }</code> |                                            |       |


### Type Aliases


#### PermissionState

<code>'prompt' | 'prompt-with-rationale' | 'granted' | 'denied'</code>


#### API

<code>'geolocation' | 'notifications'</code>

</docgen-api>
