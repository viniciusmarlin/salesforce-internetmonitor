package br.com.viniciusmarlin.internetping.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import br.com.viniciusmarlin.internetping.model.NetworkIncident;

import java.util.Map;

@Component
public class SalesforceClient {

    private final RestClient restClient;
    private final String instanceUrl;
    private final String clientId;
    private final String clientSecret;

    public SalesforceClient(
            @Value("${salesforce.instance.url}") String instanceUrl,
            @Value("${salesforce.client.id}") String clientId,
            @Value("${salesforce.client.secret}") String clientSecret) {
        this.instanceUrl = instanceUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.create();
    }

    public String createNetworkCase(NetworkIncident incident) {
        String token = authenticate();

        Map<String, Object> payload = Map.of(
            "Subject", "[Alerta Automático] Queda de Link de Internet Detectada",
            "Status", "New",
            "Origin", "Monitoramento API",
            "Priority", "High",
            "Description", String.format(
                "Indisponibilidade detectada automaticamente pela API de monitoramento.\n" +
                "Início: %s\nRetorno: %s\nDuração total: %d segundos.",
                incident.getStartedAt(), incident.getEndedAt(), incident.getDurationSeconds()
            )
        );

        Map<?, ?> response = restClient.post()
                .uri(instanceUrl + "/services/data/v60.0/sobjects/Case")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

        return response != null ? (String) response.get("id") : null;
    }

    private String authenticate() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        Map<?, ?> response = restClient.post()
                .uri(instanceUrl + "/services/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("Falha ao obter token OAuth2 do Salesforce.");
        }
        return (String) response.get("access_token");
    }
}