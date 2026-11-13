package br.com.viniciusmarlin.internetping.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import br.com.viniciusmarlin.internetping.client.SalesforceClient;
import br.com.viniciusmarlin.internetping.model.IncidentStatus;
import br.com.viniciusmarlin.internetping.model.NetworkIncident;
import br.com.viniciusmarlin.internetping.repository.IncidentRepository;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NetworkMonitorService {

    private static final Logger log = LoggerFactory.getLogger(NetworkMonitorService.class);

    private final IncidentRepository repository;
    private final SalesforceClient salesforceClient;
    

    @Value("${monitor.target.host}")
    private String targetHost;

    @Value("${monitor.target.port}")
    private int targetPort;

    @Value("${monitor.timeout.ms}")
    private int timeoutMs;

    @Value("${monitor.failure.threshold}")
    private int failureThreshold;

    private int consecutiveFailures = 0;

    public NetworkMonitorService(IncidentRepository repository, SalesforceClient salesforceClient) {
        this.repository = repository;
        this.salesforceClient = salesforceClient;
    }

    @Scheduled(fixedRate = 5000)
    public void monitorNetwork() {
        boolean isOnline = checkTcpConnection();

        if (!isOnline) {
            consecutiveFailures++;
            log.warn("Falha de conexão ({}/{}).", consecutiveFailures, failureThreshold);

            if (consecutiveFailures >= failureThreshold) {
                Optional<NetworkIncident> active = repository.findFirstByStatus(IncidentStatus.ONGOING);
                if (active.isEmpty()) {
                    NetworkIncident incident = new NetworkIncident(LocalDateTime.now());
                    repository.save(incident);
                    log.error("QUEDA DE INTERNET REGISTRADA às {}", incident.getStartedAt());
                }
            }
        } else {
            consecutiveFailures = 0;

            // Se havia uma queda em andamento, encerra agora
            repository.findFirstByStatus(IncidentStatus.ONGOING).ifPresent(incident -> {
                incident.closeIncident(LocalDateTime.now());
                repository.save(incident);
                log.info("CONEXÃO RESTABELECIDA. Duração: {}s", incident.getDurationSeconds());
            });

            // Sincroniza qualquer incidente pendente com o Salesforce
            syncPendingIncidents();
        }
    }

    private boolean checkTcpConnection() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(targetHost, targetPort), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void syncPendingIncidents() {
        List<NetworkIncident> pending = repository.findAllByStatus(IncidentStatus.PENDING_SYNC);
        for (NetworkIncident incident : pending) {
            try {
                String caseId = salesforceClient.createNetworkCase(incident);
                incident.markAsSynced(caseId);
                repository.save(incident);
                log.info("Alerta enviado ao Salesforce! Case ID: {}", caseId);
            } catch (Exception e) {
                log.error("Erro ao sincronizar com Salesforce (tentará novamente no próximo ciclo): {}", e.getMessage());
            }
        }
    }
}
