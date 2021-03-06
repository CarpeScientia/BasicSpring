package org.cs.springbase.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.cs.springbase.services.interfaces.PowerOfAttorneyServiceInterface;
import org.cs.springbase.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class BasicRestController {
	Logger log = LoggerFactory.getLogger(BasicRestController.class);

	@Autowired
	private PowerOfAttorneyServiceInterface powerOfAttorney;

	@RequestMapping("/listAccounts")
	public List<JsonNode> listAccounts(Principal principal) {
		log.info("getting account for principal: " + principal.getName());
		List<JsonNode> result =  
				powerOfAttorney.powerOfAttorneys().get(UserUtil.underscoresToSpaces(principal.getName()));
		if(log.isDebugEnabled()) log.debug("getting account for principal: " 
				+ UserUtil.underscoresToSpaces(principal.getName()));
		return result;
	}
	
	@RequestMapping("/listCards")
	public List<JsonNode> listCards(Principal principal) {
		log.info("getting cards for principal: " + principal.getName());
		List<JsonNode> paos =  
				powerOfAttorney.powerOfAttorneys().get(UserUtil.underscoresToSpaces(principal.getName()));
		List<JsonNode> cards =  new ArrayList<>();
		paos.forEach(aPao->{
			cards.addAll(powerOfAttorney.fetchActiveCards(aPao));
		});
		if(log.isDebugEnabled()) log.debug("getting card for principal: " 
				+ UserUtil.underscoresToSpaces(principal.getName()));
		return cards;
	}
}