#FROM buildpack-deps:sid-curl
FROM phusion/baseimage:latest
ENV SPLUNK_PRODUCT splunk
ENV SPLUNK_VERSION 6.4.1
ENV SPLUNK_BUILD debde650d26e
ENV SPLUNK_FILENAME splunk-${SPLUNK_VERSION}-${SPLUNK_BUILD}-Linux-x86_64.tgz

ENV SPLUNK_HOME /opt/splunk
ENV SPLUNK_GROUP splunk
ENV SPLUNK_USER splunk
ENV SPLUNK_BACKUP_DEFAULT_ETC /var/opt/splunk

# add splunk:splunk user
RUN groupadd -r ${SPLUNK_GROUP} \
    && useradd -r -m -g ${SPLUNK_GROUP} ${SPLUNK_USER}

# make the "en_US.UTF-8" locale so splunk will be utf-8 enabled by default
RUN apt-get update && apt-get install -y locales \
    && localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8
ENV LANG en_US.utf8

# pdfgen dependency
RUN apt-get install -y libgssapi-krb5-2

# Download official Splunk release, verify checksum and unzip in /opt/splunk
# Also backup etc folder, so it will be later copied to the linked volume
RUN apt-get install -y wget sudo \
    && mkdir -p ${SPLUNK_HOME} \
    && wget -qO /tmp/${SPLUNK_FILENAME} https://download.splunk.com/products/${SPLUNK_PRODUCT}/releases/${SPLUNK_VERSION}/linux/${SPLUNK_FILENAME} \
    && wget -qO /tmp/${SPLUNK_FILENAME}.md5 https://download.splunk.com/products/${SPLUNK_PRODUCT}/releases/${SPLUNK_VERSION}/linux/${SPLUNK_FILENAME}.md5 \
    && (cd /tmp && md5sum -c ${SPLUNK_FILENAME}.md5) \
    && tar xzf /tmp/${SPLUNK_FILENAME} --strip 1 -C ${SPLUNK_HOME} \
    && rm /tmp/${SPLUNK_FILENAME} \
    && rm /tmp/${SPLUNK_FILENAME}.md5 \
    && apt-get purge -y --auto-remove wget \
    && mkdir -p /var/opt/splunk \
    && cp -R ${SPLUNK_HOME}/etc ${SPLUNK_BACKUP_DEFAULT_ETC} \
    && rm -fR ${SPLUNK_HOME}/etc \
    && chown -R ${SPLUNK_USER}:${SPLUNK_GROUP} ${SPLUNK_HOME} \
    && chown -R ${SPLUNK_USER}:${SPLUNK_GROUP} ${SPLUNK_BACKUP_DEFAULT_ETC} \
    && rm -rf /var/lib/apt/lists/*


# Ports Splunk Web, Splunk Daemon, KVStore, Splunk Indexing Port, Network Input, HTTP Event Collector
EXPOSE 8000/tcp 8089/tcp 8191/tcp 9997/tcp 1514 8088/tcp

WORKDIR /opt/splunk

# Configurations folder, var folder for everyting (indexes, logs, kvstore)
VOLUME [ "/opt/splunk/etc", "/opt/splunk/var" ]

#wrapper program
#ENTRYPOINT ["java", "-Xmx1024m", "-jar", "/kubclient-1.0-SNAPSHOT.jar"]
#ENTRYPOINT ["java", "-Xmx1024m", "-cp", "/kubclient-1.0-SNAPSHOT.jar", "com.splunk.kubclient.Test"]
#ENTRYPOINT ["/bin/bash", "-c", 'while true; sleep 1; done']
#ENTRYPOINT ["java", "-Xmx1024m", "-cp", "/kubclient-1.0-SNAPSHOT.jar", "com.splunk.kubclient.Test"]
#ENTRYPOINT ["/bin/bash", "-c", 'set -e && java -jar /kubclient-1.0-SNAPSHOT.jar'] #yes, those MUST be single quotes around the argument to -c


#DO NOT INSTALL JAVA BEFORE SPLUNK. For some reason "apt-get purge -y --auto-remove wget" in the 
#splunk installation is messing with the previously installed java installation So install java
#after splunk
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

RUN rm -rf /etc/service/sshd /etc/my_init.d/00_regen_ssh_host_keys.sh

#Splunk needs ps, so I added procps to what java installs
#Also need pstack for SPLUNK_RUN_PSTACK_ON_SHUTDOWN_HANG
RUN apt-get update && apt-get install -y --no-install-recommends \
		bzip2 \
		unzip \
		xz-utils \
                procps \
                pstack \ 
	&& rm -rf /var/lib/apt/lists/*


# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]
#ADD run.sh /etc/my_init.d/run.sh

ADD https://dl.dropboxusercontent.com/u/12104474/kubclient-1.0-SNAPSHOT.jar //

COPY entrypoint.sh /sbin/entrypoint.sh
RUN chmod +x /sbin/entrypoint.sh
