package org.mobicents.servlet.restcomm.rvd.configuration;

/**
 * Model class for storing restcomm configuration options. Initially used for putting host/port info
 * @author "Tsakiridis Orestis"
 *
 */
public class RestcommConfig {

    String host;
    int port;

    public RestcommConfig(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }

    public RestcommConfig() {
        // TODO Auto-generated constructor stub
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }



}
