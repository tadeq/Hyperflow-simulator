package com.mmoskal.hyperflowsimulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class Executor {

    @JsonProperty("executable")
    String executable;

    @JsonProperty("args")
    List<String> args;
}
