package no.ntnu.network.client.clientinfo;

import java.io.PrintWriter;

public class SensorActuatorClientInfo extends BaseClientInfo {
    private final int actuatorId;
    private double sensorValue;
    private boolean isOn;
    private final String sensorType;
    private final String actuatorType;


    public SensorActuatorClientInfo(int nodeId,int actuatorId,String actuatorType,  boolean isOn, String sensorType, double sensorValue, String clientAddress, int clientPort, PrintWriter clientWriter) {
        super(nodeId, clientAddress, clientPort, clientWriter);
        this.actuatorId = actuatorId;
        this.sensorValue = sensorValue;
        this.isOn = isOn;
        this.sensorType = sensorType;
        this.actuatorType = actuatorType;
    }

    public int getActuatorId() {
        return actuatorId;
    }
    public double getSensorValue() {
        return sensorValue;
    }
    public boolean getIsOn() {
        return isOn;
    }
    public String getSensorType() {
        return sensorType;
    }

    public String getActuatorType() {
        return actuatorType;
    }
    public void setOn(boolean isOn) {
        this.isOn = isOn;
    }
    public void setSensorValue(double sensorValue) {
        this.sensorValue = sensorValue;
    }
}