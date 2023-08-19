package io.dogutech.jenkins;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
  public static void runRoutine(String projectId, String routineId, String accessToken) throws Exception {
    String url = "https://dev.api.dogutech.io" + "/v1/projects/" + projectId + "/routines/" + routineId + "/pipelines";
    URI uri;

    try {
        uri = new URI(url);
    } catch (URISyntaxException e) {
      throw e;
    }

    HttpClient httpClient = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
    .uri(uri)
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer " +  accessToken)
    .POST(HttpRequest.BodyPublishers.ofString("{}"))
    .build();
    
    HttpResponse<String> response;
    try {
        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      throw e;
    }
  
  }
}
