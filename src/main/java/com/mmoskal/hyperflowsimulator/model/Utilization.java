package com.mmoskal.hyperflowsimulator.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Utilization {
    double cpuUtilization;
    double ramUtilization;
    double bwUtilization;
}
