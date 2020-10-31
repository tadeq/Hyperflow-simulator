package com.mmoskal.hyperflowsimulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class Config {

    @JsonProperty("executor")
    Executor executor;

    @JsonProperty("name")
    String name;

    @JsonProperty("hfId")
    String hfId;

    @JsonProperty("appId")
    int appId;

    @JsonProperty("procId")
    int procId;

    @JsonProperty("firingId")
    int firingId;

    @JsonProperty("taskId")
    String taskId;
}
