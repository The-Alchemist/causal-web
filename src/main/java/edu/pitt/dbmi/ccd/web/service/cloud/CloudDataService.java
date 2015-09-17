/*
 * Copyright (C) 2015 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.ccd.web.service.cloud;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * Aug 21, 2015 9:36:34 AM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
@Service
public class CloudDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudDataService.class);

    private final Boolean webapp;

    private final RestTemplate restTemplate;

    private final String dataRestUrl;

    private final String appId;

    @Autowired(required = true)
    public CloudDataService(
            Boolean webapp,
            RestTemplate restTemplate,
            @Value("${ccd.rest.url.data:http://localhost:9000/ccd-ws/data}") String dataRestUrl,
            @Value("${ccd.rest.appId:1}") String appId) {
        this.webapp = webapp;
        this.restTemplate = restTemplate;
        this.dataRestUrl = dataRestUrl;
        this.appId = appId;
    }

    public Set<String> getDataMd5Hash(String username) {
        Set<String> hashes = new HashSet<>();

        if (!webapp) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            URI url = UriComponentsBuilder.fromHttpUrl(dataRestUrl + "/hash")
                    .queryParam("usr", username)
                    .queryParam("appId", this.appId)
                    .build().toUri();
            try {
                HttpEntity<?> entity = new HttpEntity<>(headers);
                ResponseEntity<Set> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Set.class);
                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    hashes.addAll(responseEntity.getBody());
                }
            } catch (RestClientException exception) {
                LOGGER.error(exception.getMessage());
            }
        }

        return hashes;
    }

}
