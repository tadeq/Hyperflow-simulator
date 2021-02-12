package com.mmoskal.hyperflowsimulator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Context {
    @JsonProperty("executor")
    Executor executor;

    @JsonProperty("name")
    String name;

    @JsonProperty("hfId")
    String hfId;

    @JsonProperty("appId")
    String appId;

    @JsonProperty("procId")
    String procId;

    @JsonProperty("firingId")
    String firingId;

    @JsonProperty("taskId")
    String taskId;
}
