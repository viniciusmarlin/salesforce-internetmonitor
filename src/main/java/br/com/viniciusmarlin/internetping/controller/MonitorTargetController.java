package br.com.viniciusmarlin.internetping.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.viniciusmarlin.internetping.service.NetworkMonitorService;

import java.util.Map;

@RestController
@RequestMapping("/api/monitor/target")
public class MonitorTargetController {

    private final NetworkMonitorService monitorService;

    public MonitorTargetController(NetworkMonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @GetMapping
    public Map<String, Object> getCurrentTarget() {
        return Map.of(
                "host", monitorService.getTargetHost(),
                "port", monitorService.getTargetPort()
        );
    }

    @PutMapping
    public ResponseEntity updateTarget(@RequestBody Map<String, Object> payload) {
        String newHost = payload.get("host").toString().trim();
        int newPort = Integer.parseInt(payload.get("port").toString().trim());

        monitorService.updateTarget(newHost, newPort);

        return ResponseEntity.ok(Map.of(
                "message", "Alvo atualizado com sucesso sem reiniciar",
                "host", newHost,
                "port", newPort
        ));
    }
}