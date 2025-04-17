# Smart Garden System

## Overview

Smart Garden is an integrated IoT system designed to monitor and control garden conditions. The system consists of five interconnected components that work together to provide real-time monitoring and automated control of garden parameters such as light and temperature.

## System Architecture

The Smart Garden system is composed of the following subsystems:

1. **Garden SensorBoard (ESP32)**
   - Collects physical data (light and temperature) from the garden
   - Sends data to the Garden Service via HTTP POST
   - Receives system status updates via HTTP GET

2. **Garden Service (Backend PC)**
   - Central control hub that manages communication between all subsystems
   - Handles HTTP GET/POST requests on endpoints `/api/data` and `/api/status`
   - Manages serial communication with Garden Controller
   - Stores the latest sensor data and system status

3. **Garden Dashboard (Frontend PC)**
   - Java Swing user interface for visualizing sensor data and system status
   - Receives updates from Garden Service via HTTP GET requests
   - Refreshes data every 2 seconds

4. **Garden Controller (Arduino)**
   - Physical garden control system based on Arduino UNO
   - Manages irrigation (servo motor) and lighting (four green LEDs)
   - Communicates with Garden Service via serial connection
   - Receives commands from Mobile App via Bluetooth (HC-05 module)
   - Uses task-based architecture with finite state machines

5. **Garden Mobile App (Android)**
   - Mobile interface for monitoring and controlling the garden
   - Connects to Garden Controller via Bluetooth
   - Retrieves system status from Garden Service via HTTP GET

## Hardware

### Garden SensorBoard
- ESP32 development board
- Photoresistor (light sensor)
- TMP36 analog temperature sensor
- Green LED (for alarm state indication)
- Power supply

### Garden Controller
- Arduino UNO microcontroller
- Four green LEDs (lighting system)
- Servo motor (irrigation system)
- HC-05 Bluetooth module
- Power supply

## Software

- Arduino IDE or Visual Studio Code with Arduino framework for ESP32 and Arduino programming
- Eclipse IDE with Java and Vert.x for PC components
- Android Studio for mobile app development
- Ngrok for tunneling (to make localhost:8080 accessible externally)

## System Operation

### System States

The Garden Controller operates in three main states:
- **Auto**: System operates automatically based on sensor readings
- **Manual**: User controls the garden systems through the mobile app
- **Alarm**: System enters alarm state when abnormal conditions are detected

### Tasks

The Garden Controller implements three main tasks:
1. **GardenStateTask**: Manages the main system state and communicates with Garden Service (100ms period)
2. **LampsTask**: Controls the lighting system (four LEDs) in auto or manual mode (200ms period)
3. **IrrigationTask**: Controls the irrigation system (servo motor) in auto or manual mode (100ms period)

### Communication Protocols

- **ESP32 to Garden Service**: HTTP POST to `/api/data` and HTTP GET from `/api/status`
- **Garden Dashboard to Garden Service**: HTTP GET from `/api/data` and `/api/status`
- **Garden Service to Garden Controller**: Serial communication
- **Garden Mobile App to Garden Controller**: Bluetooth communication
- **Garden Mobile App to Garden Service**: HTTP GET from `/api/status`

## Data Format

- Sensor data is exchanged in JSON format with fields:
  - `name`: Sensor name
  - `value`: Current reading
  - `range`: Range information
- System status is exchanged in JSON format with:
  - `status`: Current system state

## Troubleshooting

- Ensure all devices are properly powered
- Check that WiFi credentials are correct on the ESP32
- Verify that Garden Service is running and accessible
- Ensure the Arduino is connected to the correct serial port
- Pair the HC-05 Bluetooth module with your Android device before using the app

## Future Improvements

- Add more sensors (soil moisture, humidity)
- Implement historical data storage and analysis
- Add user authentication and multi-user support
- Develop weather forecast integration
- Create power management for battery-operated components

## References

Full report and source code are available in the project repository.

