#!/bin/sh
bin/aiIngest --L --v --log --aiic IC --aiis IS --jopts '-Xmx2048m -XX:MaxPermSize=100m' --steps AR,JR,ICAR,ICPR,MIR,MCER,MFOR,MGR,MPAR,MUAR,MCAR,MAWR,MGRNT,MGRNT_INV,MGRNT_SPON,MEDU,MEDUP,MEDUO --t0
