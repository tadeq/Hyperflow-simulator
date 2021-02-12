package com.mmoskal.hyperflowsimulator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    @JsonProperty("context")
    Context context;

    @JsonProperty("ins")
    List<String> ins;

    @JsonProperty("outs")
    List<String> outs;
}
