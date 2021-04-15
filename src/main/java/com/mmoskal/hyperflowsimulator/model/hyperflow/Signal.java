package com.mmoskal.hyperflowsimulator.model.hyperflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Signal {

    @JsonProperty("name")
    String name;

    @JsonProperty("size")
    Double size;
}
