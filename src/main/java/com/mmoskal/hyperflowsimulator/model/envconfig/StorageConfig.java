package com.mmoskal.hyperflowsimulator.model.envconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageConfig {

    @JsonProperty("capacity")
    private Long capacity;

    @JsonProperty("bandwidth")
    private Long bandwidth;

    @JsonProperty("latency")
    private Double latency;

}
