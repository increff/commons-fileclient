package com.nextscm.commons.fileclient;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertTrue;

public class UtilsTest {
    @Test
    public void testGetExpirationDate() {
        final int duration = 30 * 1000;
        Date now = new Date();
        Date expiration = Utils.getExpirationDate(duration);
        assertTrue(
                expiration.getTime() >= now.getTime() + 30 * 1000 && expiration.getTime() <= now.getTime() + 60 * 1000);
    }
}
