package com.innowise.sivachenko.feign.fallback;

import com.innowise.sivachenko.feign.RentServiceFeignClient;
import com.innowise.sivachenko.model.enums.RentStatus;
import com.innowise.sivachenko.model.exception.ServiceNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class RentServiceFallback implements FallbackFactory<RentServiceFeignClient> {
    @Override
    public RentServiceFeignClient create(Throwable cause) {
        return new RentServiceFeignClient() {
            @Override
            public Boolean canPayRent(Long rentId) throws ServiceNotFoundException {
                log.info("Fallback for canPayRent; reason was: {}, {}", cause.getMessage(), cause);
                throw new ServiceNotFoundException("Rent service is down. We are working to resolve the issue");
            }

            @Override
            public Boolean existsActiveRent(Long rentId, Long carId, Long clientId) throws ServiceNotFoundException {
                log.info("Fallback for existsActiveRent; reason was: {}, {}", cause.getMessage(), cause);
                throw new ServiceNotFoundException("Rent service is down. We are working to resolve the issue");
            }

            @Override
            public ResponseEntity<?> updateRentStatus(Long rentId, RentStatus rentStatus) throws ServiceNotFoundException {
                log.info("Fallback for updateRentStatus; reason was: {}, {}", cause.getMessage(), cause);
                throw new ServiceNotFoundException("Rent service is down. We are working to resolve the issue");
            }
        };
    }
}
