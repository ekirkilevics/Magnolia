#!/bin/sh
# -----------------------------------------------------------------------------
# Start Script for the CATALINA Server
#
# $Id: startup.sh,v 1.3 2002/08/04 18:19:43 patrickl Exp $
# -----------------------------------------------------------------------------

# -----------------------------------------------------------------------------
# Magnolia needs extra memory
# -----------------------------------------------------------------------------

export CATALINA_OPTS="$CATALINA_OPTS -ms64M -mx512M"

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
 
PRGDIR=`dirname "$PRG"`
EXECUTABLE=catalina.sh

# Check that target executable exists
if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
  echo "Cannot find $PRGDIR/$EXECUTABLE"
  echo "This file is needed to run this program"
  exit 1
fi

exec "$PRGDIR"/"$EXECUTABLE" start "$@"
