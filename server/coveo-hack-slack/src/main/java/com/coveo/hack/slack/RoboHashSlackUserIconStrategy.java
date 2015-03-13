package com.coveo.hack.slack;

public class RoboHashSlackUserIconStrategy implements SlackUserIconStrategy
{
    @Override public String iconUrlForUser(String username)
    {
        return "http://robohash.org/" + username + "?size=100x100";
    }
}
