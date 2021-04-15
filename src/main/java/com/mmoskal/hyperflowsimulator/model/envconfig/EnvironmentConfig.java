package com.mmoskal.hyperflowsimulator.model.envconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvironmentConfig {

    @JsonProperty("storage")
    private StorageConfig storage;

    @JsonProperty("hosts")
    private List<HostConfig> hosts;

    @JsonProperty("vms")
    private List<VmConfig> vms;

}
