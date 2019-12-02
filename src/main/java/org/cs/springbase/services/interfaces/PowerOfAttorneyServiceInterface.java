package org.cs.springbase.services.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public interface PowerOfAttorneyServiceInterface {

	Map<String, List<JsonNode>> powerOfAttorneys();

	Collection<? extends JsonNode> fetchActiveCards(JsonNode cardArrayNode);

}