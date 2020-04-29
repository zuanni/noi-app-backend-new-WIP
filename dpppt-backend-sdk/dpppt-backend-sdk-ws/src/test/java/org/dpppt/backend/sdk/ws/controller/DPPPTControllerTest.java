/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.backend.sdk.ws.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dpppt.backend.sdk.data.DPPPTDataService;
import org.dpppt.backend.sdk.model.ExposedOverview;
import org.dpppt.backend.sdk.model.Exposee;
import org.dpppt.backend.sdk.model.ExposeeAuthData;
import org.dpppt.backend.sdk.model.ExposeeRequest;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@SpringBootTest(properties =
{
    "ws.app.jwt.publickey=classpath://generated_pub.pem"
 })
@AutoConfigureMockMvc
public class DPPPTControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean
    private DPPPTDataService dataService;

    @Captor
    private ArgumentCaptor<Exposee> exposeeCaptor;


    @Test
    public void testHello() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/v1"))
                .andExpect(status().is2xxSuccessful()).andReturn().getResponse();

        assertNotNull(response);
        assertEquals("Hello from DP3T WS", response.getContentAsString());
    }

    @Test
    public void testJWT() throws Exception {
        ExposeeRequest exposeeRequest = new ExposeeRequest();
        exposeeRequest.setAuthData(new ExposeeAuthData());
        exposeeRequest.setKeyDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli());
        exposeeRequest.setKey(Base64.getEncoder().encodeToString("test".getBytes("UTF-8")));
        exposeeRequest.setIsFake(0);
        String token = createToken(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).plusMinutes(5));
        MockHttpServletResponse response = mockMvc.perform(post("/v1/exposed")
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .header("Authorization", "Bearer " + token)
                                                            .header("User-Agent", "MockMVC")
                                                            .content(json(exposeeRequest)))
                .andExpect(status().is2xxSuccessful()).andReturn().getResponse();
        response = mockMvc.perform(post("/v1/exposed")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + jwtToken)
                                .header("User-Agent", "MockMVC")
                                .content(json(exposeeRequest)))
                .andExpect(status().is4xxClientError()).andReturn().getResponse();


    }
    @Test
    public void testJWTFake() throws Exception {
        ExposeeRequest exposeeRequest = new ExposeeRequest();
        exposeeRequest.setAuthData(new ExposeeAuthData());
        exposeeRequest.setKeyDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli());
        exposeeRequest.setKey(Base64.getEncoder().encodeToString("test".getBytes("UTF-8")));
        exposeeRequest.setIsFake(1);
        String token = createToken(true, OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).plusMinutes(5));
        MockHttpServletResponse response  = mockMvc.perform(post("/v1/exposed")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .header("User-Agent", "MockMVC")
                                .content(json(exposeeRequest)))
                .andExpect(status().is2xxSuccessful()).andReturn().getResponse();
        response = mockMvc.perform(post("/v1/exposed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .header("User-Agent", "MockMVC")
                .content(json(exposeeRequest)))
        .andExpect(status().is4xxClientError()).andReturn().getResponse();
    }

    @Test
    public void cannotUseSameTokenTwice() throws Exception {
        ExposeeRequest exposeeRequest = new ExposeeRequest();
        exposeeRequest.setAuthData(new ExposeeAuthData());
        exposeeRequest.setKeyDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli());
        exposeeRequest.setKey(Base64.getEncoder().encodeToString("test".getBytes("UTF-8")));
        exposeeRequest.setIsFake(0);
        String token = createToken(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).plusMinutes(5));

        MockHttpServletResponse response = mockMvc.perform(post("/v1/exposed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .header("User-Agent", "MockMVC")
                    .content(json(exposeeRequest)))
            .andExpect(status().is2xxSuccessful()).andReturn().getResponse();

        response = mockMvc.perform(post("/v1/exposed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .header("User-Agent", "MockMVC")
                .content(json(exposeeRequest)))
        .andExpect(status().is4xxClientError()).andReturn().getResponse();


    }

    @Test
    public void canUseSameTokenTwiceIfFake() throws Exception {
        ExposeeRequest exposeeRequest = new ExposeeRequest();
        exposeeRequest.setAuthData(new ExposeeAuthData());
        exposeeRequest.setKeyDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli());
        exposeeRequest.setKey(Base64.getEncoder().encodeToString("test".getBytes("UTF-8")));
        exposeeRequest.setIsFake(1);
        String token = createToken(true, OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).plusMinutes(5));

        MockHttpServletResponse response = mockMvc.perform(post("/v1/exposed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .header("User-Agent", "MockMVC")
                    .content(json(exposeeRequest)))
            .andExpect(status().is2xxSuccessful()).andReturn().getResponse();

        response = mockMvc.perform(post("/v1/exposed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .header("User-Agent", "MockMVC")
                .content(json(exposeeRequest)))
        .andExpect(status().is2xxSuccessful()).andReturn().getResponse();


    }

    @Test
    public void cannotUseExpiredToken() throws Exception {
        ExposeeRequest exposeeRequest = new ExposeeRequest();
        exposeeRequest.setAuthData(new ExposeeAuthData());
        exposeeRequest.setKeyDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli());
        exposeeRequest.setKey(Base64.getEncoder().encodeToString("test".getBytes("UTF-8")));
        exposeeRequest.setIsFake(0);
        String token = createToken(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).minusMinutes(5));

        MockHttpServletResponse response = mockMvc.perform(post("/v1/exposed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .header("User-Agent", "MockMVC")
                .content(json(exposeeRequest)))
        .andExpect(status().is4xxClientError()).andReturn().getResponse();
    }
    @Test
    public void cannotUseKeyDateInFuture() throws Exception {
        ExposeeRequest exposeeRequest = new ExposeeRequest();
        exposeeRequest.setAuthData(new ExposeeAuthData());
        exposeeRequest.setKeyDate(OffsetDateTime.now().plusDays(2).withOffsetSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli());
        exposeeRequest.setKey(Base64.getEncoder().encodeToString("test".getBytes("UTF-8")));
        exposeeRequest.setIsFake(0);
        String token = createToken(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).plusMinutes(5));

        MockHttpServletResponse response = mockMvc.perform(post("/v1/exposed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .header("User-Agent", "MockMVC")
                    .content(json(exposeeRequest)))
            .andExpect(status().is4xxClientError()).andReturn().getResponse();
    }
    @Test
    public void keyDateNotOlderThan21Days() throws Exception {
        ExposeeRequest exposeeRequest = new ExposeeRequest();
        exposeeRequest.setAuthData(new ExposeeAuthData());
        exposeeRequest.setKeyDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).minusDays(22).toInstant().toEpochMilli());
        exposeeRequest.setKey(Base64.getEncoder().encodeToString("test".getBytes("UTF-8")));
        exposeeRequest.setIsFake(0);
        String token = createToken(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).plusMinutes(5), "2020-01-01");

        MockHttpServletResponse response = mockMvc.perform(post("/v1/exposed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .header("User-Agent", "MockMVC")
                    .content(json(exposeeRequest)))
            .andExpect(status().is4xxClientError()).andReturn().getResponse();
    }


    @Test
    public void shouldReturnNotModifiedWithSameEtag() throws Exception {

        // given
        Exposee exposee = getExposee();
        String date = "2020-12-30";
        when(dataService.getMaxExposedIdForDay(any())).thenReturn(10);
        when(dataService.getSortedExposedForDay(any()))
                .thenReturn(Collections.singletonList(exposee));

        ExposeeRequest creationRequest = getExposeeRequest(exposee);

        // Create an exposee
        mockMvc.perform(post("/v1/exposed")
                .content(objectMapper.writeValueAsString(creationRequest))
                .contentType("application/json")
                .header("User-Agent", "user-agent")
        ).andExpect(status().isOk())
                .andExpect(content().string(""));

        // when (1)
        // request the list of exposees for the day without ETag, get an ETag
        MvcResult getWithoutETag = mockMvc.perform(
                get("/v1/exposed/" + date))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        new ExposedOverview(Collections.singletonList(exposee)))))
                .andExpect(header().exists(HttpHeaders.ETAG)).andReturn();

        String receivedETagValue = getWithoutETag.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(receivedETagValue).isNotEmpty();

        // and when (2)
        // request the expositions for the day with the received ETag, get a http 304 unmodified response
        mockMvc.perform(
                get("/v1/exposed/" + date)
                        .header("If-None-Match", receivedETagValue))
                .andExpect(status().isNotModified())
                .andExpect(jsonPath("$").doesNotExist())
                .andExpect(header().string(HttpHeaders.ETAG, receivedETagValue));
    }


    @Test
    public void shouldReturnOKWithDifferentEtag() throws Exception {

        // given
        Exposee exposee = getExposee();
        String date = "2020-12-30";
        when(dataService.getMaxExposedIdForDay(any())).thenReturn(10);
        when(dataService.getSortedExposedForDay(any()))
                .thenReturn(Collections.singletonList(exposee));

        ExposeeRequest creationRequest = getExposeeRequest(exposee);

        // Create an exposee
        mockMvc.perform(post("/v1/exposed")
                .content(objectMapper.writeValueAsString(creationRequest))
                .contentType("application/json")
                .header("User-Agent", "user-agent")
        ).andExpect(status().isOk())
                .andExpect(content().string(""));

        // when (1)
        // request the list of exposees for the day without ETag, get an ETag
        MvcResult resultOfGetWithoutETag = mockMvc.perform(
                get("/v1/exposed/" + date))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        new ExposedOverview(Collections.singletonList(exposee)))))
                .andExpect(header().exists(HttpHeaders.ETAG)).andReturn();

        String receivedETagValue = resultOfGetWithoutETag.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(receivedETagValue).isNotEmpty();

        final String anotherETag = "\"--I'm-a-non-existing-ETag--\"";

        // and when (2)
        // request the expositions for the day with the received ETag, get a http 304 unmodified response
        MvcResult resultOfGetWithNonExistingETag = mockMvc.perform(
                get("/v1/exposed/" + date)
                        .header("If-None-Match", anotherETag))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, receivedETagValue))
                .andReturn();

        String receivedETagValue2 = resultOfGetWithNonExistingETag.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(receivedETagValue2).isNotEmpty();
        assertThat(anotherETag).isNotEqualTo(receivedETagValue2);

    }

    private ExposeeRequest getExposeeRequest(Exposee exposee) {
        ExposeeRequest creationRequest = new ExposeeRequest();
        creationRequest.setKey(exposee.getKey());
        creationRequest.setKeyDate(exposee.getKeyDate());
        creationRequest.setAuthData(new ExposeeAuthData());
        return creationRequest;
    }

    private Exposee getExposee() {
        Exposee exposee = new Exposee();
        exposee.setId(1);
        exposee.setKey("key");
        exposee.setKeyDate(111);
        return exposee;
    }
}