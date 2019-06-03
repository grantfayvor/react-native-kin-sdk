package com.reactlibrary;

public class KinConfig {

    private String appId;
    private String environment;

    public KinConfig(String appId, String environment) {
        this.appId = appId;
        this.environment = environment;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "KinConfig{" +
                "appId='" + appId + '\'' +
                ", environment='" + environment + '\'' +
                '}';
    }
}
