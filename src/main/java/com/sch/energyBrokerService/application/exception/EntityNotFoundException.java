package com.sch.energyBrokerService.application.exception;

/**
 * Sollevata quando un'entità referenziata (CPO, eMSP, Agreement) non esiste.
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
