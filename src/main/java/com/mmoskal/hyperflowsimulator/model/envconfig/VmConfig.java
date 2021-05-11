package com.mmoskal.hyperflowsimulator.model.envconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VmConfig {

    @JsonProperty("mipsCapacity")
    private Long mipsCapacity;

    @JsonProperty("numberOfPes")
    private Long numberOfPes;

    @JsonProperty("bw")
    private Long bw;

    @JsonProperty("ram")
    private Long ram;

    @JsonProperty("size")
    private Long size;
}
