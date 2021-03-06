/* Copyright (c) 2018, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.lfenergy.operatorfabric.users.controllers;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.operatorfabric.users.application.UnitTestApplication;
import org.lfenergy.operatorfabric.users.application.configuration.WithMockOpFabUser;
import org.lfenergy.operatorfabric.users.model.UserData;
import org.lfenergy.operatorfabric.users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.bus.ServiceMatcher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * <p></p>
 * Created on 13/09/18
 *
 * @author David Binder
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UnitTestApplication.class)
@ActiveProfiles("test")
@WebAppConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("end-to-end")
@Tag("mongo")
@WithMockOpFabUser(login="testAdminUser", roles = { "ADMIN" })
class GivenAdminUserUsersControllerShould {

    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    @MockBean
    private ServiceMatcher busServiceMatcher;

    @MockBean
    private ApplicationEventPublisher publisher;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeAll
    void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    public void init(){
        UserData u1,u2,u3;
        u1 = UserData.builder()
           .login("jcleese")
           .firstName("John")
           .lastName("Cleese")
           .group("Monty Pythons").group("Wanda")
           .build();
        u2 = UserData.builder()
           .login("gchapman")
           .firstName("Graham")
           .lastName("Chapman")
           .group("Monty Pythons")
           .build();
        u3 = UserData.builder()
           .login("kkline")
           .firstName("Kevin")
           .lastName("Kline")
           .group("Wanda")
           .build();
        repository.insert(u1);
        repository.insert(u2);
        repository.insert(u3);
    }

    @AfterEach
    public void clean(){
        repository.deleteAll();
    }

    @Test
    void fetchAll() throws Exception {
        ResultActions result = mockMvc.perform(get("/users"));
        result
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(jsonPath("$", hasSize(3)))
        ;
    }

    @Test
    void fetch() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/gchapman"));
        result
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(jsonPath("$.login", is("gchapman")))
           .andExpect(jsonPath("$.firstName", is("Graham")))
           .andExpect(jsonPath("$.lastName", is("Chapman")))
           .andExpect(jsonPath("$.groups", contains("Monty Pythons")))
        ;
    }

    @Test
    void fetchWithError() throws Exception {
        ResultActions result = mockMvc.perform(get("/users/tgillian"));
        result
           .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
           .andExpect(jsonPath("$.message", is(String.format(UsersController.USER_NOT_FOUND_MSG,"tgillian"))))
           .andExpect(jsonPath("$.errors").doesNotExist())
        ;
    }

    @Test
    void create() throws Exception {
        mockMvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON_UTF8)
               .content("{" +
                    "\"login\": \"mpalin\","+
                    "\"firstName\": \"Michael\","+
                    "\"lastName\": \"Palin\""+
                  "}")
            )
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(jsonPath("$.login", is("mpalin")))
           .andExpect(jsonPath("$.firstName", is("Michael")))
           .andExpect(jsonPath("$.lastName", is("Palin")))
        ;

        mockMvc.perform(get("/users"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(jsonPath("$", hasSize(4)));

        mockMvc.perform(get("/users/mpalin"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(jsonPath("$.login", is("mpalin")))
           .andExpect(jsonPath("$.firstName", is("Michael")))
           .andExpect(jsonPath("$.lastName", is("Palin")))
        ;

    }

    @Test
    void createWithDuplicateError() throws Exception {

        mockMvc.perform(get("/users/kkline"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.login", is("kkline")))
                .andExpect(jsonPath("$.firstName", is("Kevin")))
                .andExpect(jsonPath("$.lastName", is("Kline")))
        ;

        ResultActions result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{" +
                        "\"login\": \"kkline\","+
                        "\"firstName\": \"KEVIN\","+
                        "\"lastName\": \"KLINE\""+
                        "}")
        )
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.name())))
                .andExpect(jsonPath("$.message", is("Resource creation failed because a resource with the same key already exists.")))
                .andExpect(jsonPath("$.errors",hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",stringContainsInOrder(Arrays.asList("duplicate key","\"kkline\""))))
                ;

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/users/kkline"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.login", is("kkline")))
                .andExpect(jsonPath("$.firstName", is("Kevin")))
                .andExpect(jsonPath("$.lastName", is("Kline")))
        ;

    }

    @Test
    void update() throws Exception {
        mockMvc.perform(put("/users/kkline")
           .contentType(MediaType.APPLICATION_JSON_UTF8)
           .content("{" +
              "\"login\": \"kkline\","+
              "\"firstName\": \"Kevin\","+
              "\"lastName\": \"KLINE\""+
              "}")
        )
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(jsonPath("$.login", is("kkline")))
           .andExpect(jsonPath("$.firstName", is("Kevin")))
           .andExpect(jsonPath("$.lastName", is("KLINE")))
        ;

        mockMvc.perform(get("/users"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/users/kkline"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
           .andExpect(jsonPath("$.login", is("kkline")))
           .andExpect(jsonPath("$.firstName", is("Kevin")))
           .andExpect(jsonPath("$.lastName", is("KLINE")))
        ;

    }

    @Test
    void updateWithNotFoundError() throws Exception {

        mockMvc.perform(get("/users/unknownSoFar"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.message", is(String.format(UsersController.USER_NOT_FOUND_MSG,"unknownSoFar"))))
                .andExpect(jsonPath("$.errors").doesNotExist())
        ;

        mockMvc.perform(put("/users/unknownSoFar")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{" +
                        "\"login\": \"unknownSoFar\","+
                        "\"firstName\": \"John\","+
                        "\"lastName\": \"Doe\""+
                        "}")
        )
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.message", is(String.format(UsersController.USER_NOT_FOUND_MSG,"unknownSoFar"))))
                .andExpect(jsonPath("$.errors").doesNotExist())
        ;

        mockMvc.perform(get("/users/unknownSoFar"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.message", is(String.format(UsersController.USER_NOT_FOUND_MSG,"unknownSoFar"))))
                .andExpect(jsonPath("$.errors").doesNotExist())
        ;
    }

    @Test
    void updateWithMismatchedError() throws Exception {

        mockMvc.perform(get("/users/kkline"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.login", is("kkline")))
                .andExpect(jsonPath("$.firstName", is("Kevin")))
                .andExpect(jsonPath("$.lastName", is("Kline")))
        ;

        mockMvc.perform(put("/users/kkline")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{" +
                        "\"login\": \"mpalin\","+
                        "\"firstName\": \"Kevin\","+
                        "\"lastName\": \"Palin\""+
                        "}")
                )
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.name())))
                .andExpect(jsonPath("$.message", is(UsersController.NO_MATCHING_USER_NAME_MSG)))
                .andExpect(jsonPath("$.errors").doesNotExist());

        mockMvc.perform(get("/users/kkline"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.login", is("kkline")))
                .andExpect(jsonPath("$.firstName", is("Kevin")))
                .andExpect(jsonPath("$.lastName", is("Kline")))
        ;

    }

    @Test
    void updateWithMismatchedAndNotFoundError() throws Exception {

        mockMvc.perform(get("/users/unknownSoFar"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.message", is(String.format(UsersController.USER_NOT_FOUND_MSG,"unknownSoFar"))))
                .andExpect(jsonPath("$.errors").doesNotExist())
        ;

        mockMvc.perform(put("/users/unknownSoFar")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content("{" +
                        "\"login\": \"someOtherLogin\","+
                        "\"firstName\": \"John\","+
                        "\"lastName\": \"Doe\""+
                        "}")
        )
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.message", is(String.format(UsersController.USER_NOT_FOUND_MSG,"unknownSoFar"))))
                .andExpect(jsonPath("$.errors").doesNotExist())
        ;

        mockMvc.perform(get("/users/unknownSoFar"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.message", is(String.format(UsersController.USER_NOT_FOUND_MSG,"unknownSoFar"))))
                .andExpect(jsonPath("$.errors").doesNotExist())
        ;

    }

}