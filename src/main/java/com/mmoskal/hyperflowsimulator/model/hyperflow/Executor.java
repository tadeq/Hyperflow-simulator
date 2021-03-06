package com.mmoskal.hyperflowsimulator.model.hyperflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Executor {

    @JsonProperty("executable")
    String executable;

    @JsonProperty("args")
    List<String> args;

    @JsonProperty("instructions")
    Long instructions;

    @JsonProperty("memRequest")
    Long memRequest;

    @JsonProperty("cpuRequest")
    Long cpuRequest;

    @JsonProperty("filePart")
    Double filePart;
}
