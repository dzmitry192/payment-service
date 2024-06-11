package com.innowise.sivachenko.feign;

import com.innowise.sivachenko.feign.config.FeignConfig;
import com.innowise.sivachenko.feign.fallback.RentServiceFallback;
import com.innowise.sivachenko.model.enums.RentStatus;
import com.innowise.sivachenko.model.exception.ServiceNotFoundException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "${feign.client.rent-service.name}", fallbackFactory = RentServiceFallback.class, configuration = FeignConfig.class)
public interface RentServiceFeignClient {
    @GetMapping("/api/v1/rent-service/canPay/{rentId}")
    Boolean canPayRent(@PathVariable(name = "rentId") Long rentId) throws ServiceNotFoundException;

    @GetMapping("/api/v1/rent-service/active")
    Boolean existsActiveRent(@RequestParam(name = "rentId", required = false) Long rentId,
                             @RequestParam(name = "carId", required = false) Long carId,
                             @RequestParam(name = "clientId", required = false) Long clientId) throws ServiceNotFoundException;

    @PatchMapping("/api/v1/rent-service/status/{rentId}")
    ResponseEntity<?> updateRentStatus(@PathVariable(name = "rentId") Long rentId, @RequestBody RentStatus rentStatus) throws ServiceNotFoundException;
}
