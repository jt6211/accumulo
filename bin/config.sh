#! /usr/bin/env bash
#copied below from hadoop-config.sh
this="$0"
while [ -h "$this" ]; do
    ls=`ls -ld "$this"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
        this="$link"
    else
        this=`dirname "$this"`/"$link"
    fi
done
bin=`dirname "$this"`
script=`basename "$this"`
bin=`cd "$bin"; pwd`
this="$bin/$script"

ACCUMULO_HOME=`dirname "$this"`/..
export ACCUMULO_HOME=`cd $ACCUMULO_HOME; pwd`

if [ -f $ACCUMULO_HOME/conf/accumulo-env.sh ] ; then
. $ACCUMULO_HOME/conf/accumulo-env.sh
fi

if [ -z ${ACCUMULO_LOG_DIR} ]; then
        ACCUMULO_LOG_DIR=$ACCUMULO_HOME/logs
fi

mkdir -p $ACCUMULO_LOG_DIR 2>/dev/null

export ACCUMULO_LOG_DIR

if [ -z ${ACCUMULO_VERSION} ]; then
        ACCUMULO_VERSION=1.3.5-incubating-SNAPSHOT
fi

if [ -z "$HADOOP_HOME" ]
then
   HADOOP_HOME="`which hadoop`"
   if [ -z "$HADOOP_HOME" ]
   then
      echo "You must set HADOOP_HOME"
      exit 1
   fi
   HADOOP_HOME=`dirname $HADOOP_HOME`
   HADOOP_HOME=`dirname $HADOOP_HOME`
fi
export HADOOP_HOME

if [ "`$HADOOP_HOME/bin/hadoop version 2>/dev/null | head -1 | cut -f 2 -d .`" != 20 ]; then
    echo "Accumulo $ACCUMULO_VERSION requires Hadoop version 0.20.x"
    exit 1
fi

if [ ! -f "$ACCUMULO_HOME/conf/masters" -o ! -f "$ACCUMULO_HOME/conf/slaves" ]
then
    if [ ! -f "$ACCUMULO_HOME/conf/masters" -a ! -f "$ACCUMULO_HOME/conf/slaves" ]
    then
        echo "STANDALONE: Missing both conf/masters and conf/slaves files"
        echo "STANDALONE: Assuming single-node (localhost only) instance"
        echo "STANDALONE: echo "`hostname`" > $ACCUMULO_HOME/conf/masters"
        echo `hostname` > "$ACCUMULO_HOME/conf/masters"
        echo "STANDALONE: echo "`hostname`" > $ACCUMULO_HOME/conf/slaves"
        echo `hostname` > "$ACCUMULO_HOME/conf/slaves"
        fgrep -s logger.dir.walog "$ACCUMULO_HOME/conf/accumulo-site.xml" > /dev/null
        WALOG_CONFIGURED=$?
        if [ $WALOG_CONFIGURED -ne 0 -a ! -e "$ACCUMULO_HOME/walogs" ]
        then
          echo "STANDALONE: Creating default local write-ahead log directory"
          mkdir "$ACCUMULO_HOME/walogs"
          echo "STANDALONE: mkdir \"$ACCUMULO_HOME/walogs\""
        fi
        if [ ! -e "$ACCUMULO_HOME/conf/accumulo-metrics.xml" ]
        then
          echo "STANDALONE: Creating default metrics configuration"
          cp "$ACCUMULO_HOME/conf/accumulo-metrics.xml.example" "$ACCUMULO_HOME/conf/accumulo-metrics.xml"
        fi
    else
        echo "You are missing either $ACCUMULO_HOME/conf/masters or $ACCUMULO_HOME/conf/slaves"
        echo "Please configure them both for a multi-node instance, or delete them both for a single-node (localhost only) instance"
        exit 1
    fi
fi
MASTER1=`grep -v '^#' "$ACCUMULO_HOME/conf/masters" | head -1`
GC=$MASTER1
MONITOR=$MASTER1
if [ -f "$ACCUMULO_HOME/conf/gc" ]; then
    GC=`grep -v '^#' "$ACCUMULO_HOME/conf/gc" | head -1`
fi
if [ -f "$ACCUMULO_HOME/conf/monitor" ]; then
    MONITOR=`grep -v '^#' "$ACCUMULO_HOME/conf/monitor" | head -1`
fi
if [ ! -f "$ACCUMULO_HOME/conf/tracers" ]; then
    echo "$MASTER1" > "$ACCUMULO_HOME/conf/tracers"
fi
SSH='ssh -qnf'