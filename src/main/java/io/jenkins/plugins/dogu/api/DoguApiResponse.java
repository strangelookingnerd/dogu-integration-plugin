package io.jenkins.plugins.dogu.api;

public class DoguApiResponse {
    public class RunRoutineResponse {
        public int routinePipelineId;
        public String projectId;
        public String routineId;
        public int index;
        public String creatorType;
        public String creatorId;
        public String createdAt;
    }

    public class UploadApplicationResponse {}
}
