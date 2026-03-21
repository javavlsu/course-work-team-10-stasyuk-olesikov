package ru.vlsu.myng.dto;

public class DevApplicationDto {
    private String githubUsername;
    private String text;

    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
