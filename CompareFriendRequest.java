package edu.usc.csci310.project.requests;

import edu.usc.csci310.project.models.User;

import java.util.List;
import java.util.stream.Collectors;

public class CompareFriendRequest {
    private Integer loggedInUserId;
    private List<User> users;

    public Integer getLoggedInUserId() {
        return loggedInUserId;
    }
    public void setLoggedInUserId(Integer loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }
    public List<User> getUsers() {
        return users;
    }
    public void setUsers(List<User> users) {
        this.users = users;
    }
//    public List<Integer> getUserIds() {
//        return users.stream()
//                .map(user -> user.getId().intValue())
//                .collect(Collectors.toList());
//    }


}
