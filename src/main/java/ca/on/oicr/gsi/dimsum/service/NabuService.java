package ca.on.oicr.gsi.dimsum.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ca.on.oicr.gsi.dimsum.data.NabuBulkSignoff;

@Service
@ConditionalOnProperty(name = "nabu.url")
public class NabuService {

  private final WebClient client;

  public NabuService(@Value("${nabu.url}") String baseUrl, @Value("${nabu.apikey}") String apiKey) {
    this.client = WebClient.builder()
        .baseUrl(baseUrl).defaultHeader("X-API-KEY", apiKey)
        .build();
  }

  public void postSignoff(NabuBulkSignoff signoff) {
    client.post()
        .uri("/case/sign-off")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(signoff)
        .retrieve().toBodilessEntity()
        .block();
  }

}
