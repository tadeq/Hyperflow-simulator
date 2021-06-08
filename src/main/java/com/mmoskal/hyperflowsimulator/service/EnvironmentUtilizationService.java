package com.mmoskal.hyperflowsimulator.service;

import com.mmoskal.hyperflowsimulator.model.Environment;
import com.mmoskal.hyperflowsimulator.model.Utilization;
import lombok.experimental.UtilityClass;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Processor;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.vms.Vm;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class EnvironmentUtilizationService {

    public void addHostResourceUtilizationByVmListener(Environment environment) {
        environment.getCloudsim().addOnClockTickListener(eventInfo -> {
            for (Host host : environment.getDatacenter().getHostList()) {
                for (Vm vm : host.getVmList()) {
                    Utilization utilization = Utilization.builder()
                            .cpuUtilization(vm.getHostCpuUtilization() * 100)
                            .ramUtilization(vm.getHostRamUtilization() * 100)
                            .bwUtilization(vm.getHostBwUtilization() * 100)
                            .build();
                    Map<Vm, Map<Double, Utilization>> utilizationByVm = environment.getUtilizationHistoryByVmByHost().get(host);
                    if (!utilizationByVm.containsKey(vm)) {
                        utilizationByVm.put(vm, new TreeMap<>());
                    }
                    utilizationByVm.get(vm).put(eventInfo.getTime(), utilization);
                }
                Utilization utilization = Utilization.builder()
                        .cpuUtilization(host.getCpuPercentUtilization() * 100)
                        .ramUtilization(host.getRam().getPercentUtilization() * 100)
                        .bwUtilization(host.getBw().getPercentUtilization() * 100)
                        .build();
                environment.getUtilizationHistoryByHost().get(host).put(eventInfo.getTime(), utilization);
            }
        });
    }

    public void addVmsUtilizationHistoryListener(Environment environment) {
        environment.getCloudsim().addOnClockTickListener(eventInfo -> {
            for (Vm vm : environment.getVms()) {
                final Map<Double, Double> vmBandUtilizationHistory = environment.getBandwidthUtilizationHistoryByVm().get(vm);
                vmBandUtilizationHistory.put(environment.getCloudsim().clock(), vm.getResource(Bandwidth.class).getPercentUtilization());
                final Map<Double, Double> vmRamUtilizationHistory = environment.getRamUtilizationHistoryByVm().get(vm);
                vmRamUtilizationHistory.put(environment.getCloudsim().clock(), vm.getResource(Ram.class).getPercentUtilization());
                final Map<Double, Double> vmCpuUtilizationHistory = environment.getCpuUtilizationHistoryByVm().get(vm);
                vmCpuUtilizationHistory.put(environment.getCloudsim().clock(), vm.getResource(Processor.class).getPercentUtilization());
            }
        });
    }

    public void showHostResourceUtilizationByVm(Environment environment) {
        for (Host host : environment.getHosts()) {
            Map<Double, Utilization> hostUtilizationByTime = environment.getUtilizationHistoryByHost().get(host);
            Map<Vm, Map<Double, Utilization>> vmsUtilization = environment.getUtilizationHistoryByVmByHost().get(host);
            Set<Double> timestamps = hostUtilizationByTime.keySet();
            for (Double time : timestamps) {
                System.out.printf("Host %-2d Time: %.0f%n", host.getId(), time);
                for (Vm vm : environment.getVms()) {
                    if (vmsUtilization.containsKey(vm)) {
                        Utilization vmUtilization = vmsUtilization.get(vm).get(time);
                        System.out.printf(
                                "\tVm %2d: Host's CPU utilization: %5.0f%% | Host's RAM utilization: %5.0f%% | Host's BW utilization: %5.0f%%%n",
                                vm.getId(), vmUtilization.getCpuUtilization(), vmUtilization.getRamUtilization(), vmUtilization.getBwUtilization());
                    }
                }
                Utilization hostUtilization = hostUtilizationByTime.get(time);
                System.out.printf(
                        "Host %-2d Total Utilization:         %5.0f%% |                         %5.0f%% |                        %5.0f%%%n%n",
                        host.getId(),
                        hostUtilization.getCpuUtilization(),
                        hostUtilization.getRamUtilization(),
                        hostUtilization.getBwUtilization());
            }
        }
    }

    public void saveHostResourceUtilization(Environment environment) {
        try {
            FileWriter fileWriter = new FileWriter("res_utilization.csv", false);
            fileWriter.write("host,time,cpu,ram\n");
            environment.getHosts()
                    .forEach(host -> environment.getUtilizationHistoryByHost()
                            .get(host).forEach((timestamp, utilization) -> writeCsvLine(fileWriter, host, timestamp, utilization)));
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeCsvLine(FileWriter fileWriter, Host host, Double timestamp, Utilization utilization) {
        try {
            fileWriter.write(Stream.of(
                    host.getId(), timestamp, utilization.getCpuUtilization(), utilization.getRamUtilization() + "\n"
            ).map(String::valueOf)
                    .collect(Collectors.joining(",")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showVmsUtilizationHistory(Environment environment) {
        for (Vm vm : environment.getVms()) {
            System.out.println(vm + " RAM and BW utilization history");
            System.out.println("----------------------------------------------------------------------------------");

            final Set<Double> timestamps = environment.getRamUtilizationHistoryByVm().get(vm).keySet();

            final Map<Double, Double> vmRamUtilization = environment.getRamUtilizationHistoryByVm().get(vm);
            final Map<Double, Double> vmBwUtilization = environment.getBandwidthUtilizationHistoryByVm().get(vm);
            final Map<Double, Double> vmCpuUtilization = environment.getCpuUtilizationHistoryByVm().get(vm);

            for (final double time : timestamps) {
                System.out.printf(
                        "Time: %10.1f secs | RAM Utilization: %10.2f%% | CPU Utilization: %10.2f%% | BW Utilization: %10.2f%%%n",
                        time, vmRamUtilization.get(time) * 100, vmCpuUtilization.get(time) * 100, vmBwUtilization.get(time) * 100);
            }

            System.out.printf("----------------------------------------------------------------------------------%n%n");
        }
    }


    public void showCpuUtilizationForHosts(List<Host> hosts) {
        System.out.printf("%nHosts CPU utilization history for the entire simulation period%n");
        int numberOfUsageHistoryEntries = 0;
        for (Host host : hosts) {
            double mipsByPe = host.getTotalMipsCapacity() / (double) host.getNumberOfPes();
            System.out.printf("Host %d: Number of PEs %2d, MIPS by PE %.0f%n", host.getId(), host.getNumberOfPes(), mipsByPe);
            for (Map.Entry<Double, Double> entry : host.getUtilizationHistorySum().entrySet()) {
                final double time = entry.getKey();
                final double cpuUsage = entry.getValue() * 100;
                numberOfUsageHistoryEntries++;
                System.out.printf("\tTime: %4.1f CPU Utilization: %6.2f%%%n", time, cpuUsage);
            }
            System.out.println("--------------------------------------------------");
        }

        if (numberOfUsageHistoryEntries == 0) {
            System.out.println("No CPU usage history was found");
        }
    }

    public void showCpuUtilizationForVms(List<Vm> vms) {
        System.out.printf("%nHosts CPU utilization history for the entire simulation period%n%n");
        int numberOfUsageHistoryEntries = 0;
        for (Vm vm : vms) {
            System.out.printf("VM %d%n", vm.getId());
            if (vm.getUtilizationHistory().getHistory().isEmpty()) {
                System.out.println("\tThere isn't any usage history");
                continue;
            }

            for (Map.Entry<Double, Double> entry : vm.getUtilizationHistory().getHistory().entrySet()) {
                final double time = entry.getKey();
                final double vmCpuUsage = entry.getValue() * 100;
                if (vmCpuUsage > 0) {
                    numberOfUsageHistoryEntries++;
                    System.out.printf("\tTime: %2.0f CPU Utilization: %6.2f%%%n", time, vmCpuUsage);
                }
            }
        }

        if (numberOfUsageHistoryEntries == 0) {
            System.out.println("No CPU usage history was found");
        }
    }
}
