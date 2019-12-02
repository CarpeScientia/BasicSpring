package org.cs.springbase.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PowerOfAttorney {
	private static final String POA_ID = "id";
	private static String GET_ALL_POAS = "/power-of-attorneys";
	private static String GIVEN_KEY = "grantee";
	
	Logger log = LoggerFactory.getLogger(PowerOfAttorney.class);
	
	@Value( "${powerofattorney.url}" )
	private String poaUrl;
	
	@Cacheable("powerOfAtterneys")//TODO set timed eviction policy
	public Map<String, List<JsonNode>> powerOfAttorneys(){
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = 
				restTemplate.getForEntity(poaUrl + GET_ALL_POAS, String.class);
		if(response.getStatusCode() != HttpStatus.OK){
			log.warn("got nothing from powerofattorney service");
			return null;
		}
		JsonNode root = makeJsonRoot(response);//result should be a JsonArray
		Map<String, List<JsonNode>> result = new HashMap<>();
		root.elements().forEachRemaining(node -> {
			addPOAToMapWithKey(node, result, GIVEN_KEY);
		});
		return result;
	}

	private JsonNode makeJsonRoot(ResponseEntity<String> response) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root;
		try {
			root = mapper.readTree(response.getBody());
		} catch (JsonProcessingException e) {
			log.warn("got something unmappable from powerofattorney service", e);
			return null;
		}
		return root;
	}
	
	private void addPOAToMapWithKey(JsonNode node, Map<String, List<JsonNode>> result, String nodeKey) {
		ResponseEntity<String> response = fetchPOAForId(node);
		if( response == null) {
			return;
		}
		JsonNode powerOfAttorney = makeJsonRoot(response);//result should be a single JsonNode
		if( powerOfAttorney == null) {
			return;
		}
		JsonNode givenId = powerOfAttorney.get(nodeKey);
		if(givenId == null) {
			log.warn("no key found with name " + nodeKey + " in json object " + powerOfAttorney);
			return;
		}
		result.putIfAbsent(givenId.asText(), new ArrayList<>());
		result.get(givenId.asText()).add( powerOfAttorney);
	}

	private ResponseEntity<String> fetchPOAForId(JsonNode idNode) {
		RestTemplate restTemplate = new RestTemplate();
		JsonNode poaId = idNode.get(POA_ID);
		ResponseEntity<String> response = 
				restTemplate.getForEntity(poaUrl + GET_ALL_POAS + "/" + poaId.asText(), 
						String.class);
		if(response.getStatusCode() != HttpStatus.OK){
			log.warn("got nothing from powerofattorney service for id " + poaId);
			return null;
		}
		return response;
	}
}
