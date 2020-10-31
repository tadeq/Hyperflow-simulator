package com.mmoskal.hyperflowsimulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class Executor {

    @JsonProperty("executable")
    private String executable;

    @JsonProperty("args")
    private List<String> args;
}
