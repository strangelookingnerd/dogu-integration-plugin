package io.dogutech.jenkins.api;

import com.google.gson.Gson;
import io.dogutech.jenkins.DoguOption;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    public class RunRoutineResponse {
        public int routinePipelineId;
        public String projectId;
        public String routineId;
        public int index;
        public String creatorType;
        public String creatorId;
        public String createdAt;
    }

    public static RunRoutineResponse runRoutine(String projectId, String routineId) throws Exception {
        String url = DoguOption.API_URL + "/v1/projects/" + projectId + "/routines/" + routineId + "/pipelines";
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
                .header("Authorization", "Bearer " + DoguOption.DOGU_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw e;
        }

        int status = response.statusCode();
        if (status < 200 || status >= 400) {
            throw new Exception("Failed to run routine: " + status + "\n" + response.body());
        }

        RunRoutineResponse runRoutineResponse = new Gson().fromJson(response.body(), RunRoutineResponse.class);

        return runRoutineResponse;
    }

    public static DoguWebSocketClient connectRoutine(
            PrintStream logger, String projectId, String routineId, int routinePipelineId) throws Exception {
        String url = DoguOption.getWebSocketUrl(logger) + "/v1/pipeline-state?projectId=" + projectId + "&routineId="
                + routineId + "&pipelineId=" + routinePipelineId;

        URI uri = new URI(url);
        DoguWebSocketClient client = new DoguWebSocketClient(uri, logger);
        client.addHeader("Authorization", "Bearer " + DoguOption.DOGU_TOKEN);
        client.connect();

        return client;
    }
}
