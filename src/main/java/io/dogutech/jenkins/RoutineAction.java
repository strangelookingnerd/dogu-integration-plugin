package io.dogutech.jenkins;

import hudson.model.Action;

public class RoutineAction implements Action {
    private String name;

    public RoutineAction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
}
