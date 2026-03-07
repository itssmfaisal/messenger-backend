package com.backend.messenger.model;

public class ProfileDTO {
    private String username;
    private String displayName;
    private String bio;
    private String profilePictureUrl;

    public ProfileDTO() {}

    public ProfileDTO(String username, String displayName, String bio, String profilePictureUrl) {
        this.username = username;
        this.displayName = displayName;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
