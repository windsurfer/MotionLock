# MotionLock

A simple app to automatically lock your phone based on movement force, or the acceleration to be more accurate.

MotionLock can help protect your privacy and security by monitoring the accelerometer in the background and if the threshold is breached, lock the screen. 

The main use cases are if your phone is taken while you're using it, or you drop your phone.
As an additional bonus, it can also help save wear and tear on your power button!

It is based on the abandoned [Private Lock](https://github.com/wesaphzt/privatelock).

Inspired by the abandoned [PluckLockEx](https://github.com/0xFireball/PluckLockEx) project, which is based on [PluckLock](https://github.com/SyntaxBlitz/PluckLock).

<p>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/01-main.png?raw=true" width="200" height="400"/> 

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/02-test-sensitivity.png?raw=true" width="200" height="400"/> 
</p>

## Features
- Lock phone when accelerometer threshold is breached
- Animation to help test & visualize lock sensitivity
- Notification when service is running
- Start service on boot option
- Home screen widget
- Pause service for your defined length of time

## Requirements
Device admin permission for locking screen.

## Permissions
- android.permission.RECEIVE_BOOT_COMPLETED (start on boot)
- android.permission.WAKE_LOCK (start on boot)
- android.permission.BIND_DEVICE_ADMIN (lock the screen)
- android.permission.FOREGROUND_SERVICE (run the service)
- android.permission.BIND_JOB_SERVICE (run the service)

## Issues
To contribute, or to report issues please use the [Issue Tracker](https://github.com/windsurfer/privatelock/issues/).

### Known Issues
You will need to use your PIN code to unlock when phone lock is triggered, even when fingerprint/pattern unlock is enabled.
This is a system limitation and according to Google, a "feature, not a bug".

If you experience issues, please make sure battery optimizations are disabled for the app.

## Privacy
Free from ads and tracking.

## License
[GPL v3.0](LICENSE)

## Tips
If you find these apps useful, consider supporting your local humane society or animal shelter.
