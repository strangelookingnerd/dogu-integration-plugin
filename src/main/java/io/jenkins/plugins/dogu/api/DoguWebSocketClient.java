package io.jenkins.plugins.dogu.api;

import com.google.gson.Gson;
import java.io.PrintStream;
import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class DoguWebSocketClient extends WebSocketClient {
    public enum State {
        NONE,
        SUCCESS,
        FAILURE,
    }

    public class PipelineState {
        public int routinePipelineId;
        public String projectId;
        public String routineId;
        public int index;
        public String state;
        public String creatorType;
        public String creatorId;
        public String cancelerId;
        public String createdAt;
        public String inProgressAt;
        public String completedAt;
        public String resultUrl;
    }

    public State state = State.NONE;
    private PrintStream logger;

    public DoguWebSocketClient(URI serverUri, PrintStream logger) {
        super(serverUri);

        this.logger = logger;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.println("Wait for routine to finish");
    }

    @Override
    public void onMessage(String message) {
        PipelineState pipelineState = new Gson().fromJson(message, PipelineState.class);

        switch (pipelineState.state) {
            case "SUCCESS":
                logger.println("Routine succeeded. Look at the result: " + pipelineState.resultUrl);
                state = State.SUCCESS;
                break;
            case "FAILURE":
            case "CANCELLED":
            case "SKIPPED":
                logger.println("Routine " + pipelineState.state.toLowerCase() + ". Look at the result: "
                        + pipelineState.resultUrl);
                state = State.FAILURE;
                break;
            default:
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (code == 1000) {
            state = State.SUCCESS;
        } else {
            logger.println("code: " + code + " reason: " + reason);
            state = State.FAILURE;
        }
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace(logger);
        state = State.FAILURE;
    }
}
