package com.aac.ieojwo;

import com.aac.ieojwo.account.domain.AccountType;
import com.aac.ieojwo.auth.dto.OnboardingRequest;
import com.aac.ieojwo.auth.service.AuthService;
import com.aac.ieojwo.device.repository.AacDeviceRepository;
import com.aac.ieojwo.guardian.repository.UserGuardianRepository;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.oidc.*;
import org.springframework.security.oauth2.core.oidc.user.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Transactional
class OnboardingAndPairingIntegrationTests {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired EntityManager entityManager; @Autowired AuthService auth; @Autowired UserGuardianRepository links; @Autowired AacDeviceRepository devices; @Autowired JdbcTemplate jdbc;
 @Test void profileValidationAndRelationshipArePersisted() throws Exception {
  onboard("profile","profile@example.com");
  Map<String,Object> missingBirth=new HashMap<>();missingBirth.put("name","민수");missingBirth.put("relationshipType","PARENT");missingBirth.put("emergencyContact","01012345678");
  mvc.perform(post("/api/v1/me/aac-users").with(oidcLogin().idToken(t->t.subject("profile"))).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(missingBirth))).andExpect(status().isBadRequest());
  Map<String,Object> missingContact=new HashMap<>();missingContact.put("name","민수");missingContact.put("birthDate","2012-01-15");missingContact.put("relationshipType","PARENT");
  mvc.perform(post("/api/v1/me/aac-users").with(oidcLogin().idToken(t->t.subject("profile"))).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(missingContact))).andExpect(status().isBadRequest());
  mvc.perform(post("/api/v1/me/aac-users").with(oidcLogin().idToken(t->t.subject("profile"))).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(profile(Map.of("birthDate","2999-01-01")))).andExpect(status().isBadRequest());
  mvc.perform(post("/api/v1/me/aac-users").with(oidcLogin().idToken(t->t.subject("profile"))).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(profile(Map.of("relationshipType","OTHER")))).andExpect(status().isBadRequest());
  Map<String,Object> valid=new HashMap<>();valid.put("relationshipType","OTHER");valid.put("relationshipDetail","이모");long id=create("profile",valid);
  assertThat(links.findAllByUserIdOrderByPrimaryGuardianDescIdAsc(id)).singleElement().satisfies(l->{assertThat(l.isPrimaryGuardian()).isTrue();assertThat(l.getRelationshipType().name()).isEqualTo("OTHER");assertThat(l.getRelationshipDetail()).isEqualTo("이모");});
 }
 @Test void gridVoiceSummaryAndOwnershipFollowOnboardingSteps() throws Exception {
  onboard("setup-a","setup-a@example.com");onboard("setup-b","setup-b@example.com");long id=create("setup-a",Map.of());
  mvc.perform(get("/api/v1/me/aac-users/{id}/onboarding-summary",id).with(oidcLogin().idToken(t->t.subject("setup-a")))).andExpect(status().isOk()).andExpect(jsonPath("$.data.gridSize").value("GRID_3X3")).andExpect(jsonPath("$.data.voiceType").value("CHILD_MALE")).andExpect(jsonPath("$.data.speechRate").value(1.0));
  for(String grid:List.of("GRID_2X2","GRID_3X3","GRID_4X4"))mvc.perform(patch("/api/v1/me/aac-users/{id}/onboarding/grid",id).with(oidcLogin().idToken(t->t.subject("setup-a"))).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("gridSize",grid)))).andExpect(status().isOk());
  for(double rate:List.of(.7,1.3))mvc.perform(patch("/api/v1/me/aac-users/{id}/voice-settings",id).with(oidcLogin().idToken(t->t.subject("setup-a"))).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("voiceType","CHILD_FEMALE","speechRate",rate)))).andExpect(status().isOk());
  for(double rate:List.of(.69,1.31))mvc.perform(patch("/api/v1/me/aac-users/{id}/voice-settings",id).with(oidcLogin().idToken(t->t.subject("setup-a"))).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("voiceType","CHILD_MALE","speechRate",rate)))).andExpect(status().isBadRequest());
  mvc.perform(get("/api/v1/me/aac-users/{id}/onboarding-summary",id).with(oidcLogin().idToken(t->t.subject("setup-b")))).andExpect(status().isForbidden());
 }
 @Test void refreshRevokesOldCredentialAndCodeClaimCreatesDeviceOnce() throws Exception {
  onboard("pair","pair@example.com");long id=create("pair",Map.of());JsonNode first=issue("pair",id,"/device-pairings");String oldToken=token(first);JsonNode second=issue("pair",id,"/device-pairings/refresh");assertThat(second.path("remainingSeconds").asLong()).isBetween(599L,600L);assertThat(second.path("inviteCode").asText()).matches("\\d{6}");
  claimQr(oldToken,"old-device").andExpect(status().isConflict());String code=second.path("inviteCode").asText();claimCode(code,"tablet-1").andExpect(status().isOk()).andExpect(jsonPath("$.data.aacUserId").value(id));claimCode(code,"tablet-1").andExpect(status().isConflict());assertThat(devices.findByDeviceId("tablet-1")).isPresent();
 }
 @Test void qrClaimExpiryUnknownCodeAndUnauthorizedIssueHaveDistinctResults() throws Exception {
  onboard("owner","owner@example.com");onboard("other","other@example.com");long id=create("owner",Map.of());
  mvc.perform(post("/api/v1/me/aac-users/{id}/device-pairings",id).with(oidcLogin().idToken(t->t.subject("other"))).with(csrf())).andExpect(status().isForbidden());
  JsonNode issued=issue("owner",id,"/device-pairings");String token=token(issued);jdbc.update("update device_pairing_sessions set expires_at=? where id=?",java.sql.Timestamp.from(Instant.now().minusSeconds(1)),issued.path("pairingId").asLong());entityManager.clear();claimQr(token,"expired").andExpect(status().isGone());entityManager.flush();assertThat(jdbc.queryForObject("select status from device_pairing_sessions where id=?",String.class,issued.path("pairingId").asLong())).isEqualTo("EXPIRED");
  claimCode("999999","unknown").andExpect(status().isNotFound());JsonNode fresh=issue("owner",id,"/device-pairings");claimQr(token(fresh),"qr-device").andExpect(status().isOk());
 }
 private JsonNode issue(String sub,long id,String suffix)throws Exception{return json.readTree(mvc.perform(post("/api/v1/me/aac-users/{id}"+suffix,id).with(oidcLogin().idToken(t->t.subject(sub))).with(csrf())).andExpect(status().is2xxSuccessful()).andReturn().getResponse().getContentAsString()).path("data");}
 private String token(JsonNode n){String p=n.path("qrPayload").asText();return p.substring(p.indexOf("token=")+6);}
 private org.springframework.test.web.servlet.ResultActions claimQr(String token,String device)throws Exception{return mvc.perform(post("/api/v1/device-pairings/claim/qr").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("token",token,"deviceId",device,"deviceType","TABLET"))));}
 private org.springframework.test.web.servlet.ResultActions claimCode(String code,String device)throws Exception{return mvc.perform(post("/api/v1/device-pairings/claim/code").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("code",code,"deviceId",device,"deviceType","TABLET"))));}
 private long create(String sub,Map<String,Object> overrides)throws Exception{String body=mvc.perform(post("/api/v1/me/aac-users").with(oidcLogin().idToken(t->t.subject(sub))).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(profile(overrides))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();return json.readTree(body).path("data").path("id").asLong();}
 private String profile(Map<String,Object> overrides)throws Exception{Map<String,Object> m=new HashMap<>();m.put("name","민수");m.put("birthDate","2012-01-15");m.put("relationshipType","PARENT");m.put("emergencyContact","01012345678");m.putAll(overrides);return json.writeValueAsString(m);}
 private void onboard(String sub,String email){auth.upsertGoogleAccount(sub,email,"보호자",null);auth.completeOnboarding(principal(sub,email),new OnboardingRequest(AccountType.GUARDIAN,true,true,false,"01012345678"));}
 private OidcUser principal(String sub,String email){Instant now=Instant.now();OidcIdToken token=new OidcIdToken("t-"+sub,now,now.plusSeconds(300),Map.of("sub",sub,"email",email));return new DefaultOidcUser(List.of(),token,OidcUserInfo.builder().subject(sub).email(email).build());}
}
