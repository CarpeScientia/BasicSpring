# Basic Spring Rest example
  - Some Spring templates/examples
  - A cloned git submodule for PowerOfAttorney Rest Service, with some build fixes.
  - A service client for the PowerOfAttorney Rest Service
  - Rest call: /listAccounts : list granted accounts for the current logged-in user
  - Rest call: /listCards : list accounts for the current logged-in user
  - Todo: add some mocking tests, cache eviction policy, [B,S]crypt for passwords, better UI, more calls, etc. etc.

**Clone with:** `git clone --recurse-submodules`  or `git clone --recursive` depending on git version or run `git submodule update --init --recursive` after normal clone  
**To build and run:** use `mvn -f powerOfAttorneyService/pom.xml compile exec:java && mvn spring-boot:run`  
**Application runs on:** https://localhost:8443  

# Accounts
  - "user", "password"
  - "Super_duper_employee", "password"
  - "Super_duper_company", "password"
  - "Fellowship_of_the_ring", "password"

  
# Dependencies
  - Java 11, Spring-Boot & Maven
  - Doesn't return inactive products 
  - Exposed the API over HTTPS
 

