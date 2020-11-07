package com.mmoskal.hyperflowsimulator.service;

import com.mmoskal.hyperflowsimulator.model.Environment;
import lombok.experimental.UtilityClass;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.resources.Bandwidth;
import org.cloudbus.cloudsim.resources.Ram;
import org.cloudbus.cloudsim.vms.Vm;

import java.util.List;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class EnvironmentUtilizationService {

    public void addHostResourceUtilizationByVmListener(Environment environment) {
        environment.getCloudsim().addOnClockTickListener(eventInfo -> {
            for (Host host : environment.getDatacenter().getHostList()) {
                System.out.printf("Host %-2d Time: %.0f%n", host.getId(), eventInfo.getTime());
                for (Vm vm : host.getVmList()) {
                    System.out.printf(
                            "\tVm %2d: Host's CPU utilization: %5.0f%% | Host's RAM utilization: %5.0f%% | Host's BW utilization: %5.0f%%%n",
                            vm.getId(), vm.getHostCpuUtilization() * 100, vm.getHostRamUtilization() * 100, vm.getHostBwUtilization() * 100);
                }
                System.out.printf(
                        "Host %-2d Total Utilization:         %5.0f%% |                         %5.0f%% |                        %5.0f%%%n%n",
                        host.getId(), host.getCpuPercentUtilization() * 100,
                        host.getRam().getPercentUtilization() * 100,
                        host.getBw().getPercentUtilization() * 100);
            }
        });
    }

    public void addVmsUtilizationHistoryListener(Environment environment) {
        environment.getCloudsim().addOnClockTickListener(eventInfo -> {
            for (Vm vm : environment.getVms()) {
                final Map<Double, Double> vmBandUtilizationHistory = environment.getVmsBandwidthUtilizationHistory().get(vm);
                vmBandUtilizationHistory.put(environment.getCloudsim().clock(), vm.getResource(Bandwidth.class).getPercentUtilization());
                final Map<Double, Double> vmRamUtilizationHistory = environment.getVmsRamUtilizationHistory().get(vm);
                vmRamUtilizationHistory.put(environment.getCloudsim().clock(), vm.getResource(Ram.class).getPercentUtilization());
            }
        });
    }

    public void showVmsUtilizationHistory(Environment environment) {
        environment.getVms().forEach(vm -> {
            System.out.println(vm + " RAM and BW utilization history");
            System.out.println("----------------------------------------------------------------------------------");

            //A set containing all resource utilization collected times
            final Set<Double> timeSet = environment.getVmsRamUtilizationHistory().get(vm).keySet();

            final Map<Double, Double> vmRamUtilization = environment.getVmsRamUtilizationHistory().get(vm);
            final Map<Double, Double> vmBwUtilization = environment.getVmsBandwidthUtilizationHistory().get(vm);

            for (final double time : timeSet) {
                System.out.printf(
                        "Time: %10.1f secs | RAM Utilization: %10.2f%% | BW Utilization: %10.2f%%%n",
                        time, vmRamUtilization.get(time) * 100, vmBwUtilization.get(time) * 100);
            }

            System.out.printf("----------------------------------------------------------------------------------%n%n");
        });
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
