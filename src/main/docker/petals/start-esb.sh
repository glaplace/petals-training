#!/bin/sh


# Update the host name in the topology.xml file
# (otherwise, JMX connections will not work from the outside).
HOST=`hostname -i`
sed -i "s/<tns:host>.*<\/tns:host>/<tns:host>${HOST}<\/tns:host>/ig" /home/petals/petals-esb/conf/topology.xml


# Mettre la jvm en debug
export JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Xmx1024m -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y"

petals-esb -e

