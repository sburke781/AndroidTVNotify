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

import java.net.URLEncoder;

metadata {
	definition (name: 'Android TV Notify Device', namespace: 'simnet', author: 'Simon Burke') {
        
        capability 'Notification'
        capability 'Actuator'
        
        //Notification Capability Command
        command 'deviceNotification', [[name:'text', type: 'STRING', description: 'Enter the notification text' ] ]
        command "customNotificationTitle", [[name:'text', type: 'STRING', description: 'Enter the notification text' ], [name:'title', type: 'STRING', description: 'Enter the notification title', defaultValue: 'Hubitat Notification' ], [name:'DebugMessage', type: 'INTEGER', description: 'Zero = Send Notification to TV, 1 = Log Only', defaultValue: 0 ] ]
        
    }    
    
    
  preferences {
    
      // TV Preferences
      input(name: "TVIPAddress", type: "string", title:"Android TV IP Address", displayDuringSetup: true, defaultValue: "")
      input(name: "Port", type: "number", title:"Android TV Notify Port Number", displayDuringSetup: true, defaultValue: 7979)
      input(name: "DefaultDuration", type: "number", title:"Default Duration to Display Notification", displayDuringSetup: true, defaultValue: 30)
      input(name: "DefaultPosition", type: "enum", title:"Default Position of Notification", displayDuringSetup: true, defaultValue: "Top Right", required: true, options: ["Top Left","Top Right","Bottom Left","Bottom Right","Centre"])
      input(name: "DefaultTitle", type: "string", title:"Default Notification Title", displayDuringSetup: true, defaultValue: "Hubitat Notification")
      input(name: "DefaultTitleColor", type: "string", title:"Default Title Color", description: "Hex Value Starting with #", displayDuringSetup: true, defaultValue: "#")
      input(name: "DefaultTitleSize", type: "number", title:"Default Title Font Size", displayDuringSetup: true, defaultValue: 20)
      input(name: "DefaultMessageColor", type: "string", title:"Default Message Text Color", description: "Hex Value Starting with #", displayDuringSetup: true, defaultValue: "#")
      input(name: "DefaultMessageSize", type: "number", title:"Default Message Text Font Size", displayDuringSetup: true, defaultValue: 14)
      input(name: "DefaultBackgroundColor", type: "string", title:"Default Background Color", description: "Hex Value Starting with #",  displayDuringSetup: true, defaultValue: "#")
      input(name: "DefaultImageURI", type: "string", title:"Default Image URI",  displayDuringSetup: true, defaultValue: "")
      input(name: "DefaultImageWidth", type: "number", title:"Default Image Width", displayDuringSetup: true, defaultValue: 480)

      // Logging Preferences
      input(name: "DebugLogging", type: "bool", title:"Enable Debug Logging",                   displayDuringSetup: true, defaultValue: false)
      input(name: "WarnLogging",  type: "bool", title:"Enable Warning Logging",                 displayDuringSetup: true, defaultValue: true )
      input(name: "ErrorLogging", type: "bool", title:"Enable Error Logging",                   displayDuringSetup: true, defaultValue: true )
      input(name: "InfoLogging",  type: "bool", title:"Enable Description Text (Info) Logging", displayDuringSetup: true, defaultValue: false)
    }
}

//Common Methods
void installed() { }

void initialized() { }

/*
Example CURL command and Json body:

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

int translatePosition(String pPosition) {
    //Translates a text representation of the notification position into the integer values expected by the App on the TV

    int posInt = 2;    // Default to Top Right

    switch (pPosition)
    {
        case "Top Left":
            posInt=1
            break
        case "Top Right":
            posInt=0
            break
        case "Bottom Left":
            posInt=3
            break
        case "Bottom Right":
            posInt=2
            break
        case "Centre":
            posInt=4
            break
        case "Center":
            posInt=4
            break
    }
    return posInt;
}

//Notification Methods

// Device Notification Method / Command
//  Part of the Notification Capability
//  Uses the defaults defined in the device preference settings, plus the message text passed in
void deviceNotification(String pMessageText) {

  customNotificationFull(pMessageText, DefaultDuration, DefaultPosition, DefaultTitle, DefaultTitleColor, DefaultTitleSize, DefaultMessageColor, DefaultMessageSize, DefaultBackgroundColor, DefaultImageURI, DefaultImageWidth, 0);
}

// Custom Device Notification - Title

void customNotificationTitle(String pMessageText) {
  customNotificationTitle(pMessageText,  DefaultTitle, 0);
}

void customNotificationTitle(String pMessageText, String pMessageTitle) {
  customNotificationTitle(pMessageText,  pMessageTitle, 0);
}

//   Uses the defaults defined in the device preference settings, plus the message text and title passed in
//   Debug Notification Option used to just log the notification details without sending it to the TV
void customNotificationTitle(String pMessageText, String pMessageTitle, int pdebugNotification) {

  customNotificationFull(pMessageText, DefaultDuration, DefaultPosition, pMessageTitle, DefaultTitleColor, DefaultTitleSize, DefaultMessageColor, DefaultMessageSize, DefaultBackgroundColor, DefaultImageURI, DefaultImageWidth, pdebugNotification);
}

// Custom Device Notification - Full

void customNotificationFull(String pMessageText, long pDisplayDuration, String pPosition, String pTitle, String pTitleColor, long pTitleSize, String pMessageColor, long pMessageSize, String pBackgroundColor, String pImageURI, long pImageWidth) {

  customNotificationFull(pMessageText, pDisplayDuration, pPosition, pTitle, pTitleColor, pTitleSize, pMessageColor, pMessageSize, pBackgroundColor, pImageURI, pImageWidth, 0);
}

//   Allows complete control over the notification sent
//   Ignores any defaults (apart from the TV IP Address), using whatever details are passed into the method
//   Debug Notification Option used to just log the notification details without sending it to the TV
void customNotificationFull(String pMessageText, long pDisplayDuration, String pPosition, String pTitle, String pTitleColor, long pTitleSize, String pMessageColor, long pMessageSize, String pBackgroundColor, String pImageURI, long pImageWidth, int pdebugNotification) {
  
  // Checks
  if (pMessageText == "") { errorLog("customNotificationFull: Error - Empty Message Provided");
                            return;
                          } 
  if (TVIPAddress == "")  { errorLog("customNotificationFull: Error - No TV IP Address Configured, please set this in the device Preference Settings");
                            return;
                          }
  if (Port <= 0)          { errorLog("customNotificationFull: Error - TV Port not specified or negative value provided, please configure a positive value, such as 7979");
                            return;
                          }
  
  // Adjust / Translate any relevant information

  // Translate the text-based position passed in to the appropriate integer value
  int vPosNum = translatePosition(pPosition);
  // If the duration is for some reason less than zero, set it to 10 seconds
  int vDuration = (pDisplayDuration <= 0 ? 10 : pDisplayDuration);
  // Construct the media (image) section, if an image URI has been configured
  String vMediaOutput = "";
  if(pImageURI != "" && pImageURI != null) { vMediaOutput = ", \"media\": { \"image\": {\"uri\": \"${pImageURI}\", \"width\": ${pImageWidth} } }" };

  // Prepare the HTTP Details

  def headers = [:];
  headers.put("Content-Type", "application/json");
  def bodyJson = "{\"duration\": ${vDuration}, \"position\": ${vPosNum}, \"title\": \"${URLEncoder.encode(pTitle)}\", \"titleColor\": \"${pTitleColor}\", \"titleSize\": ${pTitleSize}, \"message\": \"${URLEncoder.encode(pMessageText)}\", \"messageColor\": \"${pMessageColor}\", \"messageSize\": ${pMessageSize}, \"backgroundColor\": \"${pBackgroundColor}\" ${vMediaOutput} }";
  def postParams = [ 
        uri: "http://${TVIPAddress}:${Port}/notify",
        headers: headers,
        contentType: 'application/json',
        body : bodyJson
	]

  // Log Notification

  if (DebugLogging || pDebugNotification) {
    // Log the Notification for debugging purposes
    log.debug("customNotificationFull: Debug Notification Only, this will not be sent to the TV");
    log.debug("customNotificationFull: HTTP POST Parameters: ${postParams}");
  }

  // Send the Notification
  //   Only if we are not creating a debug notification
  if (!pDebugNotification) {
    // Send the Notification to the TV
    try {
      httpPost(postParams)
      { resp -> 
          debugLog("customNotificationFull: Reponse from TV - ${resp.data}")
      }
    }
    catch (Exception e) {
      errorLog("customNotificationFull: Error - Unable to send notification: ${e}")
    }
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