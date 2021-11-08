#
# Petals Dockerfile
#

FROM openjdk:8-jre-alpine


ARG petals_link="https://repository.ow2.org/nexus/content/repositories/public/org/ow2/petals/petals-esb-minimal-zip/5.2.0/petals-esb-minimal-zip-5.2.0.zip"
ARG petals_folder_name="petals-esb-default-zip-5.2.0"
ARG PETALS_HOME=/home/petals

RUN set -eux; \
    addgroup -S -g 1000 petals; \
    adduser -S -D -u 1000 -G petals petals;

COPY --chown=petals:petals start-esb.sh /home/petals/


RUN set -eux &&\
    apk add --no-cache --virtual .bootstrap-deps wget ca-certificates && \
    wget --progress=bar:force:noscroll -O /tmp/petals-esb.zip ${petals_link} && \
    wget --progress=bar:force:noscroll -O /tmp/petals-cli.zip 'https://repository.ow2.org/nexus/content/repositories/public/org/ow2/petals/petals-cli-distrib-zip/3.1.1-1.0/petals-cli-distrib-zip-3.1.1-1.0.zip' && \
    mkdir --parent /tmp/petals-cli ${PETALS_HOME}/petals-cli ${PETALS_HOME}/petals-esb /home/petals/.m2 && \
    mkdir --parent ${PETALS_HOME}/configuration && \
    unzip -qq -d ${PETALS_HOME}/petals-esb /tmp/petals-esb.zip && \
    unzip -qq -d /tmp/petals-cli /tmp/petals-cli.zip && \
    cp -r /tmp/petals-cli/petals-cli-distrib-zip-*/* ${PETALS_HOME}/petals-cli && \
    rm -rf /tmp/petal* && \
    ln -s ${PETALS_HOME}/petals-esb/bin/petals-esb.sh /usr/local/bin/petals-esb && \
    ln -s ${PETALS_HOME}/petals-cli/bin/petals-cli.sh /usr/local/bin/petals-cli && \
    apk del --purge .bootstrap-deps && \
    rm -rf /var/cache/apk/* && \
    chown -R petals:petals /home/petals && \
    chmod u+x /home/petals/start-esb.sh

USER petals

# Expose soap, rest, flowable et jmx ports:
EXPOSE 8084 8086 8089 8000 7700 7800

WORKDIR /home/petals

#
CMD /home/petals/start-esb.sh

