package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dti.se.finalproject1backend1.inners.models.entities.Verification;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.outers.repositories.ones.VerificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
        testType = "register";
    }

    @AfterEach
    public void tearDown() {
        verificationRepository.deleteAll();
    }

    @Test
    public void testSendOtp() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/api/otp/send")
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
        assert body.getMessage().equals("OTP sent successfully.");
    }

    @Test
    public void testVerifyOtp() throws Exception {
        // First, send the OTP
        testSendOtp();

        // Retrieve the OTP from the database
        Verification verification = verificationRepository.findByEmail(testEmail);
        assert verification != null;
        String otp = verification.getCode();

        // Verify the OTP
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/api/otp/verify")
                .param("email", testEmail)
                .param("otp", otp)
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("OTP verified successfully.");
    }
}