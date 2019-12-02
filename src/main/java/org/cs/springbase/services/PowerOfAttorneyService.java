package org.cs.springbase.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cs.springbase.services.interfaces.PowerOfAttorneyServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PowerOfAttorneyService implements PowerOfAttorneyServiceInterface {
	private static final String POA_ID = "id";
	private static final String GET_ALL_POAS = "/power-of-attorneys";
	private static final String GIVEN_KEY = "grantee";
	private static final String CARD_ARRAY_KEY = "cards";
	private static final String POA_TYPE = "type";
	private static final String POA_STATUS = "status";
	private static final String POA_STATUS_ACTIVE = "ACTIVE";

	Logger log = LoggerFactory.getLogger(PowerOfAttorneyService.class);
	@Autowired
	RestTemplate restTemplate;//docs say it is threadsafe

	@Value( "${powerofattorney.url}" )
	private String poaUrl;

	@Override
	@Cacheable("powerOfAtterneys")//TODO set timed eviction policy
	public Map<String, List<JsonNode>> powerOfAttorneys(){
		ResponseEntity<String> response;
		try {
			response = 
					restTemplate.getForEntity(poaUrl + GET_ALL_POAS, String.class);
		}catch( Exception e){
			log.warn("Exception while connecting to  powerofattorney service", e);
			return Collections.emptyMap();
		}
		if(response.getStatusCode() != HttpStatus.OK){
			log.warn("got nothing from powerofattorney service");
			return Collections.emptyMap();
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
			return new ObjectMapper().createObjectNode();
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
		JsonNode poaId = idNode.get(POA_ID);
		ResponseEntity<String> response;
		try {
			response = restTemplate.getForEntity(poaUrl + GET_ALL_POAS + "/" + poaId.asText(), 
					String.class);
		}catch(Exception e) {
			log.warn("got exception from powerofattorney service for id " + poaId, e);
			return null;
		}
		if(response.getStatusCode() != HttpStatus.OK){
			log.warn("got nothing from powerofattorney service for id " + poaId);
			return null;
		}
		return response;
	}

	@Override
	public Collection<? extends JsonNode> fetchActiveCards(JsonNode cardArrayNode) {
		if( !cardArrayNode.has(CARD_ARRAY_KEY)) {
			if(log.isDebugEnabled()) log.debug("No cards json array found in " + cardArrayNode );
			return Collections.emptyList();
		}
		List<JsonNode> activeCards = new ArrayList<>();
		JsonNode cardArray = cardArrayNode.get(CARD_ARRAY_KEY);
		if( !cardArray.isArray() ) {
			log.warn("cards is not a json array in " + cardArrayNode );
			return Collections.emptyList();
		}
		cardArray.forEach(aCardId->{
			ResponseEntity<String> response = fetchCardForId(aCardId);
			if( response == null) {
				return;
			}
			JsonNode completeCard = makeJsonRoot(response);//result should be a single JsonNode
			if( completeCard == null) {
				return;
			}
			if(completeCard.has(POA_STATUS) && 
					POA_STATUS_ACTIVE.equals(completeCard.get(POA_STATUS).asText()) ) {
				activeCards.add(completeCard);
			}
		});
		return activeCards;
	}

	private ResponseEntity<String> fetchCardForId(JsonNode cardIdNode) {
		JsonNode cardId = cardIdNode.get(POA_ID);
		JsonNode cardType = cardIdNode.get(POA_TYPE);
		if(cardType == null) {
			log.warn("no type found for card " + cardIdNode);
			return null;
		}
		String cardTypePath = cardType.asText().toLowerCase().replace('_', '-') + "s";
		ResponseEntity<String> response;
		try {
			response = restTemplate.getForEntity(poaUrl + "/" + cardTypePath + "/" + cardId.asText(), 
					String.class);
		}catch(Exception e) {
			log.warn("got exception from powerofattorney card service for id " + cardId, e);
			return null;
		}
		if(response.getStatusCode() != HttpStatus.OK){
			log.warn("got nothing from powerofattorney card service for id " + cardId);
			return null;
		}
		return response;
	}
}
