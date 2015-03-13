package com.coveo.hack.slack;

import com.coveo.hack.slack.model.ParsedSlackMessage;
import com.ullink.slack.simpleslackapi.SlackMessage;

public interface SlackMessageParser
{
    ParsedSlackMessage parse(SlackMessage slackMessage);
}
