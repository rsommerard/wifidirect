FROM java:8

RUN apt-get update && apt-get -y install \
  redir \
  && rm -rf /var/lib/apt/lists/*

RUN wget http://downloads.lightbend.com/scala/2.11.8/scala-2.11.8.tgz \
  && tar xzf scala-2.11.8.tgz \
  && rm -f scala-2.11.8.tgz

ENV SCALA_HOME 	/scala-2.11.8
ENV PATH ${PATH}:${SCALA_HOME}/bin

COPY wifidirect-ui-1.0.zip .

COPY entrypoint.sh .

RUN unzip wifidirect-ui-1.0.zip && rm wifidirect-ui-1.0.zip

ENTRYPOINT ["/entrypoint.sh"]
