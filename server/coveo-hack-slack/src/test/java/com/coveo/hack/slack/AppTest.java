package com.coveo.hack.slack;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.junit.Test;

public class AppTest
{
    @Test
    public void t()
    {
        UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
        ReadableUserAgent r = parser.parse("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.89 Safari/537.36");
        System.out.println(r);
    }
}
