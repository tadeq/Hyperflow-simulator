package com.mmoskal.hyperflowsimulator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmoskal.hyperflowsimulator.client.RedisTaskResolveClient;
import com.mmoskal.hyperflowsimulator.model.Config;
import com.mmoskal.hyperflowsimulator.model.Environment;
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
import org.cloudbus.cloudsim.resources.File;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SimulationService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final RedisTaskResolveClient redisClient;

    private Environment environment;

    @Autowired
    public SimulationService(RedisTaskResolveClient redisClient) {
        this.redisClient = redisClient;
        initEnvironment();
    }

    private void initEnvironment() {
        this.environment = Environment.Creator.getSimpleConfiguration()
                .withHostResourceUtilizationByVmListener()
                .withVmsUtilizationHistoryListener();
    }

    public void addTask(String config) {
        try {
            environment.getBroker().submitCloudlet(toCloudletWithOnFinishListener(OBJECT_MAPPER.readValue(config, Config.class)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void runSimulation() {
        environment.getCloudsim().start();
        new CloudletsTableBuilder(environment.getBroker().getCloudletFinishedList()).build();
        EnvironmentUtilizationService.showVmsUtilizationHistory(environment);
        EnvironmentUtilizationService.showHostResourceUtilizationByVm(environment);
        EnvironmentUtilizationService.showCpuUtilizationForHosts(environment.getHosts());
        EnvironmentUtilizationService.showCpuUtilizationForVms(environment.getVms());
        System.out.println("--------------------END OF SIMULATION--------------------");
        initEnvironment();
    }

    private Cloudlet toCloudletWithOnFinishListener(Config config) {
        UtilizationModelDynamic utilizationModel = new UtilizationModelDynamic(40);
        Cloudlet cloudlet = new CloudletSimple((int) Math.round(config.getContext().getExecutor().getInstructions() / 1000000.0), 1, utilizationModel);
        List<File> inFiles = config.getIns().stream()
                .map(in -> Optional.ofNullable(in.getSize())
                        .map(size -> (int) Math.round(size))
                        .filter(size -> size > 0)
                        .map(size -> new File(in.getName(), size)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        inFiles.forEach(file -> {
            environment.getDatacenter().getDatacenterStorage().addFile(file);
            cloudlet.addRequiredFile(file.getName());
        });
        cloudlet.addOnFinishListener(eventInfo -> {
            redisClient.resolveTask(config.getContext().getAppId(), config.getContext().getTaskId());
            environment.getCloudsim().pause();
            System.out.println("SUBMITTED: " + environment.getBroker().getCloudletSubmittedList().size());
            System.out.println("FINISHED: " + environment.getBroker().getCloudletFinishedList().size());
            waitForCloudlets();
            environment.getCloudsim().resume();
        });
        return cloudlet;
    }

    private void waitForCloudlets() {
        for (int i = 0; i < 4; i++) {
            int submittedCloudlets = environment.getBroker().getCloudletSubmittedList().size();
            int finishedCloudlets = environment.getBroker().getCloudletFinishedList().size();
            long availablePes = environment.getHosts().stream()
                    .filter(Host::isActive)
                    .map(Host::getPeList)
                    .mapToLong(Collection::size)
                    .sum();
            if (submittedCloudlets - finishedCloudlets - availablePes <= 0) {
                System.out.println("WAITING FOR CLOUDLETS");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
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
