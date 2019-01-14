/**
 *  Daikin WiFi Split System
 *  V 1.4.1 - 11/12/2018
 *
 *  Copyright 2018 Ben Dews - https://bendews.com
 *  Contribution by RBoy Apps
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *	
 *
 *	Changelog:
 *
 *  1.0     (06/01/2018) - Initial 1.0 Release. All Temperature, Mode and Fan functions working.
 *  1.1     (06/01/2018) - Allow user to change device icon.
 *  1.2     (07/01/2018) - Fixed issue preventing user from setting desired temperature, added switch and temperature capabilities
 *  1.3     (10/01/2018) - Added support for outside temperature value, 1 minute refresh option (not reccomended) and fixed thermostat and switch state reporting when turned off
 *  1.4     (07/06/2018) - Added Fahrenheit support
 *  1.4.1   (11/12/2018) - Implemented fix for B-type adapters ('Host' header case) - Big thanks to @WGentine in the SmartThings community
 *
 */

import groovy.transform.Field

@Field final Map DAIKIN_MODES = [
    "0":    "auto",
    "1":    "auto",
    "2":    "dry",
    "3":    "cool",
    "4":    "heat",
    "6":    "fan",
    "7":    "auto",
    "off": "off",
]

@Field final Map DAIKIN_FAN_RATE = [
    "A":    "Auto",
    "B":    "Silence",
    "3":    "1",
    "4":    "2",
    "5":    "3",
    "6":    "4",
    "7":    "5"
]

@Field final Map DAIKIN_FAN_DIRECTION = [
    "0":    "Off",
    "1":    "Vertical",
    "2":    "Horizontal",
    "3":    "3D"
]

metadata {
    definition (name: "Daikin WiFi Split System", namespace: "bendews", author: "contact@bendews.com") {
        capability "Thermostat"
        capability "Temperature Measurement"
        capability "Actuator"
        capability "Switch"
        capability "Sensor"
        capability "Refresh"
        capability "Polling"

        attribute "outsideTemp", "number"
        attribute "targetTemp", "number"
        attribute "currMode", "string"
        attribute "fanAPISupport", "string"
        attribute "fanRate", "string"
        attribute "fanDirection", "string"
        attribute "statusText", "string"
        attribute "connection", "string"

        command "fan"
        command "dry"
        command "tempUp"
        command "tempDown"
        command "fanRateAuto"
        command "fanRateSilence"
        command "fanDirectionVertical"
        command "fanDirectionHorizontal"
        command "setFanRate", ["number"]
        command "setTemperature", ["number"]
    }


    preferences {
        input("ipAddress", "string", title:"Daikin WiFi IP Address", required:true, displayDuringSetup:true)
        input("ipPort", "string", title:"Daikin WiFi Port (default: 80)", defaultValue:80, required:true, displayDuringSetup:true)
        input("refreshInterval", "enum", title: "Refresh Interval in minutes", defaultValue: "10", required:true, displayDuringSetup:true, options: ["1","5","10","15","30"])
        input("displayFahrenheit", "boolean", title: "Display Fahrenheit", defaultValue: false, displayDuringSetup:true)
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale:2) {

        // Main Tile
        multiAttributeTile(name:"thermostatGeneric", type:"generic", width:6, height:4, canChangeIcon: true) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("temp", label:'${currentValue}°', unit:"dF", defaultState: true, backgroundColors: [
                // Celsius
                [value: 0, color: "#153591"],
                [value: 7, color: "#1e9cbb"],
                [value: 15, color: "#90d2a7"],
                [value: 23, color: "#44b621"],
                [value: 28, color: "#f1d801"],
                [value: 35, color: "#d04e00"],
                [value: 37, color: "#bc2323"],
                // Fahrenheit
				[value: 40, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 82, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 98, color: "#bc2323"]
                ])
            }

            tileAttribute("device.statusText", key: "SECONDARY_CONTROL") {
                attributeState("default", icon: "https://cdn.rawgit.com/bendews/smartthings-daikin-wifi/master/icons/temp-gauge.png", label:'${currentValue}', defaultState: true)
            }

            tileAttribute("device.targetTemp", key: "SLIDER_CONTROL", range:"(10..40)") {
                attributeState "level", action:"setTemperature", defaultState: true
            }
        }

        // Fan Rate controls
        valueTile("fanRateText", "device.fanRate",width: 3, height: 1) {
            state "val", label:'Fan Rate: ${currentValue}', defaultState: true
        }
        controlTile("fanRateSlider", "device.fanRate", "slider", height: 1, width: 1, range:"(1..5)") {
            state "level", action:"setFanRate"
        }
        standardTile("fanRateAuto", "device.fanRate", width:1, height:1) {
            state "default", label:'Auto', icon:"https://cdn.rawgit.com/bendews/smartthings-daikin-wifi/master/icons/fan-auto.png", backgroundColor:"#FFFFFF", action:"fanRateAuto", defaultState:true
            state "Auto", label:'Auto', icon:"https://cdn.rawgit.com/bendews/smartthings-daikin-wifi/master/icons/fan-auto.png", backgroundColor:"#00A0DC", action:"fanRateAuto"
        }
        standardTile("fanRateSilence", "device.fanRate", width:1, height:1) {
            state "default", label:'Silence', icon:"st.samsung.da.RAC_ic_silence", backgroundColor:"#FFFFFF", action:"fanRateSilence", defaultState:true
            state "Silence", label:'Silence', icon:"st.samsung.da.RAC_ic_silence", backgroundColor:"#00A0DC", action:"fanRateSilence"
        }
        
        // Fan Direction controls
        valueTile("fanDirectionText", "device.fanDirection",width: 4, height: 1) {
            state "val", label:'Fan Direction: ${currentValue}', defaultState: true
        }
        standardTile("fanDirectionVertical", "device.fanDirection", width:1, height:1) {
            state "default", label:'Vertical', icon:"https://cdn.rawgit.com/bendews/smartthings-daikin-wifi/master/icons/fan-vertical.png", backgroundColor:"#FFFFFF", action:"fanDirectionVertical", defaultState:true
            state "Vertical", label:'Vertical', icon:"https://cdn.rawgit.com/bendews/smartthings-daikin-wifi/master/icons/fan-vertical.png", backgroundColor:"#00A0DC", action:"fanDirectionVertical"
            state "3D", label:'Vertical', icon:"https://cdn.rawgit.com/bendews/smartthings-daikin-wifi/master/icons/fan-vertical.png", backgroundColor:"#00A0DC", action:"fanDirectionVertical"
        }
        standardTile("fanDirectionHorizontal", "device.fanDirection", width:1, height:1) {
            state "default", label:'Horizontal', icon:"https://cdn.rawgit.com/bendews/smartthings-daikin-wifi/master/icons/fan-horizontal.png", backgroundColor:"#FFFFFF", action:"fanDirectionHorizontal", defaultState:true
            state "Horizontal", label:'Horizontal', icon:"https://cdn.rawgit.com/bendews/smartthings-daikin-wifi/master/icons/fan-horizontal.png", backgroundColor:"#00A0DC", action:"fanDirectionHorizontal"
            state "3D", label:'Horizontal', icon:"https://cdn.rawgit.com/bendews/smartthings-daikin-wifi/master/icons/fan-horizontal.png", backgroundColor:"#00A0DC", action:"fanDirectionHorizontal"
        }  

        // Mode Toggles
        standardTile("modeHeat", "device.thermostatMode", width:2, height:2) {
            state "default", label:'Heat', icon:"st.Weather.weather14", backgroundColor:"#FFFFFF", action:"thermostat.heat", defaultState:true
            state "heat", label:'Heat', icon:"st.Weather.weather14", backgroundColor:"#E86D13", action:"thermostat.off"
        }

        standardTile("modeCool", "device.thermostatMode", width:2, height:2) {
            state "default", label:'Cool', icon:"st.Weather.weather7", backgroundColor:"#FFFFFF", action:"thermostat.cool", defaultState:true
            state "cool", label:'Cool', icon:"st.Weather.weather7", backgroundColor:"#00A0DC", action:"thermostat.off"
        }

        standardTile("modeAuto", "device.thermostatMode", width:2, height:2) {
            state "default", label:'Auto', icon:"st.tesla.tesla-hvac", backgroundColor:"#FFFFFF", action:"thermostat.auto", defaultState:true
            state "auto", label:'Auto', icon:"st.tesla.tesla-hvac", backgroundColor:"#F1D801", action:"thermostat.off"
        }
        
        standardTile("modeDry", "device.thermostatMode", width:2, height:2) {
            state "default", label:'Dry', icon:"st.Weather.weather12", backgroundColor:"#FFFFFF", action:"dry", defaultState:true
            state "dry", label:'Dry', icon:"st.Weather.weather12", backgroundColor:"#00A0DC", action:"thermostat.off"
        }

        standardTile("modeFan", "device.thermostatMode", width:2, height:2) {
            state "default", label:'Fan', icon:"st.Appliances.appliances11", backgroundColor:"#FFFFFF", action:"fan", defaultState:true
            state "fan", label:'Fan', icon:"st.Appliances.appliances11", backgroundColor:"#00A0DC", action:"thermostat.off"
        }

        // Outside Temp
        valueTile("outsideTemp", "device.outsideTemp", width:2, height:2, inactiveLabel: false) {
            state("val", label:'Outside: ${currentValue}°', backgroundColors:[
                // Celsius
                [value: 0, color: "#153591"],
                [value: 7, color: "#1e9cbb"],
                [value: 15, color: "#90d2a7"],
                [value: 23, color: "#44b621"],
                [value: 28, color: "#f1d801"],
                [value: 35, color: "#d04e00"],
                [value: 37, color: "#bc2323"],
                // Fahrenheit
				[value: 40, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 82, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 98, color: "#bc2323"]
                ])
        }

        // Refresh       
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width:2, height:2) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }  


        main("thermostatGeneric")
        details([
            "thermostatGeneric",
            "fanRateText",
            "fanRateSlider",
            "fanRateAuto",
            "fanRateSilence",
            "fanDirectionText",
            "fanDirectionVertical",
            "fanDirectionHorizontal",
            "modeHeat",
            "modeCool",
            "modeAuto",
            "modeFan",
            "modeDry",
            "outsideTemp",
            "refresh"])
    }
}

// Generic Private Functions -------
private getHostAddress() {
    def ip = settings.ipAddress
    def port = settings.ipPort
    return ip + ":" + port
}

private getDNI(String ipAddress, String port){
    log.debug "Generating DNI"
    String ipHex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    String portHex = String.format( '%04X', port.toInteger() )
    String newDNI = ipHex + ":" + portHex
    return newDNI
}

private apiGet(def apiCommand) {
    log.debug "Executing hubaction on " + getHostAddress() + apiCommand
    sendEvent(name: "hubactionMode", value: "local")

    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: apiCommand,
        headers: [Host:getHostAddress()]
    )

    return hubAction
}

private roundHalf(Double num){
    return ((num * 2).round() / 2)
}

private convertTemp(Double temp, Boolean isFahrenheit){
    log.debug "Converting ${temp}, Fahrenheit: ${isFahrenheit}"
    Double convertedTemp
    if (isFahrenheit) {
        convertedTemp = ((temp - 32) * 5) / 9
        return convertedTemp.round()
    }
    convertedTemp = ((temp * 9) / 5) + 32
    return convertedTemp.round()
}

private delayAction(long time) {
    log.debug "Delay for '${time}'"
    new physicalgraph.device.HubAction("delay $time")
}
// -------


// Daikin Specific Private Functions -------
private parseTemp(Double temp, String method){
    log.debug "${method}-ing ${temp}"
    if (settings.displayFahrenheit.toBoolean()) {
        switch(method) {
            case "GET":
                return convertTemp(temp, false)
            case "SET":
                return convertTemp(temp, true)
        }
    }
    return temp
}
private parseDaikinResp(String response) {
    // Convert Daikin response to Groovy Map
    // Convert to JSON
    def parsedResponse = response.replace("=", "\":\"").replace(",", "\",\"")
    def jsonString = "{\"${parsedResponse}\"}"
    // Parse JSON to Map
    def results = new groovy.json.JsonSlurper().parseText(jsonString)  
    return results
}

private updateDaikinDevice(Boolean turnOff = false){
    // "Power", either 0 or 1
    def pow = "?pow=1"
    // "Mode", 0, 1, 2, 3, 4, 6 or 7
    def mode = "&mode=3"
    // "Set Temperature", degrees in Celsius
    def sTemp = "&stemp=26"
    // "Fan Rate", A, B, 3, 4, 5, 6, or 7
    def fRate = "&f_rate=A"
    // "Fan Direction", 0, 1, 2, or 3
    def fDir = "&f_dir=3"

    // Current mode selected in smartthings
    // If turning unit off, get current mode of unit instead of desired mode
    String modeAttr = turnOff ? "currMode" : "thermostatMode"
    def currentMode = device.currentState(modeAttr)?.value
    // Convert textual mode (e.g "cool") to Daikin Code (e.g "3")
    def currentModeKey = DAIKIN_MODES.find{ it.value == currentMode }?.key

    // Current fan rate selected in smartthings
    def currentfRate = device.currentState("fanRate")?.value
    // Convert textual fan rate (e.g "lvl1") to Daikin Code (e.g "3")
    def currentfRateKey = DAIKIN_FAN_RATE.find{ it.value == currentfRate }?.key

    // Current fan direction selected in smartthings
    def currentfDir = device.currentState("fanDirection")?.value
    // Convert textual fan direction (e.g "3d") to Daikin Code (e.g "3")
    def currentfDirKey = DAIKIN_FAN_DIRECTION.find{ it.value == currentfDir }?.key
    log.debug "${currentfDirKey}"
    
    // Get target temperature set in Smartthings
    def targetTemp = parseTemp(device.currentValue("targetTemp"), "SET")

    // Set power mode in HTTP call
    if (turnOff) {
        pow = "?pow=0"
    }
    if (currentModeKey.isNumber()){
        // Set desired mode in HTTP call
        mode = "&mode=${currentModeKey}"
    }
    if (targetTemp){
        // Set desired Target Temperature in HTTP call
        sTemp = "&stemp=${targetTemp}"
    }
    if (currentfRateKey){
        // Set desired Fan Rate in HTTP call
        fRate = "&f_rate=${currentfRateKey}"
    }
    if (currentfDirKey){
        // Set desired Fan Direction in HTTP call
        fDir = "&f_dir=${currentfDirKey}"
    }

    def apiCalls = [
        // Send HTTP Call to update device
        apiGet("/aircon/set_control_info"+pow+mode+sTemp+fRate+fDir+"&shum=0"),
        delayAction(500),
        // Get mode info
        apiGet("/aircon/get_control_info"),
        delayAction(500),
        // Get temperature info
        apiGet("/aircon/get_sensor_info")
    ]
    return apiCalls
}
// -------


// Utility Functions -------
private startScheduledRefresh() {
    log.debug "startScheduledRefresh()"
    // Get minutes from settings
    def minutes = settings.refreshInterval?.toInteger()
    if (!minutes) {
        log.warn "Using default refresh interval: 10"
        minutes = 10
    }
    log.debug "Scheduling polling task for every '${minutes}' minutes"
    if (minutes == 1){
        runEvery1Minute(refresh)
    } else {
        "runEvery${minutes}Minutes"(refresh)
    }
}

def setDNI(){
    log.debug "Setting DNI"
    String ip = settings.ipAddress
    String port = settings.ipPort
    String newDNI = getDNI(ip, port)
    device.setDeviceNetworkId("${newDNI}")
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    // Prevent function from running twice on save
    if (!state.updated || now() >= state.updated + 5000){
        // Unschedule existing tasks
        unschedule()
        // Set DNI
        runIn(1, setDNI)
        runIn(5, refresh)
        // Start scheduled task
        startScheduledRefresh()
    }
    state.updated = now()
}

def poll() {
    log.debug "Executing poll(), unscheduling existing"
    refresh()
}

def refresh() {
    log.debug "Refreshing"
    def apiCalls = [
        sendHubCommand(apiGet("/aircon/get_sensor_info")),
        sendHubCommand(delayAction(500)),
        sendHubCommand(apiGet("/aircon/get_control_info"))
        ]
    return apiCalls
}

def installed() {
    log.debug "installed()"
    sendEvent(name:'heatingSetpoint', value: '18', displayed:false)
    sendEvent(name:'coolingSetpoint', value: '28', displayed:false)
    sendEvent(name:'temperature', value: null, displayed:false)
    sendEvent(name:'targetTemp', value: null, displayed:false)
    sendEvent(name:'thermostatOperatingState', value:'idle', displayed:false)
    sendEvent(name:'outsideTemp', value: null, displayed:false)
    sendEvent(name:'currMode', value: null, displayed:false)
    sendEvent(name:'thermostatMode', value: null, displayed:false)
    sendEvent(name:'thermostatFanMode', value: null, displayed:false)
    sendEvent(name:'fanRate', value: null, displayed:false)
    sendEvent(name:'fanDirection', value: null, displayed:false)
    sendEvent(name:'fanState', value: null, displayed:false)
}
// -------


// Parse and Update functions
def parse(String description) {
    // Parse Daikin response
    def msg = parseLanMessage(description)
    def body = msg.body
    def daikinResp = parseDaikinResp(body)
    // Debug Response
    log.debug "Parsing Response: ${daikinResp}"
    // Custom definitions
    def events = []
    def turnedOff = false
    def currMode = null
    def modeVal = null
    def targetTempVal = null
    // Define return field data we are interested in
    def devicePower = daikinResp.get("pow", null)
    def deviceMode = daikinResp.get("mode", null)
    def deviceInsideTempSensor = daikinResp.get("htemp", null)
    def deviceOutsideTempSensor = daikinResp.get("otemp", null)
    def deviceTargetTemp = daikinResp.get("stemp", null)
    def devicefanRate = daikinResp.get("f_rate", null)
    def devicefanDirection = daikinResp.get("f_dir", null)
    def deviceFanSupport = device.currentValue("fanAPISupport")
    //  Get power info
    if (devicePower){
        // log.debug "pow: ${devicePower}"
        if (devicePower == "0") {
            turnedOff = true
            events.add(createEvent(name: "thermostatMode", value: "off"))
        }  
    }
    //  Get mode info
    if (deviceMode){
        // log.debug "mode: ${deviceMode}"
        currMode = DAIKIN_MODES.get(deviceMode.toString())
        if (!turnedOff) {
            modeVal = currMode
        }
        events.add(createEvent(name: "currMode", value: currMode))
    }
    //  Get inside temperature sensor info
    if (deviceInsideTempSensor){
        // log.debug "htemp: ${deviceInsideTempSensor}"
        String insideTemp = parseTemp(Double.parseDouble(deviceInsideTempSensor), "GET")
        events.add(createEvent(name: "temperature", value: insideTemp))
    }
    //  Get outside temperature sensor info
    if (deviceOutsideTempSensor){
        // log.debug "otemp: ${deviceOutsideTempSensor}"
        String outsideTemp = parseTemp(Double.parseDouble(deviceOutsideTempSensor), "GET")
        events.add(createEvent(name: "outsideTemp", value: outsideTemp))
    }
    //  Get currently set target temperature
    if (deviceTargetTemp){
        // log.debug "stemp: ${deviceTargetTemp}"
        // Value of "M" is for modes that don't support temperature changes, make value null
        targetTempVal = deviceTargetTemp.isNumber() ? parseTemp(Double.parseDouble(deviceTargetTemp), "GET") : null
    }
    //  Get current fan rate
    if (devicefanRate){
        // log.debug "f_rate: ${devicefanRate}"
        events.add(createEvent(name: "fanAPISupport", value: "true", displayed: false))
        events.add(createEvent(name: "fanRate", value: DAIKIN_FAN_RATE.get(devicefanRate)))
    }
    //  Get current fan direction
    if (devicefanDirection){
        // log.debug "f_dir: ${devicefanDirection}"
        events.add(createEvent(name: "fanDirection", value: DAIKIN_FAN_DIRECTION.get(devicefanDirection)))
    }
    // If device doesnt support API Fan functions
    if (deviceMode && !devicefanRate){
        // log.debug "Fan support: False"
        events.add(createEvent(name: "fanAPISupport", value: "false", displayed: false))
    }
    
    // Update temperature and mode values if applicable (and all other values based from that)
    // Add to start of returned list for faster UI feedback
    if (modeVal || targetTempVal){
        events.add(0, updateEvents(mode: modeVal, temperature: targetTempVal, updateDevice: false))
    }

    return events
}

private updateEvents(Map args){
    log.debug "Executing 'updateEvents' with ${args.mode}, ${args.temperature} and ${args.updateDevice}"
    // Get args with default values
    def mode = args.get("mode", null)
    def temperature = args.get("temperature", null)
    def updateDevice = args.get("updateDevice", false)
    // Daikin "Off" mode is handled as seperate attribute
    // Smarthings thermostat handles "Off" as another mode
    // Work around this by defining a "turnOff" boolean and set where appropiate
    Boolean turnOff = false
    def events = []
    if (!mode){
        mode = device.currentValue("thermostatMode")
    } else {
        events.add(sendEvent(name: "thermostatMode", value: mode))
    }
    if (!temperature){
        temperature = device.currentValue("targetTemp")
    }
    switch(mode) {
        case "fan":
            events.add(sendEvent(name: "statusText", value: "Fan Mode", displayed: false))
            events.add(sendEvent(name: "thermostatOperatingState", value: "fan only", displayed: false))
            events.add(sendEvent(name: "targetTemp", value: null))
            break
        case "dry":
            events.add(sendEvent(name: "statusText", value: "Dry Mode", displayed: false))
            events.add(sendEvent(name: "thermostatOperatingState", value: "fan only", displayed: false))
            events.add(sendEvent(name: "targetTemp", value: null))
            break
        case "heat":
            events.add(sendEvent(name: "statusText", value: "Heating to ${temperature}°", displayed: false))
            events.add(sendEvent(name: "thermostatOperatingState", value: "heating", displayed: false))
            events.add(sendEvent(name: "heatingSetpoint", value: temperature, displayed: false))
            events.add(sendEvent(name: "targetTemp", value: temperature))
            break
        case "cool":
            events.add(sendEvent(name: "statusText", value: "Cooling to ${temperature}°", displayed: false))
            events.add(sendEvent(name: "thermostatOperatingState", value: "cooling", displayed: false))
            events.add(sendEvent(name: "coolingSetpoint", value: temperature, displayed: false))
            events.add(sendEvent(name: "targetTemp", value: temperature))
            break
        case "auto":
            events.add(sendEvent(name: "statusText", value: "Auto Mode: ${temperature}°", displayed: false))
            events.add(sendEvent(name: "targetTemp", value: temperature))
            break
        case "off":
            events.add(sendEvent(name: "statusText", value: "System is off", displayed: false))
            events.add(sendEvent(name: "thermostatOperatingState", value: "idle", displayed: false))
            turnOff = true
            break
    }

    if (turnOff){
        events.add(sendEvent(name: "switch", value: "off", displayed: false))
    } else {
        events.add(sendEvent(name: "switch", value: "on", displayed: false))
    }

    if (updateDevice){
        updateDaikinDevice(turnOff)
    }

}
// -------


// Temperature Functions
def tempUp() {
    log.debug "tempUp()"
    def step = 0.5
    def mode = device.currentValue("thermostatMode")
    def targetTemp = device.currentValue("targetTemp")
    def value = targetTemp + step
    updateEvents(temperature: value, updateDevice: true)
}

def tempDown() {
    log.debug "tempDown()"
    def step = 0.5
    def mode = device.currentValue("thermostatMode")
    def targetTemp = device.currentValue("targetTemp")
    def value = targetTemp - step
    updateEvents(temperature: value, updateDevice: true)
}

def setThermostatMode(String newMode) {
    log.debug "Executing 'setThermostatMode'"
    def currMode = device.currentValue("thermostatMode")
    if (currMode != newMode){
        updateEvents(mode: newMode, updateDevice: true)
    }
}


def setTemperature(Double value) {
    log.debug "Executing 'setTemperature' with ${value}"
    updateEvents(temperature: value, updateDevice: true)
}

def setHeatingSetpoint(Double value) {
    log.debug "Executing 'setHeatingSetpoint' with ${value}"
    updateEvents(temperature: value, updateDevice: true)
}

def setCoolingSetpoint(Double value) {
    log.debug "Executing 'setCoolingSetpoint' with ${value}"
    updateEvents(temperature: value, updateDevice: true)
}
// -------


// Daikin "Modes" ----
def auto(){
    log.debug "Executing 'auto'"
    updateEvents(mode: "auto", updateDevice: true)
}

def dry() {
    log.debug "Executing 'dry'"
    updateEvents(mode: "dry", updateDevice: true)
}

def cool() {
    log.debug "Executing 'cool'"
    // Set target temp to previously set Cool temperature
    def coolPoint = device.currentValue("coolingSetpoint")
    updateEvents(mode: "cool", temperature: coolPoint, updateDevice: true)
}

def heat() {
    log.debug "Executing 'heat'"
    // Set target temp to previously set Heat temperature
    def heatPoint = device.currentValue("heatingSetpoint")
    updateEvents(mode: "heat", temperature: heatPoint, updateDevice: true)
}

def fan() {
    log.debug "Executing 'fan'"
    updateEvents(mode: "fan", updateDevice: true)
}
// -------


// Switch Functions ----
def on(){
    log.debug "Executing 'on'"
    def currMode = device.currentValue("currMode")
    updateEvents(mode: currMode, updateDevice: true)
}

def off() {
    log.debug "Executing 'off'"
    updateEvents(mode: "off", updateDevice: true)
}
// -------


// Fan Actions ----
private fanAPISupported() {
    // Returns boolean based on whether API Fan actions are supported by the model
    String deviceFanSupport = device.currentValue("fanAPISupport")
    if (deviceFanSupport == "false"){
        log.debug "Fan settings not supported on this model"
        sendEvent(name: "fanDirection", value: "Not Supported")
        return false
    } else {
        return true
    }
}

def setFanRate(def fanRate) {
    log.debug "Executing 'setFanRate' with ${fanRate}"
    def currFanRate = device.currentValue("fanRate")
    // Check that rate is different before setting.
    // TODO: Clean messy IF statements
	if (currFanRate != fanRate){
		if (fanAPISupported()){
			sendEvent(name: "supportedThermostatFanModes", value: ["auto", "on"], displayed: false) // We don't support circulate at this time
			if (fanRate == 0){
				sendEvent(name: "fanRate", value: "Auto")
				sendEvent(name: "thermostatFanMode", value: "auto", displayed: false)
			} else {
				sendEvent(name: "fanRate", value: fanRate)
				switch (fanRate) {
					case "Auto":
						sendEvent(name: "thermostatFanMode", value: "auto", displayed: false)
						break

					default: // all other modes indicate fan on
						sendEvent(name: "thermostatFanMode", value: "on", displayed: false)
						break
				}
			}
			updateDaikinDevice(false)
		} else {
			sendEvent(name: "fanRate", value: "Not Supported")
		}
	}
}

def fanRateAuto(){
    log.debug "Executing 'fanRateAuto'"
    setFanRate("Auto")
}

def fanRateSilence(){
    log.debug "Executing 'fanRateSilence'"
    setFanRate("Silence")
}

def toggleFanDirection(String toggleDir){
    log.debug "Executing 'toggleFanDirection' with ${toggleDir}"
    String currentDir = device.currentValue("fanDirection")
    
    if (currentDir == "Off"){
        // If both directions are OFF, set to toggled value
        sendEvent(name: "fanDirection", value: toggleDir)
    } else if (currentDir == "3D"){
        // If both directions are on ("3D"), set to opposite of toggled state
        String newDir = toggleDir == "Horizontal" ? "Vertical" : "Horizontal"
        sendEvent(name: "fanDirection", value: newDir)
    } else if (currentDir != toggleDir) {
        // If one direction is on and toggled state is not currently active, turn on both
        sendEvent(name: "fanDirection", value: "3D")
    } else if (currentDir == toggleDir){
        // If toggled state is currently active, turn directional off
        sendEvent(name: "fanDirection", value: "Off")
    }
    if (fanAPISupported()){
        updateDaikinDevice(false)
    } else {
        sendEvent(name: "fanDirection", value: "Not Supported")
    }

}

def fanDirectionHorizontal() {
    log.debug "Executing 'fanDirectionHorizontal'"
    toggleFanDirection("Horizontal") 
}

def fanDirectionVertical() {
    log.debug "Executing 'fanDirectionVertical'"
    toggleFanDirection("Vertical") 
}

def fanOn() {
    log.debug "Executing 'fanOn'"
    fanRateSilence() // Should this be made configurable to allow the user to select the default fan rate?
}

def fanAuto() {
    log.debug "Executing 'fanAuto'"
	fanRateAuto()
}

// TODO: Implement these functions if possible
 def fanCirculate() {
    log.warn "Executing 'fanCirculate' not currently supported"
    // TODO: handle 'fanCirculate' command
 }

 def setThermostatFanMode(String value) {
    log.debug "Executing 'setThermostatFanMode' with fan mode $value"
	switch(value){
		case "auto":
			fanAuto()
			break
		
		case "on":
			fanOn()
			break
		
		case "circulate":
			fanCirculate()
			break
		
		default:
			log.warn "Unknown fan mode: $value"
			break
	}
 }

// def setSchedule() {
    // log.debug "Executing 'setSchedule'"
    // TODO: handle 'setSchedule' command
// }
// -------

