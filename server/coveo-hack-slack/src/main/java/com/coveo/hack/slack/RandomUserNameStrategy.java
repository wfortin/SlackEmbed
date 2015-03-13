package com.coveo.hack.slack;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class RandomUserNameStrategy implements UserNameStrategy
{
    private SecureRandom random = new SecureRandom();

    @Override
    public String generateUserName()
    {
        StringBuilder builder = new StringBuilder("user");
        for (int i = 0; i < 4; i++) {
            builder.append(random.nextInt(9));
        }
        return builder.toString();
    }
}
