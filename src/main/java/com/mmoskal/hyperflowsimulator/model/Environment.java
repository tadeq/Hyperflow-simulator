package com.mmoskal.hyperflowsimulator.model;

import com.mmoskal.hyperflowsimulator.service.EnvironmentUtilizationService;
import lombok.Getter;
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

@Getter
public class Environment {

    private CloudSim cloudsim;
    private DatacenterBroker broker;
    private Datacenter datacenter;
    private List<Host> hosts;
    private List<Vm> vms;
    private Map<Vm, Map<Double, Double>> ramUtilizationHistoryByVm;
    private Map<Vm, Map<Double, Double>> bandwidthUtilizationHistoryByVm;
    private Map<Host, Map<Vm, Map<Double, Utilization>>> utilizationHistoryByVmByHost;
    private Map<Host, Map<Double, Utilization>> utilizationHistoryByHost;

    public Environment withHostResourceUtilizationByVmListener() {
        EnvironmentUtilizationService.addHostResourceUtilizationByVmListener(this);
        return this;
    }

    public Environment withVmsUtilizationHistoryListener() {
        EnvironmentUtilizationService.addVmsUtilizationHistoryListener(this);
        return this;
    }

    @UtilityClass
    public class Creator {
        public Environment getSimpleConfiguration() {
            Environment environment = new Environment();
            environment.cloudsim = new CloudSim();
            environment.broker = new DatacenterBrokerSimple(environment.cloudsim);

            List<Pe> pes1 = List.of(
                    new PeSimple(2500),
                    new PeSimple(2500),
                    new PeSimple(2500),
                    new PeSimple(2500),
                    new PeSimple(2500),
                    new PeSimple(2500),
                    new PeSimple(2500),
                    new PeSimple(2500)
            );
            Host host1 = new HostSimple(16_000, 5_000, 128_000, pes1)
                    .setBwProvisioner(new ResourceProvisionerSimple())
                    .setRamProvisioner(new ResourceProvisionerSimple())
                    .setVmScheduler(new VmSchedulerTimeShared());
            host1.enableStateHistory();
            List<Pe> pes2 = List.of(
                    new PeSimple(2000),
                    new PeSimple(2000)
            );
            Host host2 = new HostSimple(16_000, 64_000, 32_000, pes2)
                    .setBwProvisioner(new ResourceProvisionerSimple())
                    .setRamProvisioner(new ResourceProvisionerSimple())
                    .setVmScheduler(new VmSchedulerTimeShared());
            host2.enableStateHistory();
//            environment.hosts = List.of(host1, host2);
            environment.hosts = List.of(host1);

            FileStorage storage = new SanStorage(1000000, 5000, 0.01); //capacity in MBs, bandwidth in Mb/s
            DatacenterStorage datacenterStorage = new DatacenterStorage(List.of(storage));
            environment.datacenter = new DatacenterSimple(environment.cloudsim, environment.hosts, new VmAllocationPolicyBestFit(), datacenterStorage);
            environment.vms = List.of(new VmSimple(2500, 8)
                    .setBw(1250)
                    .setRam(4000)
                    .setSize(1000)
                    .setCloudletScheduler(new CloudletSchedulerSpaceShared()));
//            environment.vms = List.of(new VmSimple(500, 2)
//                            .setBw(8000)
//                            .setRam(8000)
//                            .setSize(1000)
//                            .setCloudletScheduler(new CloudletSchedulerSpaceShared()),
//                    new VmSimple(1000, 4)
//                            .setBw(16000)
//                            .setRam(12000)
//                            .setSize(4000)
//                            .setCloudletScheduler(new CloudletSchedulerSpaceShared()));
            environment.vms.forEach(vm -> vm.getUtilizationHistory().enable());

            environment.broker.submitVmList(environment.vms);

            initializeUtilizationMaps(environment);

            return environment;
        }

        private void initializeUtilizationMaps(Environment environment) {
            environment.ramUtilizationHistoryByVm = initializeVmOneResourceUtilizationMap(environment.vms);
            environment.bandwidthUtilizationHistoryByVm = initializeVmOneResourceUtilizationMap(environment.vms);
            environment.utilizationHistoryByHost = environment.hosts.stream()
                    .collect(Collectors.toMap(host -> host, host -> new TreeMap<>()));
            environment.utilizationHistoryByVmByHost = environment.hosts.stream()
                    .collect(Collectors.toMap(host -> host, host -> new TreeMap<>()));
        }

        private Map<Vm, Map<Double, Double>> initializeVmOneResourceUtilizationMap(List<Vm> vms) {
            return vms.stream()
                    .collect(Collectors.toMap(vm -> vm, vm -> new TreeMap<>()));
        }
    }

}
