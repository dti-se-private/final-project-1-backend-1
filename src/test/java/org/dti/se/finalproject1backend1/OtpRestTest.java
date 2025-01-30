package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.outers.deliveries.gateways.MailgunGateway;
import org.dti.se.finalproject1backend1.outers.repositories.ones.VerificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OtpRestTest extends TestConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VerificationRepository verificationRepository;

    private String testEmail;
    private String testType;

    @BeforeEach
    public void setUp() {
        testEmail = String.format("email-%s@example.com", UUID.randomUUID());
        testType = "REGISTER";
    }

    @AfterEach
    public void tearDown() {
        verificationRepository.deleteAll();
    }

    @Test
    public void sendOtp() throws Exception {
        Mockito.doNothing().when(mailgunGatewayMock).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/otps/send")
                .param("email", testEmail)
                .param("type", testType)
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("OTP sent succeed.");
    }
}