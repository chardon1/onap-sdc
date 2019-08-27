#!/bin/sh

JAVA_OPTIONS=" ${JAVA_OPTIONS} \
               -Dconfig.home=${JETTY_BASE}/config -Dlog.home=${JETTY_BASE}/logs \
               -Dlogback.configurationFile=${JETTY_BASE}/config/catalog-be/logback.xml \
               -Dconfiguration.yaml=${JETTY_BASE}/config/catalog-be/configuration.yaml \
               -Donboarding_configuration.yaml=${JETTY_BASE}/config/onboarding-be/onboarding_configuration.yaml \
               -Djavax.net.ssl.trustStore=${JETTY_BASE}/etc/org.onap.sdc.trust.jks \
               -Djavax.net.ssl.trustStorePassword=Y,f975ZNJfVZhV*{+Y[}pA?0 \
               -Djetty.console-capture.dir=${JETTY_BASE}/logs"

cd /root/chef-solo
chef-solo -c solo.rb -E ${ENVNAME}

cd /var/lib/jetty
/docker-entrypoint.sh &

while true; do sleep 2; done



