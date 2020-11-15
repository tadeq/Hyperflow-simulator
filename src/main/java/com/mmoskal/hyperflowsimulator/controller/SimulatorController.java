package com.mmoskal.hyperflowsimulator.controller;

import com.mmoskal.hyperflowsimulator.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SimulatorController {

    private final SimulationService simulationService;

    @Autowired
    public SimulatorController(SimulationService simulationService) {
        this.simulationService = simulationService;
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
