package br.com.viniciusmarlin.internetping.model;

public enum IncidentStatus {
    ONGOING,       // Queda em andamento
    PENDING_SYNC,  // Internet voltou, aguardando envio ao Salesforce
    SYNCED         // Registrado com sucesso no painel do Salesforce
}
