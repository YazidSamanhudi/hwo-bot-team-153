#!/bin/sh

SHLOC=`dirname $0`
WD=`(cd $SHLOC/ShallowGreen; pwd)`
PIDFILE="$WD/shallowgreen.pid"

[ -f $PIDFILE ] && kill -TERM `head -1 $PIDFILE` && rm -f $PIDFILE
