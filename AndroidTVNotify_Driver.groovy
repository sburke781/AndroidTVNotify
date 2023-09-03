/**
 *  Android TV Notify Driver
 *
 *  Copyright 2023 Simon Burke
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2023-08-28  Simon Burke    Original Creation
  */


metadata {
	definition (name: 'Android TV Notify Device', namespace: 'simnet', author: 'Simon Burke') {
        
        capability 'Notification'
        
        //Notification Capability Command
        command 'deviceNotification', [[name:'text', type: 'STRING', description: 'Enter the notification text' ] ]
        
    }    
    
    
  preferences {
    
      // Logging Preferences
      input(name: "DebugLogging", type: "bool", title:"Enable Debug Logging",                   displayDuringSetup: true, defaultValue: false)
      input(name: "WarnLogging",  type: "bool", title:"Enable Warning Logging",                 displayDuringSetup: true, defaultValue: true )
      input(name: "ErrorLogging", type: "bool", title:"Enable Error Logging",                   displayDuringSetup: true, defaultValue: true )
      input(name: "InfoLogging",  type: "bool", title:"Enable Description Text (Info) Logging", displayDuringSetup: true, defaultValue: false)
    
      // TV Preferences
      input(name: "TVIPAddress", type: "string", title:"Android TV IP Address", displayDuringSetup: true, defaultValue: "")
      input(name: "DisplayDuration", type: "number", title:"Duration to Display Notification", displayDuringSetup: true, defaultValue: 30)
      input(name: "Position", type: "number", title:"Position of Notification", displayDuringSetup: true, defaultValue: 0)
      input(name: "DefaultTitle", type: "string", title:"Default Notification Title", displayDuringSetup: true, defaultValue: "Hubitat Notification")
      input(name: "TitleColor", type: "string", title:"Title Color (Hex Value Starting with #)", displayDuringSetup: true, defaultValue: "#")
      input(name: "TitleSize", type: "number", title:"Font Size of Message Title", displayDuringSetup: true, defaultValue: 20)
      input(name: "MessageColor", type: "string", title:"Message Color (Hex Value Starting with #)", displayDuringSetup: true, defaultValue: "#")
      input(name: "MessageSize", type: "number", title:"Font Size of Message Title", displayDuringSetup: true, defaultValue: 20)
      input(name: "BackgroundColor", type: "string", title:"Background Color (Hex Value Starting with #)", displayDuringSetup: true, defaultValue: "#")
    }
}

//Common Methods
void installed() { }

void initialized() { }

/*

curl --header "Content-Type: application/json" --request POST --data @text.json http://192.168.2.x:7979/notify


    {
"duration": 30,
"position": 0,
"title": "Your awesome title",
"titleColor": "#0066cc",
"titleSize": 20,
"message": "What ever you want to say... do it here...",
"messageColor": "#000000",
"messageSize": 14,
"backgroundColor": "#ffffff",
"media": { "image": {
"uri": "https://mir-s3-cdn-cf.behance.net/project_modules/max_1200/cfcc3137009463.5731d08bd66a1.png", "width": 480
}}
}
*/

//Notification Methods
void deviceNotification(String text) {

  deviceNotification(text, DefaultTitle);
}

void deviceNotification(String text, String title) {

  def headers = [:];
  headers.put("Content-Type", "application/json");
  def bodyJson = "{\"duration\": \"${DisplayDuration}\", \"position\": ${Position}, \"title\": \"${title}", \"titleColor\": \"${TitleColor}\", \"titleSize\": ${TitleSize}, \"message\": \"${text}\", \"messageColor\": \"${MessageColor}\", \"messageSize\": ${MessageSize}, \"backgroundColor\": \"${BackgroundColor}\" }";
  def postParams = [
        uri: "http://${TVIPAddress}:7979/notify",
        headers: headers,
        contentType: 'application/json',
        body : bodyJson
	]


    try {
        
        httpPost(postParams)
        { resp -> 
            debugLog("deviceNotification: ${resp.data}")
            
        }
            
	}
	catch (Exception e) {
        errorLog("deviceNotification: Unable to send notification: ${e}")
	}
}


//Logging Utility methods
def debugLog(debugMessage) {
	if (DebugLogging == true) {log.debug(debugMessage)}	
}

def errorLog(errorMessage) {
    if (ErrorLogging == true) { log.error(errorMessage)}  
}

def infoLog(infoMessage) {
    if(InfoLogging == true) {log.info(infoMessage)}    
}

def warnLog(warnMessage) {
    if(WarnLogging == true) {log.warn(warnMessage)}    
}