package com.mmoskal.hyperflowsimulator.model.envconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostConfig {

    @JsonProperty("ram")
    private Long ram;

    @JsonProperty("bw")
    private Long bw;

    @JsonProperty("storage")
    private Long storage;

    @JsonProperty("pesMipsCapacities")
    private List<Long> pesMipsCapacities;
}
