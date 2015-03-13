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
        parsedSlackMessage.setChatText(slackMessage.getMessageContent());

        String message = slackMessage.getMessageContent();

        // TODO - Better parsing. This is way too simple.
        Matcher chatMessageMatcher = chatMessagePattern.matcher(message);
        if (chatMessageMatcher.matches()) {
            parsedSlackMessage.setOperation(ParsedSlackMessage.Operation.CHAT);
            parsedSlackMessage.setTargetUsername(chatMessageMatcher.group(1));
            parsedSlackMessage.setChatText(chatMessageMatcher.group(2));
            return parsedSlackMessage;
        }

        Matcher takeMatcher = takePattern.matcher(message);
        if (takeMatcher.matches()) {
            parsedSlackMessage.setOperation(ParsedSlackMessage.Operation.TAKE);
            parsedSlackMessage.setTargetUsername(takeMatcher.group(1));
            return parsedSlackMessage;
        }

        return parsedSlackMessage;
    }
}
