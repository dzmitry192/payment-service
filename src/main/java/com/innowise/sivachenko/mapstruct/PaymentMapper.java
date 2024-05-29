package com.innowise.sivachenko.mapstruct;

import com.innowise.sivachenko.model.dto.request.CreatePaymentDto;
import com.innowise.sivachenko.model.dto.response.PaymentDto;
import com.innowise.sivachenko.model.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentMapper {

    PaymentDto toPaymentDto(PaymentEntity paymentEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stripePaymentId", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PaymentEntity toPaymentEntity(CreatePaymentDto createPaymentDto);
}
