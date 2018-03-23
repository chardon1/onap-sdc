/*
 * Copyright © 2016-2017 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.logging.slf4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.UUID;
import org.openecomp.sdc.logging.api.ContextData;
import org.openecomp.sdc.logging.api.LoggingContext;
import org.slf4j.MDC;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Unit-testing logging context service via its facade.
 *
 * @author evitaliy
 * @since 12 Sep 2016
 */
public class LoggingContextTest {

    @AfterMethod
    public void clearMdc() {
        MDC.clear();
    }

    @Test
    public void returnMdcWrapperWhenToRunnableCalled() {
        assertEquals(LoggingContext.copyToRunnable(() -> {}).getClass(), MDCRunnableWrapper.class);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenToRunnableWithNull() {
        LoggingContext.copyToRunnable(null);
    }

    @Test
    public void returnMdcWrapperWhenToCallableCalled() {
        assertEquals(LoggingContext.copyToCallable(() -> "").getClass(), MDCCallableWrapper.class);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenToCallableWithNull() {
        LoggingContext.copyToCallable(null);
    }

    @Test
    public void keysClearedWhenContextCleared() {

        String value = UUID.randomUUID().toString();

        ContextData context = ContextData.builder().partnerName(value).requestId(value).serviceName(value).build();
        LoggingContext.put(context);
        LoggingContext.clear();

        for (ContextField field : ContextField.values()) {
            assertNull(MDC.get(field.asKey()));
        }
    }

    @Test
    public void unrelatedKeysRemainWhenContextCleared() {

        String randomValue = UUID.randomUUID().toString();
        String randomKey = "Key-" + randomValue;

        MDC.put(randomKey, randomValue);
        LoggingContext.clear();
        assertEquals(MDC.get(randomKey), randomValue);
    }

    @Test
    public void contextHasServiceNameWhenPut() {

        String random = UUID.randomUUID().toString();
        ContextData context = ContextData.builder().serviceName(random).build();
        LoggingContext.put(context);
        assertEquals(random, MDC.get(ContextField.SERVICE_NAME.asKey()));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void throwNpeWhenContextDataNull() {
        LoggingContext.put(null);
    }

    @Test
    public void contextHasRequestIdWhenPut() {

        String random = UUID.randomUUID().toString();
        ContextData context = ContextData.builder().requestId(random).build();
        LoggingContext.put(context);
        assertEquals(random, MDC.get(ContextField.REQUEST_ID.asKey()));
    }

    @Test
    public void contextHasPartnerNameWhenPut() {

        String random = UUID.randomUUID().toString();
        ContextData context = ContextData.builder().partnerName(random).build();
        LoggingContext.put(context);
        assertEquals(random, MDC.get(ContextField.PARTNER_NAME.asKey()));
    }

    @Test
    public void contextHasServerHostWhenPopulated() {

        ContextData context = ContextData.builder().build();
        LoggingContext.put(context);
        assertNotNull(MDC.get(ContextField.SERVER.asKey()));
    }

    @Test
    public void contextHasServerAddressWhenPopulated() {

        ContextData context = ContextData.builder().build();
        LoggingContext.put(context);
        assertNotNull(MDC.get(ContextField.SERVER_IP_ADDRESS.asKey()));
    }

    @Test
    public void contextHasInstanceIdWhenPopulated() {

        ContextData context = ContextData.builder().build();
        LoggingContext.put(context);
        assertNotNull(MDC.get(ContextField.INSTANCE_ID.asKey()));
    }
}
