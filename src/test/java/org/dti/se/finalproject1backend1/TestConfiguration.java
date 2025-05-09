package org.dti.se.finalproject1backend1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.dti.se.finalproject1backend1.inners.models.entities.*;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.ResponseBody;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.Session;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.accounts.AccountResponse;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.LoginByInternalRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByExternalRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.authentications.RegisterByInternalRequest;
import org.dti.se.finalproject1backend1.inners.models.valueobjects.verifications.VerificationRequest;
import org.dti.se.finalproject1backend1.outers.configurations.GoogleConfiguration;
import org.dti.se.finalproject1backend1.outers.configurations.SecurityConfiguration;
import org.dti.se.finalproject1backend1.outers.deliveries.gateways.GmailGateway;
import org.dti.se.finalproject1backend1.outers.exceptions.accounts.AccountNotFoundException;
import org.dti.se.finalproject1backend1.outers.exceptions.verifications.VerificationNotFoundException;
import org.dti.se.finalproject1backend1.outers.repositories.ones.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
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
    protected ProviderRepository providerRepository;
    @Autowired
    protected AccountAddressRepository accountAddressRepository;
    @Autowired
    AccountPermissionRepository accountPermissionRepository;
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
    protected OrderRepository orderRepository;
    @Autowired
    protected OrderItemRepository orderItemRepository;
    @Autowired
    protected OrderStatusRepository orderStatusRepository;
    @Autowired
    protected WarehouseLedgerRepository warehouseLedgerRepository;
    @Autowired
    protected VerificationRepository verificationRepository;
    @Autowired
    protected WarehouseAdminRepository warehouseAdminRepository;

    @MockitoBean
    protected GmailGateway gmailGatewayMock;
    @MockitoBean
    protected GoogleConfiguration googleConfiguration;

    @Autowired
    protected SecurityConfiguration securityConfiguration;

    protected List<Account> fakeAccounts = new ArrayList<>();
    protected List<Provider> fakeProviders = new ArrayList<>();
    protected List<AccountAddress> fakeAccountAddresses = new ArrayList<>();
    protected List<AccountPermission> fakePermissions = new ArrayList<>();
    protected List<Warehouse> fakeWarehouses = new ArrayList<>();
    protected List<Category> fakeCategories = new ArrayList<>();
    protected List<Product> fakeProducts = new ArrayList<>();
    protected List<WarehouseProduct> fakeWarehouseProducts = new ArrayList<>();
    protected List<CartItem> fakeCartItems = new ArrayList<>();
    protected List<Order> fakeOrders = new ArrayList<>();
    protected List<OrderItem> fakeOrderItems = new ArrayList<>();
    protected List<OrderStatus> fakeOrderStatuses = new ArrayList<>();
    protected List<WarehouseLedger> fakeWarehouseLedgers = new ArrayList<>();
    protected List<WarehouseAdmin> fakeWarehouseAdmins = new ArrayList<>();

    protected String rawPassword = String.format("password-%s", UUID.randomUUID());
    protected Account authenticatedAccount;
    protected Session authenticatedSession;

    @Autowired
    protected ObjectMapper objectMapper;

    public void populate() {
        GeometryFactory geometryFactory = new GeometryFactory();
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        for (int i = 0; i < 5; i++) {
            Account newAccount = Account
                    .builder()
                    .id(UUID.randomUUID())
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .email(String.format("email-%s", UUID.randomUUID()))
                    .password(securityConfiguration.encode(rawPassword))
                    .phone(String.format("phone-%s", UUID.randomUUID()))
                    .build();
            fakeAccounts.add(newAccount);

            Provider newProvider = Provider
                    .builder()
                    .id(UUID.randomUUID())
                    .account(newAccount)
                    .name("INTERNAL")
                    .build();
            fakeProviders.add(newProvider);

            AccountAddress newAccountAddress = AccountAddress
                    .builder()
                    .id(UUID.randomUUID())
                    .account(newAccount)
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .location(geometryFactory.createPoint(new Coordinate(Math.random() * 10, Math.random() * 10)))
                    .build();
            fakeAccountAddresses.add(newAccountAddress);

            AccountPermission newPermission = AccountPermission
                    .builder()
                    .id(UUID.randomUUID())
                    .account(newAccount)
                    .permission("SUPER_ADMIN")
                    .build();
            fakePermissions.add(newPermission);
        }
        accountRepository.saveAllAndFlush(fakeAccounts);
        providerRepository.saveAllAndFlush(fakeProviders);
        accountAddressRepository.saveAllAndFlush(fakeAccountAddresses);
        accountPermissionRepository.saveAllAndFlush(fakePermissions);

        for (int i = 0; i < 5; i++) {
            Warehouse newWarehouse = Warehouse
                    .builder()
                    .id(UUID.randomUUID())
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .description(String.format("description-%s", UUID.randomUUID()))
                    .location(geometryFactory.createPoint(new Coordinate(Math.random() * 10, Math.random() * 10)))
                    .build();
            fakeWarehouses.add(newWarehouse);
        }
        warehouseRepository.saveAllAndFlush(fakeWarehouses);

        for (int i = 0; i < 5; i++) {
            Category newCategory = Category
                    .builder()
                    .id(UUID.randomUUID())
                    .name(String.format("name-%s", UUID.randomUUID()))
                    .description(String.format("description-%s", UUID.randomUUID()))
                    .build();
            fakeCategories.add(newCategory);
        }
        categoryRepository.saveAllAndFlush(fakeCategories);

        fakeCategories.forEach(category -> {
            for (int i = 0; i < 5; i++) {
                Product newProduct = Product
                        .builder()
                        .id(UUID.randomUUID())
                        .category(category)
                        .name(String.format("name-%s", UUID.randomUUID()))
                        .description(String.format("description-%s", UUID.randomUUID()))
                        .price(Math.ceil(Math.random() * 1000000))
                        .weight(Math.ceil(Math.random() * 10000))
                        .image(null)
                        .build();
                fakeProducts.add(newProduct);
            }
        });
        productRepository.saveAllAndFlush(fakeProducts);

        fakeProducts.forEach(product -> {
            fakeWarehouses.forEach(warehouse -> {
                WarehouseProduct newWarehouseProduct = WarehouseProduct
                        .builder()
                        .id(UUID.randomUUID())
                        .warehouse(warehouse)
                        .product(product)
                        .quantity(2000 + Math.ceil(Math.random() * 1000))
                        .build();
                fakeWarehouseProducts.add(newWarehouseProduct);
            });
        });
        warehouseProductRepository.saveAllAndFlush(fakeWarehouseProducts);

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
        cartItemRepository.saveAllAndFlush(fakeCartItems);

        List<String> ledgerStatuses = List.of(
                "WAITING_FOR_APPROVAL",
                "APPROVED",
                "REJECTED"
        );
        List<String> orderStatuses = List.of(
                "WAITING_FOR_PAYMENT",
                "WAITING_FOR_PAYMENT_CONFIRMATION",
                "PROCESSING",
                "SHIPPING",
                "ORDER_CONFIRMED",
                "CANCELED"
        );

        fakeAccounts.forEach(account -> {
            for (int i = 0; i < 5; i++) {
                Warehouse orderOriginWarehouse = fakeWarehouses.get(i);
                Order newOrder = Order
                        .builder()
                        .id(UUID.randomUUID())
                        .account(account)
                        .totalPrice(Math.ceil(Math.random() * 1000000))
                        .shipmentOrigin(geometryFactory.createPoint(new Coordinate(Math.random() * 10, Math.random() * 10)))
                        .shipmentDestination(geometryFactory.createPoint(new Coordinate(Math.random() * 10, Math.random() * 10)))
                        .shipmentPrice(Math.ceil(Math.random() * 1000000))
                        .itemPrice(Math.ceil(Math.random() * 1000000))
                        .originWarehouse(orderOriginWarehouse)
                        .build();
                fakeOrders.add(newOrder);

                for (int j = 0; j < orderStatuses.size() - (i + 1); j++) {
                    OrderStatus newOrderStatus = OrderStatus
                            .builder()
                            .id(UUID.randomUUID())
                            .order(newOrder)
                            .status(orderStatuses.get(j))
                            .time(now.plusNanos(j * 1000))
                            .build();
                    fakeOrderStatuses.add(newOrderStatus);
                }

                for (int j = 0; j < fakeProducts.size(); j++) {
                    Product product = fakeProducts.get(j);
                    WarehouseProduct originWarehouseProduct = fakeWarehouseProducts
                            .stream()
                            .filter(warehouseProduct -> !warehouseProduct.getWarehouse().equals(orderOriginWarehouse) && warehouseProduct.getProduct().equals(product))
                            .findFirst()
                            .orElseThrow();
                    WarehouseProduct destinationWarehouseProduct = fakeWarehouseProducts
                            .stream()
                            .filter(warehouseProduct -> warehouseProduct.getWarehouse().equals(orderOriginWarehouse) && warehouseProduct.getProduct().equals(product))
                            .findFirst()
                            .orElseThrow();
                    String ledgerStatus = ledgerStatuses.get(j % ledgerStatuses.size());
                    Double difference = Math.ceil(Math.random() * 100);
                    WarehouseLedger newWarehouseLedger = WarehouseLedger
                            .builder()
                            .id(UUID.randomUUID())
                            .originWarehouseProduct(originWarehouseProduct)
                            .destinationWarehouseProduct(destinationWarehouseProduct)
                            .originPreQuantity(100.0)
                            .originPostQuantity(100 - difference)
                            .destinationPreQuantity(200.0)
                            .destinationPostQuantity(200 + difference)
                            .time(now)
                            .status(ledgerStatus)
                            .build();
                    fakeWarehouseLedgers.add(newWarehouseLedger);

                    OrderItem newOrderItem = OrderItem
                            .builder()
                            .id(UUID.randomUUID())
                            .order(newOrder)
                            .product(product)
                            .quantity(Math.ceil(Math.random() * 100))
                            .warehouseLedger(newWarehouseLedger)
                            .build();
                    fakeOrderItems.add(newOrderItem);
                }
            }
        });
        orderRepository.saveAllAndFlush(fakeOrders);
        orderStatusRepository.saveAllAndFlush(fakeOrderStatuses);
        warehouseLedgerRepository.saveAllAndFlush(fakeWarehouseLedgers);
        orderItemRepository.saveAllAndFlush(fakeOrderItems);

        WarehouseAdmin warehouseAdmin = WarehouseAdmin.builder()
                .id(UUID.randomUUID())
                .account(fakeAccounts.getFirst())
                .warehouse(fakeWarehouses.getFirst())
                .build();
        fakeWarehouseAdmins.add(warehouseAdmin);
        warehouseAdminRepository.saveAllAndFlush(fakeWarehouseAdmins);
    }

    public void depopulate() {
        orderStatusRepository.deleteAll(fakeOrderStatuses);
        fakeOrderStatuses.clear();
        orderItemRepository.deleteAll(fakeOrderItems);
        fakeOrderItems.clear();
        warehouseLedgerRepository.deleteAll(fakeWarehouseLedgers);
        fakeWarehouseLedgers.clear();
        orderRepository.deleteAll(fakeOrders);
        fakeOrders.clear();
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
        accountPermissionRepository.deleteAll(fakePermissions);
        fakePermissions.clear();
        accountAddressRepository.deleteAll(fakeAccountAddresses);
        fakeAccountAddresses.clear();
        accountRepository.deleteAll(fakeAccounts);
        fakeAccounts.clear();
        warehouseAdminRepository.deleteAll(fakeWarehouseAdmins);
        fakeWarehouseAdmins.clear();
    }

    public void auth() throws Exception {
        ResponseBody<AccountResponse> accountResponse = registerByInternal();
        authenticatedAccount = accountRepository
                .findById(accountResponse.getData().getId())
                .orElseThrow(AccountNotFoundException::new);
        fakeAccounts.add(authenticatedAccount);
        authenticatedSession = loginByInternal(authenticatedAccount).getData();
    }

    public void auth(Account account) throws Exception {
        authenticatedAccount = account;
        authenticatedSession = loginByInternal(account).getData();
    }

    public void deauth() throws Exception {
        logout(authenticatedSession);
    }

    protected ResponseBody<AccountResponse> registerByInternal() throws Exception {
        String email = String.format("email-%s", UUID.randomUUID());
        String type = "REGISTER";

        Verification verification = getVerification(email, type);

        RegisterByInternalRequest requestBody = RegisterByInternalRequest
                .builder()
                .name(String.format("name-%s", UUID.randomUUID()))
                .email(email)
                .password(rawPassword)
                .phone(String.format("phone-%s", UUID.randomUUID()))
                .otp(verification.getCode())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/registers/internal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<AccountResponse> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Register by internal succeed.");
        assert body.getData() != null;
        assert body.getData().getId() != null;
        assert body.getData().getName().equals(requestBody.getName());
        assert body.getData().getEmail().equals(requestBody.getEmail());
        assert securityConfiguration.matches(requestBody.getPassword(), body.getData().getPassword());
        assert body.getData().getPhone().equals(requestBody.getPhone());
        assert body.getData().getImage() == null;
        assert body.getData().getIsVerified().equals(true);

        Account registeredAccount = accountRepository
                .findById(body.getData().getId())
                .orElseThrow(AccountNotFoundException::new);
        fakeAccounts.add(registeredAccount);

        return body;
    }

    protected ResponseBody<Account> registerByExternal() throws Exception {
        String email = String.format("email-%s", UUID.randomUUID());
        String name = String.format("name-%s", UUID.randomUUID());
        String picture = "https://placehold.co/400x400";

        GoogleTokenResponse tokenResponse = Mockito.mock(GoogleTokenResponse.class);
        Mockito.when(googleConfiguration.getToken(Mockito.any())).thenReturn(tokenResponse);
        GoogleIdToken googleIdToken = Mockito.mock(GoogleIdToken.class);
        Mockito.when(tokenResponse.parseIdToken()).thenReturn(googleIdToken);
        GoogleIdToken.Payload payload = Mockito.mock(GoogleIdToken.Payload.class);
        Mockito.when(payload.getEmail()).thenReturn(email);
        Mockito.when(payload.get("name")).thenReturn(name);
        Mockito.when(payload.get("picture")).thenReturn(picture);
        Mockito.when(googleIdToken.getPayload()).thenReturn(payload);

        RegisterByExternalRequest requestBody = RegisterByExternalRequest
                .builder()
                .authorizationCode(UUID.randomUUID().toString())
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/registers/external")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isCreated())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Account> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        assert body != null;
        assert body.getMessage().equals("Register by external succeed.");
        assert body.getData() != null;
        assert body.getData().getId() != null;
        assert body.getData().getName().equals(name);
        assert body.getData().getEmail().equals(email);
        assert body.getData().getImage() != null;

        return body;
    }

    protected ResponseBody<Session> loginByInternal(Account account) throws Exception {
        LoginByInternalRequest requestBody = LoginByInternalRequest
                .builder()
                .email(account.getEmail())
                .password(rawPassword)
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/logins/internal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
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

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/authentications/logouts/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(session));

        MvcResult result = mockMvc
                .perform(httpRequest)
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
        Mockito
                .doNothing()
                .when(gmailGatewayMock)
                .sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        VerificationRequest requestBody = VerificationRequest
                .builder()
                .email(email)
                .type(type)
                .build();

        MockHttpServletRequestBuilder httpRequest = MockMvcRequestBuilders
                .post("/verifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));

        MvcResult result = mockMvc
                .perform(httpRequest)
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseBody<Void> body = objectMapper.readValue(content, new TypeReference<>() {
        });
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        assert body != null;
        assert body.getMessage().equals("Verification send succeed.");
        assert body.getData() == null;

        return verificationRepository
                .findByEmailAndType(email, type)
                .orElseThrow(VerificationNotFoundException::new);
    }
}