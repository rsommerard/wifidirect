FROM ubuntu:15.10

RUN apt-get update && apt-get -y install \
  qemu-kvm \
  software-properties-common \
  telnet \
  wget \
  unzip \
  redir \
  && add-apt-repository -y ppa:webupd8team/java \
  && apt-get update \
  && echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections \
  && apt-get install -y oracle-java8-installer \
  && rm -rf /var/lib/apt/lists/*

RUN wget -q http://dl.google.com/android/android-sdk_r24.4.1-linux.tgz \
  && tar xzf android-sdk_r24.4.1-linux.tgz \
  && rm -f android-sdk_r24.4.1-linux.tgz

ENV ANDROID_HOME /android-sdk-linux
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools

RUN echo y | android update sdk --filter platform-tools --no-ui -a
RUN echo y | android update sdk --filter android-22 --no-ui -a
RUN echo y | android update sdk --filter sys-img-x86-android-22 --no-ui -a

RUN echo y | android update adb

RUN echo no | android create avd -n AndroidAPI22x86 -t android-22

RUN sed -i -e 's/hw.ramSize=512/hw.ramSize=2048/g' /root/.android/avd/AndroidAPI22x86.avd/config.ini

RUN wget http://downloads.lightbend.com/scala/2.11.8/scala-2.11.8.tgz \
  && tar xzf scala-2.11.8.tgz \
  && rm -f scala-2.11.8.tgz

ENV SCALA_HOME 	/scala-2.11.8
ENV PATH ${PATH}:${SCALA_HOME}/bin

COPY entrypoint.sh .
COPY app-debug.apk .
COPY wifidirect-node-1.0.zip .

RUN unzip wifidirect-node-1.0.zip \
  && rm wifidirect-node-1.0.zip

ENTRYPOINT ["/entrypoint.sh"]
