package com.mmoskal.hyperflowsimulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mmoskal.hyperflowsimulator.model.Environment;
import com.mmoskal.hyperflowsimulator.model.hyperflow.Config;
import lombok.experimental.UtilityClass;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@UtilityClass
public class LogGenerator {

    private ObjectMapper objectMapper = new ObjectMapper();

    public void generate(Environment environment, Map<Long, Config> jobsHistory) {
        if (jobsHistory.isEmpty()) {
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS");
        LocalDateTime startTime = LocalDateTime.now();
        List<Cloudlet> finishedCloudlets = environment.getBroker().getCloudletFinishedList();
        System.out.println(finishedCloudlets.size());
        Map<Long, Long> finishedCloudletsByVm = LongStream.range(0, environment.getVms().size())
                .boxed()
                .collect(Collectors.toMap(vmId -> vmId, vmId -> 0L));
        String directoryName = jobsHistory.values().stream()
                .findAny()
                .get()
                .getContext()
                .getHfId();
        Path jobDescriptionsPath = Path.of(directoryName, "job_descriptions.jsonl");
        Path metricsPath = Path.of(directoryName, "metrics.jsonl");
        try {
            Files.createDirectories(Path.of(directoryName));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        finishedCloudlets.forEach(cloudlet -> {
            Config config = jobsHistory.get(cloudlet.getId());
            ObjectNode mainNode = objectMapper.createObjectNode();
            LocalDateTime cloudletStartTime = startTime
                    .plusNanos(((int) Math.round(cloudlet.getExecStartTime() * 1000)) % 1000 * 1000000)
                    .plusSeconds((int) cloudlet.getExecStartTime())
                    .plusNanos(500000000L * finishedCloudletsByVm.get(cloudlet.getVm().getId()))
                    .plusSeconds(2 * finishedCloudletsByVm.get(cloudlet.getVm().getId()));
            LocalDateTime cloudletFinishTime = startTime
                    .plusNanos(((int) Math.round(cloudlet.getFinishTime() * 1000)) % 1000 * 1000000)
                    .plusSeconds((int) cloudlet.getFinishTime())
                    .plusNanos(500000000L * finishedCloudletsByVm.get(cloudlet.getVm().getId()))
                    .plusSeconds(2 * finishedCloudletsByVm.get(cloudlet.getVm().getId()));
            mainNode.put("time", cloudletStartTime.format(formatter));
            mainNode.put("pid", String.valueOf(cloudlet.getVm().getHost().getDatacenter().getId())
                    + cloudlet.getVm().getHost().getId());
            mainNode.put("workflowId", config.getContext().getHfId() + config.getContext().getAppId());
            mainNode.put("jobId", config.getContext().getHfId() + config.getContext().getAppId() + config.getContext().getProcId());
            mainNode.put("name", config.getContext().getName());
            mainNode.put("parameter", "event");
            mainNode.put("value", "handlerStart");
            try {
                Files.writeString(metricsPath, mainNode.toString() + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                mainNode.put("value", "jobStart");
                Files.writeString(metricsPath, mainNode.toString() + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                mainNode.put("time", cloudletFinishTime.format(formatter));
                mainNode.put("value", "jobEnd");
                Files.writeString(metricsPath, mainNode.toString() + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                mainNode.put("value", "handlerEnd");
                Files.writeString(metricsPath, mainNode.toString() + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ObjectNode jobDescriptionsMainNode = objectMapper.createObjectNode();
            jobDescriptionsMainNode.put("workflowName", "Simulation " + config.getContext().getHfId());
            jobDescriptionsMainNode.put("size", "");
            jobDescriptionsMainNode.put("version", "");
            jobDescriptionsMainNode.put("hyperflowId", config.getContext().getHfId() + config.getContext().getAppId());
            jobDescriptionsMainNode.put("jobId", config.getContext().getHfId() + config.getContext().getAppId() + config.getContext().getProcId());
            jobDescriptionsMainNode.put("nodeName", String.valueOf(cloudlet.getVm().getHost().getDatacenter().getId())
                    + cloudlet.getVm().getHost().getId());
            jobDescriptionsMainNode.put("executable", config.getContext().getName());
            ArrayNode argsArray = jobDescriptionsMainNode.putArray("args");
            config.getContext().getExecutor().getArgs().forEach(argsArray::add);
            ArrayNode inputsNode = jobDescriptionsMainNode.putArray("inputs");
            config.getIns().forEach(in -> {
                ObjectNode signalNode = objectMapper.createObjectNode();
                signalNode.put("name", in.getName());
                signalNode.put("size", Optional.ofNullable(in.getSize()).map(size -> size*1000000).orElse(0.0));
                inputsNode.add(signalNode);
            });
            ArrayNode outputsNode = jobDescriptionsMainNode.putArray("outputs");
              config.getOuts().forEach(out -> {
                ObjectNode signalNode = objectMapper.createObjectNode();
                signalNode.put("name", out.getName());
                signalNode.put("size", Optional.ofNullable(out.getSize()).map(size -> size*1000000).orElse(0.0));
                outputsNode.add(signalNode);
            });
            jobDescriptionsMainNode.put("name", config.getContext().getName());
            jobDescriptionsMainNode.put("command", config.getContext().getName() + " "
                    + String.join(" ", config.getContext().getExecutor().getArgs()));
            ZonedDateTime zonedCloudletFinishTime = ZonedDateTime.of(cloudletFinishTime, ZoneId.systemDefault());
            ZonedDateTime zonedCloudletStartTime = ZonedDateTime.of(cloudletStartTime, ZoneId.systemDefault());
            jobDescriptionsMainNode.put("execTimeMs",
                    zonedCloudletFinishTime.toInstant().toEpochMilli() - zonedCloudletStartTime.toInstant().toEpochMilli());
            try {
                Files.writeString(jobDescriptionsPath, jobDescriptionsMainNode.toString() + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finishedCloudletsByVm.put(cloudlet.getVm().getId(), finishedCloudletsByVm.get(cloudlet.getVm().getId()) + 1);
        });
//        addColumnDataFunction(getTable().addColumn("Cloudlet", ID), Identifiable::getId);
//        addColumnDataFunction(getTable().addColumn("Status "), cloudlet -> cloudlet.getStatus().name());
//        addColumnDataFunction(getTable().addColumn("DC", ID), cloudlet -> cloudlet.getVm().getHost().getDatacenter().getId());
//        addColumnDataFunction(getTable().addColumn("Host", ID), cloudlet -> cloudlet.getVm().getHost().getId());
//        addColumnDataFunction(getTable().addColumn("Host PEs ", CPU_CORES), cloudlet -> cloudlet.getVm().getHost().getWorkingPesNumber());
//        addColumnDataFunction(getTable().addColumn("VM", ID), cloudlet -> cloudlet.getVm().getId());
//        addColumnDataFunction(getTable().addColumn("VM PEs   ", CPU_CORES), cloudlet -> cloudlet.getVm().getNumberOfPes());
//        addColumnDataFunction(getTable().addColumn("CloudletLen", "MI"), Cloudlet::getLength);
//        addColumnDataFunction(getTable().addColumn("CloudletPEs", CPU_CORES), Cloudlet::getNumberOfPes);
//
//        TableColumn col = getTable().addColumn("StartTime", SECONDS).setFormat(TIME_FORMAT);
//        addColumnDataFunction(col, Cloudlet::getExecStartTime);
//
//        col = getTable().addColumn("FinishTime", SECONDS).setFormat(TIME_FORMAT);
//        addColumnDataFunction(col, cl -> roundTime(cl, cl.getFinishTime()));
//
//        col = getTable().addColumn("ExecTime", SECONDS).setFormat(TIME_FORMAT);
//        addColumnDataFunction(col, cl -> roundTime(cl, cl.getActualCpuTime()));
    }

}


