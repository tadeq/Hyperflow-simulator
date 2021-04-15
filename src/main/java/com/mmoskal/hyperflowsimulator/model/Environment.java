package com.mmoskal.hyperflowsimulator.model;

import com.mmoskal.hyperflowsimulator.service.EnvironmentUtilizationService;
import lombok.Builder;
import lombok.Getter;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class Environment {

    private final CloudSim cloudsim;
    private final DatacenterBroker broker;
    private final Datacenter datacenter;
    private final List<Host> hosts;
    private final List<Vm> vms;
    private final Map<Vm, Map<Double, Double>> ramUtilizationHistoryByVm;
    private final Map<Vm, Map<Double, Double>> bandwidthUtilizationHistoryByVm;
    private final Map<Vm, Map<Double, Double>> cpuUtilizationHistoryByVm;
    private final Map<Host, Map<Vm, Map<Double, Utilization>>> utilizationHistoryByVmByHost;
    private final Map<Host, Map<Double, Utilization>> utilizationHistoryByHost;

    public Environment withHostResourceUtilizationByVmListener() {
        EnvironmentUtilizationService.addHostResourceUtilizationByVmListener(this);
        return this;
    }

    public Environment withVmsUtilizationHistoryListener() {
        EnvironmentUtilizationService.addVmsUtilizationHistoryListener(this);
        return this;
    }
}
