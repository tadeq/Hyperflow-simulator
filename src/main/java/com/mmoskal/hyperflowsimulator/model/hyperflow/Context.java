package com.mmoskal.hyperflowsimulator.model.hyperflow;

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
    Long appId;

    @JsonProperty("procId")
    Long procId;

    @JsonProperty("firingId")
    Long firingId;

    @JsonProperty("taskId")
    String taskId;
}
