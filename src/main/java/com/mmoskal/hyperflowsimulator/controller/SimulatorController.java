package com.mmoskal.hyperflowsimulator.controller;

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

    private SimulationService simulationService;

    @Autowired
    public SimulatorController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("simulate")
    public ResponseEntity<?> simulate(@RequestBody Config config) {
        System.out.println(config);
        return ResponseEntity.ok().build();
    }

    @GetMapping("test")
    public ResponseEntity<?> testSimulation() {
        simulationService.runSimulation();
        return ResponseEntity.ok().build();
    }
}
