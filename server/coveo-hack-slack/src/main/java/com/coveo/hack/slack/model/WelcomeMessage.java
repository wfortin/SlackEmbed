package com.coveo.hack.slack.model;

public class WelcomeMessage
{
    private String userId;
    private String userAgent;
    private String currentPage;
    private String language;
    private String timezone;

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    public String getCurrentPage()
    {
        return currentPage;
    }

    public void setCurrentPage(String currentPage)
    {
        this.currentPage = currentPage;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }
}
