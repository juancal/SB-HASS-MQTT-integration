/**
 *  Aeon HEM - Node_Red
 *
 *  Copyright 2015 Juan Albert
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License  for the specific language governing permissions and limitations under the License.
 *
 *
 *
 *  Genesys: Based off of Aeon HEM - Xively by Dan Anghelescu, Aeon Smart Meter Code sample provided by SmartThings (2013-05-30), Aeon Home Energy Meter v2 by Barry A. Burke, and Xively Logger by Patrick Stuart  Built on US model
 *           may also work on international versions (currently reports total values only)
 */


// Automatically generated. Make future change here.
definition (
                name: "Aeon HEM - Node-Red",
                namespace: "juancal",
                author: "Juan Albert",
                description: "Aeon HEM - Node-Red Logger",
                category: "My Apps",
                iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
                iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("Log devices...") {
        input "energymeters", "capability.powerMeter", title: "Energy Meter", required: false, multiple: true
    }

    section ("nodeRed Info") {
        input "nr_url", "text", title: "Node-Red URL"
        input "nr_port", "number", title: "Node-Red IP Port"
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()

    initialize()
}

def initialize() {
    state.clear()
        unschedule(checkSensors)
        schedule("0 */15 * * * ?", "checkSensors")
        subscribe(app, appTouch)
}

def appTouch(evt) {
    log.debug "appTouch: $evt"
    checkSensors()
}



def checkSensors() {

    def logitems = []
    for (t in settings.energymeters) {
        logitems.add([t.displayName, "energymeter.energy", t.latestValue("energy")] )
        state[t.displayName + ".energy"] = t.latestValue("energy")
    }
    for (t in settings.energymeters) {
        logitems.add([t.displayName, "energymeter.power", t.latestValue("power")] )
        state[t.displayName + ".power"] = t.latestValue("power")
    }
    for (t in settings.energymeters) {
        logitems.add([t.displayName, "energymeter.volts", t.latestValue("volts")] )
        state[t.displayName + ".volts"] = t.latestValue("volts")
    }
    for (t in settings.energymeters) {
        logitems.add([t.displayName, "energymeter.amps", t.latestValue("amps")] )
        state[t.displayName + ".amps"] = t.latestValue("amps")
    }
    logField2(logitems)

}

private getFieldMap(channelInfo) {
    def fieldMap = [:]
    channelInfo?.findAll { it.key?.startsWith("field") }.each { fieldMap[it.value?.trim()] = it.key }
    return fieldMap
}


private logField2(logItems) {
    def fieldvalues = ""
    log.debug logItems


//    def nrPayload = ""
//    logItems.eachWithIndex() { item, i ->
//    def sid = "\"sId\":\"" + item[0].replace(" ","_") + "\""
//    def splitclass = item[1].tokenize('.')
//	def stype = splitclass[0]
//    def sloc = "mains"
//    def sphysic = splitclass[1]
    
//    nrPayload += "{\"sId\":\"${sid}\",\"sType\":\"${stype}\",\"sLoc\":\"mains\",\"sPhysics\":\"${sphysic}\",\"sValue\":\"${item[2]}\"}"

    def nrPayload = ""
    logItems.eachWithIndex() { item, i ->
    def sid = "\"sId\":\"" + item[0].replace(" ","_") + "\""
    def splitclass = item[1].tokenize('.')
	def stype = "\"sType\":\"" + splitclass[0] + "\""
    def sloc = "\"sLoc\":\"" + "mains" + "\""
    def sphysic = "\"sPhysics\":\"" + splitclass[1] + "\""
    def svalue = "\"sValue\":" + item[2]
    
    nrPayload += "{${sid},${stype},${sloc},${sphysic},${svalue}}"
    
    if (i.toInteger() + 1 < logItems.size())
    {
    nrPayload += ","
    }

    }
    log.debug nrPayload
    def uri = "${nr_url}:${nr_port}/sensor"
    def json = "{\"version\":\"1.0.0\",\"ts\":\"000\",\"datastreams\":[${nrPayload} ]}"

    def headers = [
        "X-node-red" : "Header Stub"
    ]

	log.debug uri
    log.debug headers
    log.debug json
    
    def params = [
        uri: uri,
        headers: headers,
        body: json
    ]
   // log.debug params.body
    httpPostJson(params) {response -> parseHttpResponse(response)}
}

def parseHttpResponse(response) {
    log.debug "HTTP Response: ${response}"
}

def captureState(theDevice) {
    def deviceAttrValue = [:]
    for ( attr in theDevice.supportedAttributes ) {
        def attrName = "${attr}"
        def attrValue = theDevice.currentValue(attrName)
        deviceAttrValue[attrName] = attrValue
    }
    return deviceAttrValue
}