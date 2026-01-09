package com.sch.energyBrokerService.application.service.tariff.command;

import com.sch.energyBrokerService.adapter.in.web.dto.Request.CreateTariffRequest;
import com.sch.energyBrokerService.adapter.in.web.dto.Response.TariffResponse;
import com.sch.energyBrokerService.adapter.out.persistence.mapper.TariffPersistenceMapper;
import com.sch.energyBrokerService.domain.model.geo.Coordinate;
import com.sch.energyBrokerService.domain.model.geo.GeoArea;
import com.sch.energyBrokerService.domain.model.supplier.SupplierId;
import com.sch.energyBrokerService.domain.model.tariff.PricePerKwh;
import com.sch.energyBrokerService.domain.model.tariff.Tariff;
import com.sch.energyBrokerService.domain.model.tariff.TariffId;
import com.sch.energyBrokerService.domain.model.tariff.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CreateTariffCommandHandler {

    private final TariffRepository tariffRepository;

    public TariffResponse handle(CreateTariffRequest request) {

        Tariff tariff = new Tariff(
            TariffId.newId(),
            new SupplierId(request.supplierId()),
            request.type(),
            new PricePerKwh(request.pricePerKwh()),
            new GeoArea(
                new Coordinate(request.latitude(), request.longitude()),
                request.geoRadiusKm()
            )
        );

        tariffRepository.save(tariff);
        return TariffPersistenceMapper.toResponse(tariff);
    }
}