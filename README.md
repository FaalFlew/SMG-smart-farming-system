## To run the commandline app
1. in the networks package -> server; launch the smartFarming server.
22. in the networks package -> client; input your desired arguments before laucnhing the controlpanel node client **(MainControlPanelClass)**
  control panel node arguments:
Usage: java MainControlPanelClass <actuatorId> <nodeId> <isOn>

22. in the networks package -> client; input your desired arguments before laucnhing the sensor actuator node client **(MainSensorActuatorClass)**
   sensor actuator node arguments:
Usage: java MainSensorActuatorClass <nodeId> <actuatorId> <actuatorType> <isOn> <sensorType> <sensorValue>


the commands, protocol and more information is documented inside the protocol.md file in the **source** folder.
