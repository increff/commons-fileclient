/*
 * Copyright (c) 2021. Increff
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.increff.commons.fileclient;


import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
