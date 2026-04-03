package com.fantasycloud.fantasyskilltrees.integration;

public interface PluginIntegration {
    String getId();

    boolean isHooked();

    void initialize();

    void shutdown();
}
