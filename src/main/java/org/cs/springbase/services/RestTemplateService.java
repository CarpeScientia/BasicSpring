package org.cs.springbase.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class RestTemplateService {

    @Value("${endpoint.property:http://localhost:80}")
    private String url;
    @Autowired
    @Qualifier("httpRestTemplate")//optional bean name
    private RestTemplate restTemplate;// = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();
    private Logger log = LoggerFactory.getLogger(RestTemplateService.class);
    private HttpHeaders headers = new HttpHeaders();//thread safety assumed

    public RestTemplateService() {
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    public Object getMethod(String param) {
        return getRequest(new Object(), Object.class, "getpath");
    }

    public Object postMethod(String param) {
        return postRequest(new Object(),
                Object.class,
                "postpath");
    }

    private <R, T> T postRequest(R request, Class<T> clazz, String path) {
        ResponseEntity<String> result;
        MultiValueMap<String, String> formRequest = convert(request);
        try {
            result = restTemplate.postForEntity(
                    url + path,
                    new HttpEntity<>(
                            formRequest,
                            headers),
                    String.class);
        } catch (Exception e) {
            log.warn("Failed to process json request for " + path, e);
            return null;
        }
        try {
            T response =
                    objectMapper.readValue(
                            Objects.requireNonNull(result.getBody()),
                            clazz);
            return response;
        } catch (JsonProcessingException e) {
            log.warn("Failed to process json result for " + path, e);
        }
        return null;
    }

    private <R, T> T getRequest(R request, Class<T> clazz, String path) {
        ResponseEntity<T> response;
        UriComponents uri =  UriComponentsBuilder.fromHttpUrl(url + path)
                .queryParams(convert(request))
                .build();
        try {
            response = restTemplate.exchange(
                    uri.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(
                            null,
                            headers),
                    clazz);
        } catch (Exception e) {
            log.warn("Failed to process get request for " + path, e);
            return null;
        }
        return response.getBody();
    }


    private MultiValueMap<String, String> convert(Object obj) {
        MultiValueMap<String,String> parameters = new LinkedMultiValueMap<>();
        Map<String, String> maps = objectMapper.convertValue(obj, new TypeReference<>() {
        });
        parameters.setAll(maps);

        return parameters;
    }

}
