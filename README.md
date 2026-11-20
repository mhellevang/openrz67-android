# OpenRZ67 for Android
An Android app for the [openrz67-trigger](https://github.com/mhellevang/openrz67-trigger), a Bluetooth remote release you build yourself for the Mamiya RZ67. The app needs the trigger to do anything. The [build guide](https://github.com/mhellevang/openrz67-trigger/blob/main/docs/build-guide.md) shows how to build one.

## Status:
The app works as intended for my use case, but I'm sure there are lots of improvements to be made or bugs to be ironed out. Pull requests welcome.

## Features:
- Automatically connects to the openrz67-trigger, and re-connects if connection is broken
- Shows the trigger's battery level
- Trigger the camera using the shutter button
- Trigger a delayed trigger with a user specified countdown
- Trigger bulb mode. The trigger only holds the release; the camera must be set to B (bulb), or it just takes one normal picture at the dialed speed

## Installation:
Needs Android 12 or later. Download the APK from [releases](https://github.com/mhellevang/openrz67-android/releases/latest) and open it on the phone. Android asks you to allow installing from that source. Or clone the repo and build it yourself.

## Using it
Press the button on the trigger to wake it. The app finds it and connects on its own. If something does not work, see [support](https://github.com/mhellevang/openrz67-trigger/blob/main/docs/support.md).

## Look & feel
Follows the system light/dark setting.

<img src="docs/images/direct.jpg" width="200" alt="Direct mode"> <img src="docs/images/countdown.jpg" width="200" alt="Countdown running"> <img src="docs/images/bulb.jpg" width="200" alt="Bulb exposure with elapsed time"> <img src="docs/images/dark.jpg" width="200" alt="Dark theme">

## License
[MIT](LICENSE). Privacy policy: [the app collects nothing](https://github.com/mhellevang/openrz67-trigger/blob/main/docs/privacy.md).

Not affiliated with Mamiya.
