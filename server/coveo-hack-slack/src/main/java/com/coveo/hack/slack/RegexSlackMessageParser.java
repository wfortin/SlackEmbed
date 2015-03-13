package com.coveo.hack.slack;

import com.coveo.hack.slack.model.ParsedSlackMessage;
import com.ullink.slack.simpleslackapi.SlackMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSlackMessageParser implements SlackMessageParser
{
    private Pattern chatMessagePattern = Pattern.compile("^@(\\w+):?(.*)");
    private Pattern takePattern = Pattern.compile("^take @(\\w+)");

    @Override
    public ParsedSlackMessage parse(SlackMessage slackMessage)
    {
        ParsedSlackMessage parsedSlackMessage = new ParsedSlackMessage();
        parsedSlackMessage.setOperation(ParsedSlackMessage.Operation.UNKNOWN);
        parsedSlackMessage.setOriginalSlackMessage(slackMessage);
        parsedSlackMessage.setChannelName(slackMessage.getChannel().getName());
        parsedSlackMessage.setChatText(slackMessage.getMessageContent().trim());

        String message = slackMessage.getMessageContent();

        // TODO - Better parsing. This is way too simple.
        Matcher chatMessageMatcher = chatMessagePattern.matcher(message);
        if (chatMessageMatcher.matches()) {
            parsedSlackMessage.setOperation(ParsedSlackMessage.Operation.CHAT);
            parsedSlackMessage.setTargetUsername(chatMessageMatcher.group(1).trim());
            parsedSlackMessage.setChatText(chatMessageMatcher.group(2).trim());
            return parsedSlackMessage;
        }

        Matcher takeMatcher = takePattern.matcher(message);
        if (takeMatcher.matches()) {
            parsedSlackMessage.setOperation(ParsedSlackMessage.Operation.TAKE);
            parsedSlackMessage.setTargetUsername(takeMatcher.group(1).trim());
            return parsedSlackMessage;
        }

        return parsedSlackMessage;
    }
}
