
FROM eclipse-temurin:8-focal

RUN set -x && \
    ln -snf /usr/bin/bash /usr/bin/sh && \
    apt-get update -q && \
    apt-get install -yq retry busybox && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir /opt/busybox && \
    busybox --install /opt/busybox

ENV PATH=/opt/java/openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/busybox

ENV TASK_CANAL_ENABLE="False"

COPY compass-v1.1.2.tar.gz /opt

# compass
RUN tar -xzf /opt/compass-*.tar.gz -C /opt && \
    rm /opt/compass-*.tar.gz && \
    ln -s /opt/compass-* /opt/compass && \
    rm /opt/compass/bin/compass_env.sh

COPY conf/compass_env.sh /opt/compass/bin


# ENTRYPOINT ["/opt/compass/bin/start_all.sh"]
CMD /opt/compass/bin/start_all.sh && /bin/bash
