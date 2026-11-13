package br.com.viniciusmarlin.internetping.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import br.com.viniciusmarlin.internetping.model.IncidentStatus;
import br.com.viniciusmarlin.internetping.model.NetworkIncident;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<NetworkIncident, Long> {
    Optional<NetworkIncident> findFirstByStatus(IncidentStatus status);
    List<NetworkIncident> findAllByStatus(IncidentStatus status);
}    
