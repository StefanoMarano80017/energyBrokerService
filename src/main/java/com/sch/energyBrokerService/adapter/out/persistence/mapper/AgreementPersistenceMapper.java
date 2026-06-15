package com.sch.energyBrokerService.adapter.out.persistence.mapper;

import com.sch.energyBrokerService.adapter.in.web.dto.Response.AgreementResponse;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.TariffView;
import com.sch.energyBrokerService.adapter.out.persistence.entity.AgreementEntity;
import com.sch.energyBrokerService.adapter.out.persistence.entity.PricePerKwhEmbeddable;
import com.sch.energyBrokerService.adapter.out.persistence.entity.TariffEmbeddable;
import com.sch.energyBrokerService.domain.model.agreement.Agreement;
import com.sch.energyBrokerService.domain.model.agreement.AgreementId;
import com.sch.energyBrokerService.domain.model.cpo.CpoId;
import com.sch.energyBrokerService.domain.model.emsp.EmspId;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;

public class AgreementPersistenceMapper {

    // Domain -> Entity
    public static AgreementEntity toEntity(Agreement domain) {
        Tariff tariff = domain.tariff();
        TariffEmbeddable tariffEmbeddable = new TariffEmbeddable(
                tariff.id().value(),
                tariff.type(),
                new PricePerKwhEmbeddable(tariff.pricePerKwh().amount())
        );

        return AgreementEntity.builder()
                .id(domain.id().value())
                .cpoId(domain.cpoId().value())
                .emspId(domain.emspId().value())
                .tariff(tariffEmbeddable)
                .status(domain.status())
                .createdAt(domain.createdAt())
                .acceptedAt(domain.acceptedAt())
                .build();
    }

    // Entity -> Domain
    public static Agreement toDomain(AgreementEntity entity) {
        TariffEmbeddable t = entity.getTariff();
        Tariff tariff = new Tariff(
                new TariffId(t.getTariffId()),
                t.getType(),
                new PricePerKwh(t.getPricePerKwh().getAmount())
        );

        return new Agreement(
                new AgreementId(entity.getId()),
                new CpoId(entity.getCpoId()),
                new EmspId(entity.getEmspId()),
                tariff,
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getAcceptedAt()
        );
    }

    public static AgreementResponse toResponse(Agreement domain) {
        Tariff tariff = domain.tariff();
        TariffView tariffView = new TariffView(
                tariff.id().value(),
                tariff.type().toString(),
                tariff.pricePerKwh().amount()
        );

        return new AgreementResponse(
                domain.id().value(),
                domain.cpoId().value(),
                domain.emspId().value(),
                tariffView,
                domain.status().toString(),
                domain.createdAt(),
                domain.acceptedAt()
        );
    }
}
