package com.mmoskal.hyperflowsimulator.service;

import com.mmoskal.hyperflowsimulator.model.Environment;
import com.mmoskal.hyperflowsimulator.model.envconfig.EnvironmentConfig;
import com.mmoskal.hyperflowsimulator.model.envconfig.HostConfig;
import com.mmoskal.hyperflowsimulator.model.envconfig.StorageConfig;
import com.mmoskal.hyperflowsimulator.model.envconfig.VmConfig;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class EnvironmentConfigMapperTest {

    public void shouldCreateEnvironmentFromConfig() {
        //TODO not finished
        StorageConfig storageConfig = new StorageConfig();
        storageConfig.setBandwidth(1000000L);
        storageConfig.setCapacity(5000L);
        storageConfig.setLatency(0.1);
        HostConfig hostConfig = new HostConfig();
        hostConfig.setBw(64000L);
        hostConfig.setRam(16000L);
        hostConfig.setStorage(32000L);
        hostConfig.setPesMipsCapacities(List.of(2500L, 2500L, 2500L, 2500L, 2500L, 2500L, 2500L, 2500L));
        VmConfig vmConfig = new VmConfig();
        vmConfig.setBw(1250L);
        vmConfig.setRam(4000L);
        vmConfig.setSize(1000L);
        vmConfig.setMipsCapacity(2500L);
        vmConfig.setNumberOfPes(8L);
        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        environmentConfig.setStorage(storageConfig);
        environmentConfig.setHosts(List.of(hostConfig));
        environmentConfig.setVms(List.of(vmConfig));
        Environment environment = EnvironmentConfigMapper.mapToEnvironment(environmentConfig);
    }
}
