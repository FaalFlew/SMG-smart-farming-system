# Communication protocol

This document describes the protocol used for communication between the different nodes of the
distributed application.

## Terminology

* Sensor - a device which senses the environment and describes it with a value (an integer value in
  the context of this project). Examples: temperature sensor, humidity sensor.
* Actuator - a device which can influence the environment. Examples: a fan, a window opener/closer,
  door opener/closer, heater.
* Sensor and actuator node - a computer which has direct access to a set of sensors, a set of
  actuators and is connected to the Internet.
* Control-panel node - a device connected to the Internet which visualizes status of sensor and
  actuator nodes and sends control commands to them.
* Graphical User Interface (GUI) - A graphical interface where users of the system can interact with
  it.

## The underlying transport protocol
We opt for the TCP transport-layer protocol due to its reliability. This choice is driven by the protocol's ability to incorporate error-checking mechanisms and facilitate the retransmission of lost packets; which could lead to a suboptimal control of the farming enviornment.


TCP efficiently manages data transmission rates, preventing congestion. It establishes a persistent connection, crucial for ongoing interactions. UDP offers faster transmission but lacks data reliability. A Hybrid solution is most optimal.

## The architecture

TODO - show the general architecture of your network. Which part is a server? Who are clients? 
Do you have one or several servers? Perhaps include a picture here. 

**Server**: Central server managing communication between control-panel nodes and sensor/actuator nodes.

**Clients**: Control-panel node clients and sensor/actuator node clients.


| ![Network architecture](https://github.com/FaalFlew/SMG-smart-farming-system/assets/126261797/2078438a-53ac-428d-88fc-3b27a9640a75) |
|:--:|
| *The network architecture* |

## The flow of information and events

TODO - describe what each network node does and when. Some periodic events? Some reaction on 
incoming packets? Perhaps split into several subsections, where each subsection describes one 
node type (For example: one subsection for sensor/actuator nodes, one for control panel nodes).

### Sensor/Actuator Nodes
**client listener**: listens for incoming server messages and handles them appropriately. For example, the server sends a command with a type attribute of value "shutdown", the client knows the server has or is shutting down and so the client closes its socket and printewriter. 
**First connection**: when a client connects for the first time, they will receive a list of all the connecteed clients of its type. The client can request this list in a form of a command at a later point if they wish. the clients also get a list of all available commands that they can utizlize and send to the server.

**Incoming Packets**: React to control commands from the server.

### Control-panel Nodes
**Periodic Events**: Update GUI with sensor data.

**Incoming Packets**: Receive sensor data, send control commands.

## Connection and state

Our communication protocol is connection-oriented, and stateless. This makes it more scalable and reliable

**Connection-Oriented**: Protocol establishes and maintains connections.

**Stateless**: Scalable and reliable without relying on stored connection states.

## Types, constants

TODO - Do you have some specific value types you use in several messages? They you can describe 
them here.

## Message format

TODO - describe the general format of all messages. Then describe specific format for each 
message type in your protocol.

We use JSON as the message format, this is how clients and servers receive and send messages and command to each other.

commands can be sent to server using the attribue **type** where the value of the attribute is the command.

**Commands:** 
- **all_sensors**: This command retrieves a list of all sensoror actuator node clients connected to the server.
- **all_control_panels**: This command retrieves a list of all control panel node clients connected to the server.
- **command_to_sensor_actuator**: This command turns an actuator on or off, Example usage: {"type":"command_to_control_panel","nodeid":"4", "ison":false} 

### Case sensitivity
Commands are not case sensitive and spaces are trimmed from the message, meaning {"TyPE":"aLL_ SenSORs"} is interpreted as  {"type":"all_sensors"}


### Warnings

When the server is about to shut down it gives a warning to all connected clients that it is shutting down. It closes the sockets of all the connected clients, and their printwriter.
The clients listen for a message that contains the command **shutdown** from the server, if so; it handles it correctly.

### When client connects

when a client connects to the server, the server loggs the the type of client that has connected, for example  **CONTROL_PANEL** or **SENSOR_ACTUATOR**

When a client connects, the client gets a logg list of all the connected other clients of its type alongside all the properties.
The client further gets a list of all available commands that can be sent to the server

### When client disconnects
The server loggs the client type and node id of the disconnected client handles it properly by removing it from any relevant lists where it is stored or information regarding that client is stored.

### Error messages

TODO - describe the possible error messages that nodes can send in your system.

**Possible Errors**: Connection failure, invalid command, sensor failure.
- **Connection failure**: If the server is not runing and a client is not able to connect
- **Invalid command**: If message/command type is unknown an error is logged: "Unknown message type"
- **client input validation**: at the client level, the client has to write messages in proper JSON format, otherwise an error will be logged to the client indicating invalid JSON format. the error can vary depending depending on the input, but it is overal a parsing error "Error parsing JSON:" + e.getMessage(), "Invalid JSON format. Please enter a valid JSON message."... and similar errors
- **sending messages to other clients**: "Client with nodeId " + nodeId + " not found"
- **When the server fails to start**: "Error starting the server: " e.getMessage()
- **Client fails close a socket**: "Failed to close socket: " + e.getMessage()
- **Client fails establish a socket**: "Failed to create a socket: " + e.getMessage()
- **Client fails to create writer**: "Error initializing writer: " + e.getMessage()
- **Server fails sending shutdown message to clients**: "Error sending shutdown message to client: " + e.getMessage()
- **If message/command retrieved from client is unknown type**  "Unknown message type: " + messageType
- **When the server fails to handle message type**: "Error handling message type: " + e.getMessage()

**Error Format**: Include error code and description in error messages.
We use the logger class to logg errors. The error message are not fixed.

## An example scenario

TODO - describe a typical scenario. How would it look like from communication perspective? When 
are connections established? Which packets are sent? How do nodes react on the packets? An 
example scenario could be as follows:
1. A sensor node with ID=1 is started. It has a temperature sensor, and a heater sensors. It can
   also turn on the heater.
2. A sensor node with ID=2 is started. It has a single temperature sensor and can control two fans
   and a heater.
3. A control panel node is started.
4. Another control panel node is started.
6. Any control panel can retrieve all sensor data
7. The user of the first-control panel sends a command to the sesnsor which allows him to turn the actuator for that sensor ON.


## Reliability and security

TODO - describe the reliability and security mechanisms your solution supports.

**Reliability Mechanisms**: TCP's inherent error-checking and retransmission.

**Security Mechanisms**: Data encryption for sensitive information exchange. Additional security protocols can be implemented as needed.
