package com.innowise.sivachenko.feign.fallback;

import com.innowise.sivachenko.feign.RentServiceFeignClient;
import com.innowise.sivachenko.model.exception.ServiceNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class RentServiceFallback implements FallbackFactory<RentServiceFeignClient> {
    @Override
    public RentServiceFeignClient create(Throwable cause) {
        return rentId -> {
            log.info("fallback; reason was: {}, {}", cause.getMessage(), cause);
            throw new ServiceNotFoundException("Rent service is down. We are working to resolve the issue");
        };
    }
}
