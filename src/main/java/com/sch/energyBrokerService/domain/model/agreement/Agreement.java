package com.sch.energyBrokerService.domain.model.agreement;

import java.time.Instant;

import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffType;

/**
 * Aggregate Root: accordo commerciale tra un CPO ed un eMSP avente per oggetto una {@link Tariff}.
 *
 * <p>Il ciclo di vita dello stato è governato qui (non nei command handler):
 * <pre>
 *   propose()  ->  PROPOSED
 *   accept()   :  PROPOSED  -> ACCEPTED  (valorizza acceptedAt)
 *   reject()   :  PROPOSED  -> REJECTED
 *   cancel()   :  PROPOSED/ACCEPTED -> CANCELLED
 * </pre>
 * Le transizioni non ammesse sollevano {@link IllegalStateException}.
 */
public class Agreement {

    private final AgreementId id;
    private final CpoId cpoId;
    private final EmspId emspId;
    private Tariff tariff;
    private AgreementStatus status;
    private final Instant createdAt;
    private Instant acceptedAt;
    private Instant lastModifiedAt;

    /**
     * Costruttore completo, usato per la ricostruzione dell'aggregato dal layer di persistenza.
     */
    public Agreement(
        AgreementId id,
        CpoId cpoId,
        EmspId emspId,
        Tariff tariff,
        AgreementStatus status,
        Instant createdAt,
        Instant acceptedAt,
        Instant lastModifiedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("AgreementId is required");
        }
        if (cpoId == null) {
            throw new IllegalArgumentException("CpoId is required");
        }
        if (emspId == null) {
            throw new IllegalArgumentException("EmspId is required");
        }
        if (tariff == null) {
            throw new IllegalArgumentException("Tariff is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("AgreementStatus is required");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required");
        }

        this.id = id;
        this.cpoId = cpoId;
        this.emspId = emspId;
        this.tariff = tariff;
        this.status = status;
        this.createdAt = createdAt;
        this.acceptedAt = acceptedAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    /**
     * Pubblicazione: un CPO propone una tariffa a un eMSP. Nasce un accordo in stato PROPOSED.
     */
    public static Agreement propose(CpoId cpoId, EmspId emspId, Tariff tariff) {
        return new Agreement(
            AgreementId.newId(),
            cpoId,
            emspId,
            tariff,
            AgreementStatus.PROPOSED,
            Instant.now(),
            null,
            Instant.now()
        );
    }

    public void accept() {
        requireStatus(AgreementStatus.PROPOSED, "accept");
        this.status = AgreementStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
        this.lastModifiedAt = Instant.now();
    }

    public void reject() {
        requireStatus(AgreementStatus.PROPOSED, "reject");
        this.status = AgreementStatus.REJECTED;
        this.lastModifiedAt = Instant.now();
    }

    public void cancel() {
        if (status != AgreementStatus.PROPOSED && status != AgreementStatus.ACCEPTED) {
            throw new IllegalStateException(
                "Cannot cancel an agreement in status " + status);
        }
        this.status = AgreementStatus.CANCELLED;
        this.lastModifiedAt = Instant.now();
    }

    public void updateTariff(TariffType type, PricePerKwh pricePerKwh) {
        this.tariff = new Tariff(tariff.id(), type, pricePerKwh);
        this.lastModifiedAt = Instant.now();
    }

    private void requireStatus(AgreementStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException(
                "Cannot " + action + " an agreement in status " + status +
                " (expected " + expected + ")");
        }
    }

    public AgreementId id() {
        return id;
    }

    public CpoId cpoId() {
        return cpoId;
    }

    public EmspId emspId() {
        return emspId;
    }

    public Tariff tariff() {
        return tariff;
    }

    public AgreementStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant acceptedAt() {
        return acceptedAt;
    }

    public Instant lastModifiedAt() {
        return lastModifiedAt;
    }

}
