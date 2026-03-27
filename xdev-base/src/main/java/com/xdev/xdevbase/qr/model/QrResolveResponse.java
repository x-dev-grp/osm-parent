package com.xdev.xdevbase.qr.model;


import lombok.Data;

/**
 * Réponse standardisée de l'API de résolution.
 * Contient toutes les informations nécessaires au mobile pour afficher l'entité.
 */
@Data
public class QrResolveResponse {
    private String entityType;      // type de l'entité (ex: "OF")
    private String publicCode;      // code public scanné
    private String entityId;        // identifiant interne (UUID converti en String)
    private String label;           // libellé affichable (ex: numéro d'OF)
    private String status;          // statut métier de l'entité
    private String mobileRoute;     // route à utiliser sur le mobile
    private Object data;            // données complètes de l'entité (facultatif)
}