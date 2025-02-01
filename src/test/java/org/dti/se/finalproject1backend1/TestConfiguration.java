package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.LoginByEmailAndPasswordRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterAndLoginByExternalRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByEmailAndPasswordRequest;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.deliveries.gateways.MailgunGateway;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.*;
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

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TestConfiguration {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AccountRepository accountRepository;
    @Autowired
    protected WarehouseRepository warehouseRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected ProductRepository productRepository;
    @Autowired
    protected WarehouseProductRepository warehouseProductRepository;
    @Autowired
    protected CartItemRepository cartItemRepository;
    @Autowired
    protected VerificationRepository verificationRepository;

    @MockitoBean
    protected MailgunGateway mailgunGatewayMock;
    @MockitoBean
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @Autowired
    protected SecurityConfiguration securityConfiguration;

    protected List<Account> fakeAccounts = new ArrayList<>();
    protected List<Warehouse> fakeWarehouses = new ArrayList<>();
    protected List<Category> fakeCategories = new ArrayList<>();
    protected List<Product> fakeProducts = new ArrayList<>();
    protected List<WarehouseProduct> fakeWarehouseProducts = new ArrayList<>();
    protected List<CartItem> fakeCartItems = new ArrayList<>();

    protected String rawPassword = String.format("password-%s", UUID.randomUUID());
    protected Account authenticatedAccount;
    protected Session authenticatedSession;

    @Autowired
    protected ObjectMapper objectMapper;

    public void populate() {
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        for (int i = 0; i < 4; i++) {
            Account newAccount = Account
                    .builder()
                    .id(UUID.randomUUID())
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .email(String.format("email-%s", UUID.randomUUID()))
                    .password(securityConfiguration.encode(rawPassword))
                    .phone(String.format("phone-%s", UUID.randomUUID()))
                    .build();
            fakeAccounts.add(newAccount);

        }
        fakeAccounts = accountRepository.saveAll(fakeAccounts);

        for (int i = 0; i < 4; i++) {
            Warehouse newWarehouse = Warehouse
                    .builder()
                    .id(UUID.randomUUID())
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .description(String.format("description-%s", UUID.randomUUID()))
                    .build();
            fakeWarehouses.add(newWarehouse);
        }
        fakeWarehouses = warehouseRepository.saveAll(fakeWarehouses);

        for (int i = 0; i < 4; i++) {
            Category newCategory = Category
                    .builder()
                    .id(UUID.randomUUID())
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .description(String.format("description-%s", UUID.randomUUID()))
                    .build();
            fakeCategories.add(newCategory);
        }
        fakeCategories = categoryRepository.saveAll(fakeCategories);

        fakeCategories.forEach(category -> {
            for (int i = 0; i < 4; i++) {
                Product newProduct = Product
                        .builder()
                        .id(UUID.randomUUID())
                        .category(category)
                        .name(String.format("name-%s", UUID.randomUUID()))
                        .description(String.format("description-%s", UUID.randomUUID()))
                        .price(Math.random() * 1000000)
                        .image(null)
                        .build();
                fakeProducts.add(newProduct);
            }
        });
        fakeProducts = productRepository.saveAll(fakeProducts);

        fakeProducts.forEach(product -> {
            fakeWarehouses.forEach(warehouse -> {
                WarehouseProduct newWarehouseProduct = WarehouseProduct
                        .builder()
                        .id(UUID.randomUUID())
                        .warehouse(warehouse)
                        .product(product)
                        .quantity(Math.ceil(Math.random() * 1000))
                        .build();
                fakeWarehouseProducts.add(newWarehouseProduct);
            });
        });
        fakeWarehouseProducts = warehouseProductRepository.saveAll(fakeWarehouseProducts);

        fakeAccounts.forEach(account -> {
            fakeProducts.forEach(product -> {
                CartItem newCartItem = CartItem
                        .builder()
                        .id(UUID.randomUUID())
                        .account(account)
                        .product(product)
                        .quantity(Math.ceil(Math.random() * 100))
                        .build();
                fakeCartItems.add(newCartItem);
            });
        });
        fakeCartItems = cartItemRepository.saveAll(fakeCartItems);
    }

    public void depopulate() {
        cartItemRepository.deleteAll(fakeCartItems);
        fakeCartItems.clear();
        warehouseProductRepository.deleteAll(fakeWarehouseProducts);
        fakeWarehouseProducts.clear();
        productRepository.deleteAll(fakeProducts);
        fakeProducts.clear();
        categoryRepository.deleteAll(fakeCategories);
        fakeCategories.clear();
        warehouseRepository.deleteAll(fakeWarehouses);
        fakeWarehouses.clear();
        accountRepository.deleteAll(fakeAccounts);
        fakeAccounts.clear();
    }

    public void auth() throws Exception {
        authenticatedAccount = registerInternal().getData();
        fakeAccounts.add(authenticatedAccount);
        authenticatedSession = login(authenticatedAccount).getData();
    }

    public void auth(Account account) throws Exception {
        authenticatedAccount = account;
        authenticatedSession = login(account).getData();
    }

    public void deauth() throws Exception {
        logout(authenticatedSession);
    }

    protected ResponseBody<Account> registerInternal() throws Exception {
        RegisterByEmailAndPasswordRequest requestBody = RegisterByEmailAndPasswordRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .email(String.format("email-%s", UUID.randomUUID()))
                .password(rawPassword)
                .phone(String.format("phone-%s", UUID.randomUUID()))
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/authentications/registers/email-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Account> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Register succeed.");
        assert body.getData() != null;
        assert body.getData().getId() != null;
        assert body.getData().getName().equals(requestBody.getName());
        assert body.getData().getEmail().equals(requestBody.getEmail());
        assert securityConfiguration.matches(requestBody.getPassword(), body.getData().getPassword());
        assert body.getData().getPhone().equals(requestBody.getPhone());
        return body;
    }

    protected ResponseBody<Account> registerExternal() throws Exception {
        String mockIdToken = "mock-id-token";
        String email = String.format("email-%s", UUID.randomUUID());
        String name = String.format("name-%s", UUID.randomUUID());
        String picture = "http://example.com/picture.jpg";

        GoogleIdToken.Payload payload = Mockito.mock(GoogleIdToken.Payload.class);
        Mockito.when(payload.getEmail()).thenReturn(email);
        Mockito.when(payload.get("name")).thenReturn(name);
        Mockito.when(payload.get("picture")).thenReturn(picture.getBytes());

        GoogleIdToken idToken = Mockito.mock(GoogleIdToken.class);
        Mockito.when(idToken.getPayload()).thenReturn(payload);

        Mockito.when(googleIdTokenVerifier.verify(mockIdToken)).thenReturn(idToken);

        RegisterAndLoginByExternalRequest requestBody = RegisterAndLoginByExternalRequest
                .builder()
                .idToken(mockIdToken)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/authentications/registers/external")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Account> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Register succeed.");
        assert body.getData() != null;
        assert body.getData().getId() != null;
        assert body.getData().getName().equals(name);
        assert body.getData().getEmail().equals(email);
        assert body.getData().getImage() != null;

        return body;
    }

    protected ResponseBody<Session> login(Account account) throws Exception {
        LoginByEmailAndPasswordRequest requestBody = LoginByEmailAndPasswordRequest
                .builder()
                .email(account.getEmail())
                .password(rawPassword)
                .build();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/authentications/logins/email-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Session> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        assert body != null;
        assert body.getMessage().equals("Login succeed.");
        assert body.getData() != null;
        assert body.getData().getAccessToken() != null;
        assert body.getData().getRefreshToken() != null;
        assert body.getData().getAccessTokenExpiredAt().isAfter(now);
        assert body.getData().getRefreshTokenExpiredAt().isAfter(now);
        return body;
    }

    protected ResponseBody<Void> logout(Session session) throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/authentications/logouts/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(session));

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Logout succeed.");
        return body;
    }

    protected Verification getVerification(String email, String type) throws Exception {
        Mockito.doNothing().when(mailgunGatewayMock).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/otps/send")
                .param("email", email)
                .param("type", type)
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        assert body != null;
        assert body.getMessage().equals("OTP sent succeed.");
        assert body.getData() == null;

        return verificationRepository
                .findByEmailAndType(email, type)
                .orElseThrow(VerificationNotFoundException::new);

    }
}