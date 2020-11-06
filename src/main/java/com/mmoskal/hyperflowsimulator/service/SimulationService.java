package com.mmoskal.hyperflowsimulator.service;

import com.mmoskal.hyperflowsimulator.model.Config;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    private final List<Config> configs = new ArrayList<>();

    public void addConfig(Config config) {
        configs.add(config);
    }

    public void runSimulation() {
        CloudSim cloudsim = new CloudSim();
        DatacenterBroker broker = new DatacenterBrokerSimple(cloudsim);
        List<Pe> pes1 = List.of(
                new PeSimple(10000),
                new PeSimple(10000),
                new PeSimple(10000),
                new PeSimple(10000)
        );
        Host host1 = new HostSimple(32_000, 128_000, 128_000, pes1);
        List<Pe> pes2 = List.of(
                new PeSimple(2000),
                new PeSimple(2000)
        );
        Host host2 = new HostSimple(16_000, 64_000, 32_000, pes2);
        Datacenter datacenter = new DatacenterSimple(cloudsim, List.of(host1, host2), new VmAllocationPolicyBestFit());
        Vm vm = new VmSimple(500, 2)
                .setBw(8000)
                .setRam(8000)
                .setSize(1000)
                .setCloudletScheduler(new CloudletSchedulerSpaceShared());

        broker.submitVm(vm);

        UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.4);
        List<Cloudlet> cloudlets = configs.stream()
                .map(config -> new CloudletSimple(25000, 1, utilizationModel))
                .collect(Collectors.toList());
        broker.submitCloudletList(cloudlets);

        cloudsim.start();

        new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();
    }

    public void runTestSimulation() {
        //Enables just some level of logging.
        //Make sure to import org.cloudsimplus.util.Log;
        //Log.setLevel(ch.qos.logback.classic.Level.WARN);

        //Creates a CloudSim object to initialize the simulation.
        CloudSim cloudsim = new CloudSim();

        /*Creates a Broker that will act on behalf of a cloud user (customer).*/
        DatacenterBroker broker0 = new DatacenterBrokerSimple(cloudsim);

        //Creates a list of Hosts, each host with a specific list of CPU cores (PEs).
        List<Host> hostList = new ArrayList<>(1);
        List<Pe> hostPes = new ArrayList<>(1);
        //Uses a PeProvisionerSimple by default to provision PEs for VMs
        hostPes.add(new PeSimple(20000));
        long ram = 10000; //in Megabytes
        long storage = 100000; //in Megabytes
        long bw = 100000; //in Megabits/s

        //Uses ResourceProvisionerSimple by default for RAM and BW provisioning
        //Uses VmSchedulerSpaceShared by default for VM scheduling
        Host host0 = new HostSimple(ram, bw, storage, hostPes);
        hostList.add(host0);

        //Creates a Datacenter with a list of Hosts.
        //Uses a VmAllocationPolicySimple by default to allocate VMs
        Datacenter dc0 = new DatacenterSimple(cloudsim, hostList, new VmAllocationPolicyBestFit());

        //Creates VMs to run applications.
        List<Vm> vmList = new ArrayList<>(1);
        //Uses a CloudletSchedulerTimeShared by default to schedule Cloudlets
        Vm vm0 = new VmSimple(1000, 1);
        vm0.setRam(1000).setBw(1000).setSize(1000);
        vmList.add(vm0);

        //Creates Cloudlets that represent applications to be run inside a VM.
        List<Cloudlet> cloudletList = new ArrayList<>();
        //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
        UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(0.5);
        Cloudlet cloudlet0 = new CloudletSimple(10000, 1, utilizationModel);
        Cloudlet cloudlet1 = new CloudletSimple(10000, 1, utilizationModel);
        cloudletList.add(cloudlet0);
        cloudletList.add(cloudlet1);

        broker0.submitVmList(vmList);
        broker0.submitCloudletList(cloudletList);

        /*Starts the simulation and waits all cloudlets to be executed, automatically
        stopping when there is no more events to process.*/
        cloudsim.start();

        /*Prints results when the simulation is over
        (you can use your own code here to print what you want from this cloudlet list).*/
        new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();
    }
}
