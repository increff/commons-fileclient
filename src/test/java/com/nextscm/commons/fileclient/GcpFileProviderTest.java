package com.nextscm.commons.fileclient;

import com.google.cloud.storage.HttpMethod;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GcpFileProviderTest {

    @Test
    public void testStaticValues() {
        assertEquals(30000, GcpFileProvider.EXPIRATION);
        assertEquals(HttpMethod.PUT, GcpFileProvider.HTTP_METHOD);
    }

}
