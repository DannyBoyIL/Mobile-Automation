package com.mobileAutomation.model;

import java.util.List;

public class UsersPayload {
    private List<UserData> users;

    public List<UserData> getUsers() {
        return users;
    }

    public void setUsers(List<UserData> users) {
        this.users = users;
    }
}
