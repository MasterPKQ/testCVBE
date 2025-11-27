package com.example.identity.dto.response;

import java.util.List;

public class StatsResponse {

    private long totalUsers;
    private long totalProfiles;
    private long pageViews;
    private List<UserGrowth> userGrowth;

    public StatsResponse() {}

    public StatsResponse(long totalUsers, long totalProfiles, long pageViews, List<UserGrowth> userGrowth) {
        this.totalUsers = totalUsers;
        this.totalProfiles = totalProfiles;
        this.pageViews = pageViews;
        this.userGrowth = userGrowth;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalProfiles() { return totalProfiles; }
    public void setTotalProfiles(long totalProfiles) { this.totalProfiles = totalProfiles; }

    public long getPageViews() { return pageViews; }
    public void setPageViews(long pageViews) { this.pageViews = pageViews; }

    public List<UserGrowth> getUserGrowth() { return userGrowth; }
    public void setUserGrowth(List<UserGrowth> userGrowth) { this.userGrowth = userGrowth; }

    // Inner class
    public static class UserGrowth {
        private String month;
        private long users;

        public UserGrowth() {}
        public UserGrowth(String month, long users) {
            this.month = month;
            this.users = users;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public long getUsers() { return users; }
        public void setUsers(long users) { this.users = users; }
    }
}
