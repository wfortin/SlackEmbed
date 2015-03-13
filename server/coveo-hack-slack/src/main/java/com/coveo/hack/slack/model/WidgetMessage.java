package com.coveo.hack.slack.model;

public class WidgetMessage<T>
{
    public WidgetMessage(SenderInfo info,
                         T message)
    {
        this.message = message;
        this.senderInfo = info;
    }

    private T message;
    private SenderInfo senderInfo;

    public SenderInfo getSenderInfo()
    {
        return senderInfo;
    }

    public T getMessage()
    {
        return message;
    }

    public void setMessage(T message)
    {
        this.message = message;
    }

    @Override
    public String toString()
    {
        return "WidgetMessage{" + "message=" + message + ", senderInfo=" + senderInfo + '}';
    }
}
