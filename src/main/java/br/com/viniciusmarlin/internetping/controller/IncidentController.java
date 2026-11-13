package br.com.viniciusmarlin.internetping.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.viniciusmarlin.internetping.model.NetworkIncident;
import br.com.viniciusmarlin.internetping.repository.IncidentRepository;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentRepository repository;

    public IncidentController(IncidentRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<NetworkIncident> listAll() {
        return repository.findAll();
    }
}