package hu.otp.ticketing.core.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.otp.ticketing.core.common.exception.UnauthorizedException;
import hu.otp.ticketing.core.config.GeneralConfig;
import hu.otp.ticketing.core.endpoint.rest.model.ChargeRequest;
import hu.otp.ticketing.core.endpoint.rest.model.ChargeResponse;
import hu.otp.ticketing.core.endpoint.rest.model.GeneralSuccessResponse;
import hu.otp.ticketing.core.endpoint.rest.model.Info;
import hu.otp.ticketing.core.service.CoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static hu.otp.ticketing.core.common.HeaderConstants.TOKEN;
import static hu.otp.ticketing.core.common.HeaderConstants.USER_ID;
import static hu.otp.ticketing.core.config.SecurityConfig.SERVICE_USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = CoreEndpoint.class)
@TestPropertySource(properties = {"spring.security.user.name=user", "spring.security.user.password=pass"})
class CoreEndpointTest {
    private static final String BASE_PATH = "/core-api/v1";
    private ObjectMapper mapper = new GeneralConfig().objectMapper();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CoreService coreService;

    @Test
    @WithMockUser(roles = {SERVICE_USER})
    void authenticate() throws Exception {

        GeneralSuccessResponse rp = new GeneralSuccessResponse();
        Info info = new Info();
        info.setSuccess(true);
        rp.setInfo(info);
        when(coreService.isAuthenticated()).thenReturn(rp);

        mvc.perform(MockMvcRequestBuilders
                .get(BASE_PATH + "/authenticate")
                .headers(getHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.info.success").value(true));
    }

    @Test
    @WithMockUser(roles = {SERVICE_USER})
    void authenticate_missingHeader() throws Exception {

        GeneralSuccessResponse rp = new GeneralSuccessResponse();
        Info info = new Info();
        info.setSuccess(true);
        rp.setInfo(info);
        when(coreService.isAuthenticated()).thenReturn(rp);

        mvc.perform(MockMvcRequestBuilders
                .get(BASE_PATH + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void authenticate_noRole() throws Exception {

        GeneralSuccessResponse rp = new GeneralSuccessResponse();
        Info info = new Info();
        info.setSuccess(true);
        rp.setInfo(info);
        when(coreService.isAuthenticated()).thenReturn(rp);

        mvc.perform(MockMvcRequestBuilders
                .get(BASE_PATH + "/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }


    @Test
    @WithMockUser(roles = {SERVICE_USER})
    void charge() throws Exception {
        when(coreService.charge(any())).thenReturn(new ChargeResponse());

        mvc.perform(MockMvcRequestBuilders
                .post(BASE_PATH + "/charge")
                .headers(getHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new ChargeRequest()))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = {SERVICE_USER})
    void charge_unAuth() throws Exception {
        doThrow(new UnauthorizedException("")).when(coreService).charge(any());

        mvc.perform(MockMvcRequestBuilders
                .post(BASE_PATH + "/charge")
                .headers(getHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new ChargeRequest()))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    private String asJsonString(final Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(USER_ID, "userId");
        headers.set(TOKEN, "token");
        return headers;
    }
}

