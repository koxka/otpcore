package hu.otp.ticketing.core.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        traceRequest(httpRequest, body);
        ClientHttpResponse response = execution.execute(httpRequest, body);
        traceResponse(response);
        return response;
    }

    private void traceResponse(ClientHttpResponse response) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
        String line = br.readLine();
        while (line != null) {
            stringBuilder.append(line);
            stringBuilder.append('\n');
            line = br.readLine();
        }
        br.close();

        log.info("------------------ RP BEGIN ------------------");
        log.info("Status code         : {}", response.getStatusCode());
        log.info("Status text         : {}", response.getStatusText());
        log.info("Headers             : {}", response.getHeaders());
        log.info("Response body       : {}", stringBuilder);
        log.info("------------------- RP END -------------------");
    }

    private void traceRequest(HttpRequest httpRequest, byte[] body) {
        log.info("------------------ RQ BEGIN ------------------");
        log.info("URI                 : {}", httpRequest.getURI());
        log.info("Method              : {}", httpRequest.getMethod());
        log.info("Headers             : {}", httpRequest.getHeaders());
        log.info("Request body        : {}", new String(body, StandardCharsets.UTF_8));
        log.info("------------------- RQ END -------------------");
    }
}
