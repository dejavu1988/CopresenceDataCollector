# Data Specification #

### Format ###

**CSV**

* meta info (1st line): <SampleID\>#<DeviceID\>#<Groundtruth\>#<CommentInfo\>
* scan result: <Timestamp\>;<SensorTypeMask\>;<SensingData\> 

**WAV**

* raw audio recording: PCM 16bit, 44100Hz

**Note**: 

* Groundtruth: 1 - copresence, 3 - non-copresence
* Timestamp: in ms
* SensorTypeMask: bitwise mask for each sensor type
* SensingData: scanned sensor attributes with delimiter "\#"

### Sensors and SensingData ###

Onboard sensors:

|    | Onboard Sensors           | SensorType Mask (bitwise) | SensingData                             |
|----|---------------------------|---------------------------|-----------------------------------------|
| 1  | GPS satellites-in-view    | 1                         | PRNs#SNRs                               |
| 2  | WiFi AP info              | 2                         | BSSID#SSID#capabilities#frequency#level |
| 3  | Bluetooth inquiry         | 4                         | MAC#name#RSSI                           |
| 4  | audio recording           | 8                         | wav file name                           |
| 5  | cell(GSM) stations        | 32                        | CellID#RadioType#LAC#PSC#RSSI           |
| 6  | ARP query                 | 64                        | IpAddr#MAC                              |
| 7  | ambient geomagnetic field | 128                       | x#y#z(uT)                               |
| 8  | ambient illumination      | 256                       | illuminance(lux)                        |
| 9  | ambient temperature       | 512                       | temperature(celcius degree)             |
| 10 | relative humidity         | 1024                      | relative humidity(%)                    |
| 11 | air pressure              | 2048                      | pressure(hPa)                           |
| 12 | GPS coordinate info       | 16                        | longitude#latitude#altitude#accuracy    |

Sensordrone:

|    | Sensordrone Sensors                                               | SensorType Mask (bitwise) | SensingData                                                                                |
|----|-------------------------------------------------------------------|---------------------------|--------------------------------------------------------------------------------------------|
| 1  | ambient temperature                                               | SD1                       | (celcius degree)                                                                           |
| 2  | relative humidity                                                 | SD2                       | (%)                                                                                        |
| 3  | air pressure                                                      | SD4                       | (Pa)                                                                                       |
| 4  | object temperature (IR)                                           | SD8                       | (celcius degree)                                                                           |
| 5  | color properties and illumination (calculated from RGBC channels) | SD16                      | illuminance(lux)#color temperature(K)#Red channel#Green channel#Blue channel#Clear channel |
| 6  | precision gas                                                     | SD32                      | (ppmCO)                                                                                    |
| 7  | reducing gas                                                      | SD64                      | (Ohm)                                                                                      |
| 8  | oxidizing gas                                                     | SD128                     | (Ohm)                                                                                      |
| 9  | proximity (capacitance)                                           | SD256                     | (fF)                                                                                       |
| 10 | altitude(calculated from pressure)                                | SD512                     | (m)                                                                                        |

