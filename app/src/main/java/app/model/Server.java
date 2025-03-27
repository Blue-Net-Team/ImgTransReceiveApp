// app/src/main/java/app/model/Server.java
package app.model;

public class Server {
    public String Ip;
    public int Port;
    public boolean isSelected; // Add this field

    // Constructor
    public Server(String ip, int port, boolean isSelected) {
        this.Ip = ip;
        this.Port = port;
        this.isSelected = isSelected;
    }

    // Getters and Setters (optional)
    public String getIp() {
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    public int getPort() {
        return Port;
    }

    public void setPort(int port) {
        Port = port;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}