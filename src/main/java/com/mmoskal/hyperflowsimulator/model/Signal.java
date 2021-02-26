package com.mmoskal.hyperflowsimulator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Signal {
    @JsonProperty("name")
    String name;

    @JsonProperty("size")
    String size;
}
