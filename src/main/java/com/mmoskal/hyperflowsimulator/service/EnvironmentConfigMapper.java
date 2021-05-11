package com.mmoskal.hyperflowsimulator.service;

import com.mmoskal.hyperflowsimulator.model.Environment;
import com.mmoskal.hyperflowsimulator.model.Utilization;
import com.mmoskal.hyperflowsimulator.model.envconfig.EnvironmentConfig;
import com.mmoskal.hyperflowsimulator.model.envconfig.HostConfig;
import com.mmoskal.hyperflowsimulator.model.envconfig.VmConfig;
import lombok.experimental.UtilityClass;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple;
import org.cloudbus.cloudsim.resources.*;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@UtilityClass
public class EnvironmentConfigMapper {

    public Environment mapToEnvironment(EnvironmentConfig environmentConfig) {
        CloudSim cloudsim = new CloudSim();
        DatacenterBroker datacenterBroker = new DatacenterBrokerSimple(cloudsim);
        FileStorage storage = new SanStorage(1000000, 5000, 0.01); //capacity in MBs, bandwidth in Mb/s
        DatacenterStorage datacenterStorage = new DatacenterStorage(List.of(storage));
        List<Host> hosts = environmentConfig.getHosts()
                .stream()
                .map(EnvironmentConfigMapper::mapToHost)
                .collect(Collectors.toList());
        Datacenter datacenter = new DatacenterSimple(cloudsim, hosts, new VmAllocationPolicyBestFit(), datacenterStorage);
        List<Vm> vms = environmentConfig.getVms().stream()
                .map(EnvironmentConfigMapper::mapToVm)
                .collect(Collectors.toList());
        vms.forEach(vm -> vm.getUtilizationHistory().enable());
        datacenterBroker.submitVmList(vms);

        return Environment.builder()
                .cloudsim(cloudsim)
                .broker(datacenterBroker)
                .hosts(hosts)
                .datacenter(datacenter)
                .vms(vms)
                .bandwidthUtilizationHistoryByVm(initializeVmOneResourceUtilizationMap(vms))
                .cpuUtilizationHistoryByVm(initializeVmOneResourceUtilizationMap(vms))
                .ramUtilizationHistoryByVm(initializeVmOneResourceUtilizationMap(vms))
                .utilizationHistoryByHost(initializeUtilizationHistoryByHostMap(hosts))
                .utilizationHistoryByVmByHost(initializeUtilizationHistoryByVmByHostMap(hosts))
                .build()
                .withHostResourceUtilizationByVmListener()
                .withVmsUtilizationHistoryListener();
    }

    private Host mapToHost(HostConfig hostConfig) {
        List<Pe> pes = hostConfig.getPesMipsCapacities().stream()
                .map(PeSimple::new)
                .collect(Collectors.toList());
        Host host = new HostSimple(hostConfig.getRam(), hostConfig.getBw(), hostConfig.getStorage(), pes)
                .setBwProvisioner(new ResourceProvisionerSimple())
                .setRamProvisioner(new ResourceProvisionerSimple())
                .setVmScheduler(new VmSchedulerTimeShared());
        host.enableStateHistory();
        return host;
    }

    private Vm mapToVm(VmConfig vmConfig) {
        return new VmSimple(vmConfig.getMipsCapacity(), vmConfig.getNumberOfPes())
                .setBw(vmConfig.getBw())
                .setRam(vmConfig.getRam())
                .setSize(vmConfig.getSize())
                .setCloudletScheduler(new CloudletSchedulerSpaceShared());
    }

    private Map<Vm, Map<Double, Double>> initializeVmOneResourceUtilizationMap(List<Vm> vms) {
        return vms.stream()
                .collect(Collectors.toMap(vm -> vm, vm -> new TreeMap<>()));
    }

    private Map<Host, Map<Double, Utilization>> initializeUtilizationHistoryByHostMap(List<Host> hosts) {
        return hosts.stream()
                .collect(Collectors.toMap(host -> host, host -> new TreeMap<>()));
    }

    private Map<Host, Map<Vm, Map<Double, Utilization>>> initializeUtilizationHistoryByVmByHostMap(List<Host> hosts) {
        return hosts.stream()
                .collect(Collectors.toMap(host -> host, host -> new TreeMap<>()));
    }

}
