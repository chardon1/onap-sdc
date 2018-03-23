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

package org.openecomp.sdc.logging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Collect information the is required for logging, but should not concern the business code of an application. For
 * example, host name and IP address.
 *
 * @author evitaliy
 * @since 04 Mar 2018
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace", "squid:S106", "squid:S1148"})
public class GlobalLoggingContext {

    // should be cashed to avoid low-level call, but with a timeout to account for IP or FQDN changes
    private static final HostAddressCache HOST_ADDRESS_CACHE = new HostAddressCache();

    @SuppressWarnings("squid:S1075")
    private static final String INSTANCE_UUID_PREFERENCES_PATH = "/logging/instance/uuid";

    private static final String INSTANCE_ID;

    static {
        INSTANCE_ID = readInstanceId();
    }

    private GlobalLoggingContext() {
        // prevent instantiation
    }

    /**
     * A unique ID of the logging entity. Is useful to distinguish between different nodes of the same application. It
     * is assumed, that the node can be re-started, in which case the unique ID must be retained.
     *
     * @return unique logging entity ID
     */
    public static String getInstanceId() {
        return INSTANCE_ID;
    }

    /**
     * Local host address as returned by Java runtime. A value of host address will be cached for the interval specified
     * in {@link HostAddressCache#REFRESH_TIME}
     *
     * @return local host address, may be null if could not be read for some reason
     */
    public static InetAddress getHostAddress() {
        return HOST_ADDRESS_CACHE.get();
    }

    private static String readInstanceId() {

        try {

            // On Linux, by default this will be ~/.java/.userPrefs/prefs.xml
            final Preferences preferences = Preferences.userRoot();
            String existingId = preferences.get(INSTANCE_UUID_PREFERENCES_PATH, null);
            if (existingId != null) {
                return existingId;
            }

            String newId = UUID.randomUUID().toString();
            preferences.put(INSTANCE_UUID_PREFERENCES_PATH, newId);
            preferences.flush();
            return newId;

        } catch (BackingStoreException e) {
            e.printStackTrace();
            // don't fail if there's a problem to use the store for some unexpected reason
            return UUID.randomUUID().toString();
        }
    }

    private static class HostAddressCache {

        private static final long REFRESH_TIME = 60000L;

        private final AtomicLong lastUpdated = new AtomicLong(0L);
        private InetAddress hostAddress;

        InetAddress get() {

            long current = System.currentTimeMillis();
            if (current - lastUpdated.get() > REFRESH_TIME) {

                synchronized (this) {

                    try {
                        // set now to register the attempt even if failed
                        lastUpdated.set(current);
                        hostAddress = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace(); // can't really use logging
                        hostAddress = null;
                    }
                }
            }

            return hostAddress;
        }
    }
}
