#!/bin/bash

dir=`pwd`/..

add_to_file()
{
    copyright_file=$1
    for f in `find $dir -name '*.java' -not -path '*/target/*'`
    do
        head -10 $f | grep Copyright > /dev/null 2>&1
        if [ $? != 0 ]
        then
            echo $f
            cat $copyright_file $f | sponge $f
        fi
    done
}


add_to_file copyright.txt.java

