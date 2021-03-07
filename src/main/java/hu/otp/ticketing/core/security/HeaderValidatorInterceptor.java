package hu.otp.ticketing.core.security;

import hu.otp.ticketing.core.common.HeaderConstants;
import hu.otp.ticketing.core.common.exception.MissingHeaderException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class HeaderValidatorInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        checkRequiredHeader(request, HeaderConstants.USER_ID);
        checkRequiredHeader(request, HeaderConstants.TOKEN);
        return true;
    }

    private void checkRequiredHeader(HttpServletRequest request, String headerName) {
        String header = request.getHeader(headerName);
        if (StringUtils.isEmpty(header)) {
            throw new MissingHeaderException(String.format("Required header %s missing!", headerName));
        }
    }
}
