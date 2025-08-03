# Chat Bluetooth

A modern Android application for Bluetooth-based chat communication built with Material Design 3 principles.

## Features

- **Bluetooth Device Management**: Discover, pair, and connect with nearby Bluetooth devices
- **Real-time Messaging**: Send and receive text messages over Bluetooth connections
- **Modern UI**: Material Design 3 interface with dark/light theme support
- **Permission Handling**: Proper runtime permission management for Bluetooth operations
- **Navigation**: Bottom navigation with Connections and Chat tabs

## Architecture

The application follows modern Android development practices:

- **MVVM Architecture**: ViewModels for business logic
- **ViewBinding**: Type-safe view binding
- **Navigation Component**: Fragment-based navigation
- **Coroutines**: Asynchronous operations
- **Material Design 3**: Modern UI components

## Project Structure

```
app/src/main/
├── java/com/bluetoothchat/with/ai/
│   ├── MainActivity.kt              # Main activity with navigation setup
│   ├── ConnectionsFragment.kt       # Bluetooth device management
│   ├── ChatFragment.kt              # Messaging interface
│   ├── PairedDevicesAdapter.kt      # RecyclerView adapter for paired devices
│   ├── AvailableDevicesAdapter.kt   # RecyclerView adapter for discovered devices
│   ├── ChatAdapter.kt               # RecyclerView adapter for messages
│   └── BluetoothService.kt          # Bluetooth connection management
├── res/
│   ├── layout/                      # XML layouts
│   ├── drawable/                    # Vector drawables
│   ├── values/                      # Strings, themes, styles
│   ├── menu/                        # Menu resources
│   └── navigation/                  # Navigation graph
└── AndroidManifest.xml              # App manifest with permissions
```

## Permissions

The application requires the following permissions:

- `BLUETOOTH_SCAN`: For discovering nearby devices
- `BLUETOOTH_CONNECT`: For connecting to devices
- `BLUETOOTH_ADVERTISE`: For making the device discoverable
- `ACCESS_FINE_LOCATION`: Required for Bluetooth scanning
- `ACCESS_COARSE_LOCATION`: Alternative location permission

## Usage

1. **Enable Bluetooth**: The app will prompt to enable Bluetooth if it's disabled
2. **Grant Permissions**: Allow the required permissions when prompted
3. **Discover Devices**: Use the "Scan for New Devices" button to find nearby devices
4. **Connect**: Tap on a device to establish a connection
5. **Chat**: Once connected, switch to the Chat tab to send messages

## Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on a device with Bluetooth support

## Requirements

- Android API Level 24+ (Android 7.0)
- Device with Bluetooth support
- Android Studio Arctic Fox or later

## Dependencies

- Material Design 3: `com.google.android.material:material:1.12.0`
- Navigation Component: `androidx.navigation:navigation-fragment-ktx:2.7.7`
- ViewBinding: Built-in Android feature
- Coroutines: `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3`

## License

This project is for educational purposes and demonstrates modern Android development practices with Bluetooth functionality. "# chat-bluetooth-ai" 
