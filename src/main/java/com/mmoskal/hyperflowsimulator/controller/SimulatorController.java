package com.mmoskal.hyperflowsimulator.controller;

import com.mmoskal.hyperflowsimulator.model.envconfig.EnvironmentConfig;
import com.mmoskal.hyperflowsimulator.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class SimulatorController {

    private final SimulationService simulationService;

    @Autowired
    public SimulatorController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("simulate")
    public ResponseEntity<?> simulate() {
        try {
            simulationService.runSimulation();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("environment/configure")
    public ResponseEntity<?> configureEnvironment(@RequestBody EnvironmentConfig environmentConfig) {
        simulationService.initEnvironment(environmentConfig);
        return ResponseEntity.ok().build();
    }

    @GetMapping("test")
    public ResponseEntity<?> testSimulation() {
        simulationService.runTestSimulation();
        return ResponseEntity.ok().build();
    }
}
