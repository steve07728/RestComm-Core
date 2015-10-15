package org.mobicents.servlet.restcomm.rvd.configuration;

public class RvdXml {
    private String workspaceLocation;
    private String restcommPublicIp;
    private String sslMode;

    public RvdXml() {
    }

    public RvdXml(String workspaceLocation, String restcommPublicIp, String sslMode) {
        super();
        this.workspaceLocation = workspaceLocation;
        this.restcommPublicIp = restcommPublicIp;
        this.sslMode = sslMode;
    }

    public String getWorkspaceLocation() {
        return workspaceLocation;
    }

    public String getRestcommPublicIp() {
        return restcommPublicIp;
    }

    public String getSslMode() {
        return sslMode;
    }

}
