package com.mmoskal.hyperflowsimulator.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmoskal.hyperflowsimulator.model.Config;
import com.mmoskal.hyperflowsimulator.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class SimulatorController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SimulationService simulationService;

    @Autowired
    public SimulatorController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("simulate")
    public ResponseEntity<?> addJob(@RequestBody Config config) {
        try {
            System.out.println(OBJECT_MAPPER.writeValueAsString(config));
            simulationService.addConfig(config);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("simulate")
    public ResponseEntity<?> simulate() {
        simulationService.runSimulation();
        return ResponseEntity.ok().build();
    }

    @GetMapping("test")
    public ResponseEntity<?> testSimulation() {
        simulationService.runTestSimulation();
        return ResponseEntity.ok().build();
    }
}
