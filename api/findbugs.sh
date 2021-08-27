#!/bin/sh

mvn -T 1C -Dfindbugs.maxHeap=4096 -Dfindbugs.timeout=6000000 compile findbugs:findbugs findbugs:gui

