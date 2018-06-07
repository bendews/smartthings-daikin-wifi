# SmartThings Device Type - Daikin WiFi Split System

![Preview of device page in SmartThings interface](https://raw.githubusercontent.com/bendews/smartthings-daikin-wifi/master/preview.png)

This Device type allows the integration of Daikin Split Systems that have the optional "WiFi module" installed. Most commonly, this is the "BRP072A42" module. This Device Type has only been tested with that module, however other modules (such as the BRP069A42) should be compatible.

It implements the SmartThings "Thermostat" capability, which means it has full compatibility with most thermostat automations and allows integration with platforms such as Google Home.

Note that "P" Series units (FTXMxxP) do not support fan direction or rate controls.

## Features

- Ability to set desired temperature
- Ability to set desired mode (Auto, Cool, Heat, Dry, Fan)
- Seperate temperatures for each mode
- See indoor environment temperature
- See outdoor environment temperature
- Adjustment of fan direction (Horizontal, Vertical and "3D")
- Adjustment of fan rate (0-5, Auto, Silence)
- Fahrenheit and Celsius support

## To Install

Either copy the device handler directly in to the IDE, or add the repo:

- **Owner:** bendews
- **Name:** smartthings-daikin-wifi
- **Branch:** master

Once the Device Handler has been added, we can then add a virtual device via the IDE:

- Login to the IDE @ https://graph.api.smartthings.com/
- Click "My Devices"
- Click the "New Device" Button
- Enter a "Name" for the device, this can be whatever you want. (I.E "Bedroom Air Conditoner")
- Enter a "Label" for the device, this is optional and can be whatever you want.
- Enter a "Device Network Id" This can be anything, however it must be different from any other device. This ID will change once the device has been configured.
- "Zigbee" Id should be left blank
- Select a "Type" from the dropdown, this should be "Daikin WiFi Split System"
- "Version" should be published
- "Location" should be your hub location, probably "Home"
- "Hub" should be your hub name
- "Group" leave as is
- Click Create

Once created, we can then open the settings for the device, and fill out the following values:

![Preview of device settings in SmartThings interface](https://raw.githubusercontent.com/bendews/smartthings-daikin-wifi/master/settings.png)

- **Daikin WiFi IP Address:** The IP address of the WiFi unit
- **Daikin WiFi Port:** Ensure this is set to 80 if you are not sure
- **Refresh Interval in minutes:** The device will refresh automatically when tasks are triggered by SmartThings, however if it is being controlled by other means (I.E a remote), the state will go out of sync. This setting will refresh the device information on a set interval. 10 minutes or higher is strongly recommended.
- **Display Fahrenheit:** If toggled temperature values will be displayed in Fahrenheit

# License
[MIT](https://github.com/bendews/smartthings-daikin-wifi/master/LICENSE)

# Author Information

Created in 2018 by [Ben Dews](https://bendews.com)