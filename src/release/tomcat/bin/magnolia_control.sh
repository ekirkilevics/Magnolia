#!/bin/sh

# the $JDKPath variable can be replaced by external tools such as an installer
if [ -d "$JDKPath" ] ; then
    export JAVA_HOME="$JDKPath"
fi

#--- this was copied from the tomcat scripts ...
# resolve links - $0 may be a softlink
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
#---

if [ "$1" = "start" ] ; then
    export CATALINA_OPTS="$CATALINA_OPTS -Xms64M -Xmx512M -Djava.awt.headless=true"
    exec "$PRGDIR"/startup.sh
elif [ "$1" = "stop" ] ; then
    exec "$PRGDIR"/shutdown.sh
else
    echo "Please provide \"start\" or \"stop\" as argument."
    exit 1
fi
