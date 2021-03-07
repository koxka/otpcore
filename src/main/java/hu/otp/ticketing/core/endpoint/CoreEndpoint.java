package hu.otp.ticketing.core.endpoint;

import hu.otp.ticketing.core.endpoint.rest.OtpCoreApi;
import hu.otp.ticketing.core.endpoint.rest.model.ChargeRequest;
import hu.otp.ticketing.core.endpoint.rest.model.ChargeResponse;
import hu.otp.ticketing.core.endpoint.rest.model.GeneralSuccessResponse;
import hu.otp.ticketing.core.service.CoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.basePath}")
@RequiredArgsConstructor
public class CoreEndpoint implements OtpCoreApi {
    private final CoreService coreService;

    @Override
    public ResponseEntity<ChargeResponse> charge(ChargeRequest chargeRequest) {
        return ResponseEntity.ok(coreService.charge(chargeRequest));
    }

    @Override
    public ResponseEntity<GeneralSuccessResponse> isAuthenticated() {
        return ResponseEntity.ok(coreService.isAuthenticated());
    }
}
