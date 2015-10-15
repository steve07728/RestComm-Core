package org.mobicents.servlet.restcomm.rvd.model.client;

import org.mobicents.servlet.restcomm.rvd.RvdConfiguration;

public class WorkspaceSettings {

    private String apiServerHost;
    private Integer apiServerRestPort; // null values should be allowed too
    private String apiServerUsername;
    private String apiServerPass;
    private String appStoreDomain;


    public static WorkspaceSettings createDefault() {
        WorkspaceSettings settingsModel = new WorkspaceSettings(null, null);
        settingsModel.appStoreDomain = RvdConfiguration.DEFAULT_APPSTORE_DOMAIN;
        return settingsModel;
    }

    public WorkspaceSettings(String apiServerHost, Integer apiServerRestPort) {
        super();
        this.apiServerHost = apiServerHost;
        this.apiServerRestPort = apiServerRestPort;
    }


    public WorkspaceSettings(String apiServerHost, Integer apiServerRestPort, String apiServerUsername, String apiServerPass) {
        super();
        this.apiServerHost = apiServerHost;
        this.apiServerRestPort = apiServerRestPort;
        this.apiServerUsername = apiServerUsername;
        this.apiServerPass = apiServerPass;
    }


    public String getApiServerHost() {
        return apiServerHost;
    }

    public void setApiServerHost(String apiServerHost) {
        this.apiServerHost = apiServerHost;
    }

    public Integer getApiServerRestPort() {
        return apiServerRestPort;
    }

    public void setApiServerRestPort(Integer apiServerRestPort) {
        this.apiServerRestPort = apiServerRestPort;
    }


    public String getApiServerUsername() {
        return apiServerUsername;
    }


    public void setApiServerUsername(String apiServerUsername) {
        this.apiServerUsername = apiServerUsername;
    }


    public String getApiServerPass() {
        return apiServerPass;
    }


    public void setApiServerPass(String apiServerPass) {
        this.apiServerPass = apiServerPass;
    }

    public String getAppStoreDomain() {
        return appStoreDomain;
    }

    public void setAppStoreDomain(String appStoreDomain) {
        this.appStoreDomain = appStoreDomain;
    }

}
