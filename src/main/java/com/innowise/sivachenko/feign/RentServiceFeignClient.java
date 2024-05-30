package com.innowise.sivachenko.feign;

import com.innowise.sivachenko.feign.config.FeignConfig;
import com.innowise.sivachenko.feign.fallback.RentServiceFallback;
import com.innowise.sivachenko.model.exception.ServiceNotFoundException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "${feign.client.rent-service.name}", fallbackFactory = RentServiceFallback.class, configuration = FeignConfig.class)
public interface RentServiceFeignClient {
    @GetMapping("/api/v1/rent-service/paid/{rentId}")
    Boolean isRentPaid(@PathVariable(name = "rentId") Long rentId) throws ServiceNotFoundException;

    @GetMapping("/api/v1/rent-service/inactive/{rentId}")
    Boolean isRentActive(@PathVariable("rentId") Long rentId) throws ServiceNotFoundException;
}
