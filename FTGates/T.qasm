#############################################################
####################Qubit declarations#######################
############################FT-T#############################
#############################################################
# qubits for Prepare 0 circuit (QEC):
qubit q000,1
qubit q001,1
qubit q002,0
qubit q003,1
qubit q004,0
qubit q005,0
qubit q006,0

qubit q200,1
qubit q201,1
qubit q202,0
qubit q203,1
qubit q204,0
qubit q205,0
qubit q206,0
############################################################
# qubits for Prepare + circuit (QEC):
qubit q100,0 
qubit q101,0 
qubit q102,1 
qubit q103,0 
qubit q104,1 
qubit q105,1 
qubit q106,1 

qubit q300,0 
qubit q301,0 
qubit q302,1 
qubit q303,0 
qubit q304,1 
qubit q305,1 
qubit q306,1 
############################################################
# qubits for FT implementation of T gate:
qubit q400,1,0,51,-,-
qubit q401,1,0,51,-,-
qubit q402,1,0,50,-,-
qubit q403,1,0,50,-,-
qubit q404,1,0,50,-,-
qubit q405,1,0,50,-,-
qubit q406,1,0,50,-,-

qubit q500,1,-,-,0,51
qubit q501,1,-,-,0,51
qubit q502,0,-,-,0,50
qubit q503,1,-,-,0,50
qubit q504,0,-,-,0,50
qubit q505,0,-,-,0,50
qubit q506,0,-,-,0,50

# ancila for first TXT:
qubit q600,0
qubit q601,0
qubit q602,0
qubit q603,1
qubit q604,0
qubit q605,0
qubit q606,0
qubit q607,0

# ancila for second TXT:
qubit q610,0
qubit q611,0
qubit q612,0
qubit q613,1
qubit q614,0
qubit q615,0
qubit q616,0
qubit q617,0

# qubits for Prepare 0 circuit (QED):
qubit q700,1
qubit q701,1
qubit q702,0
qubit q703,1
qubit q704,0
qubit q705,0
qubit q706,0

qubit q710,1
qubit q711,1
qubit q712,0
qubit q713,1
qubit q714,0
qubit q715,0
qubit q716,0

qubit q800,1
qubit q801,1
qubit q802,0
qubit q803,1
qubit q804,0
qubit q805,0
qubit q806,0

qubit q810,1
qubit q811,1
qubit q812,0
qubit q813,1
qubit q814,0
qubit q815,0
qubit q816,0

# qubits for Prepare + circuit (QED):
qubit q720,0 
qubit q721,0 
qubit q722,1 
qubit q723,0 
qubit q724,1 
qubit q725,1 
qubit q726,1 

qubit q730,0 
qubit q731,0 
qubit q732,1 
qubit q733,0 
qubit q734,1 
qubit q735,1 
qubit q736,1 

qubit q820,0 
qubit q821,0 
qubit q822,1 
qubit q823,0 
qubit q824,1 
qubit q825,1 
qubit q826,1 

qubit q830,0 
qubit q831,0 
qubit q832,1 
qubit q833,0 
qubit q834,1 
qubit q835,1 
qubit q836,1
#############################################################
#### Logical zero encoded (QEC) #############################
#############################################################
#Gate 0
Y q002
Z q002
Z q000
geophase q002,q000
Y q002
#Gate 1
Y q005
Z q005
Z q003
geophase q005, q003
Y q005
#Gate 2
Y q006
Z q006
Z q001
geophase q006, q001
Y q006
#Gate 3
Y q004
Z q004
Z q000
geophase q004, q000
Y q004
#Gate 4
Y q006
Z q006
Z q003
geophase q006, q003
Y q006
#Gate 5
Y q005
Z q005
Z q001
geophase q005, q001
Y q005
#Gate 6
Y q006
Z q006
Z q000
geophase q006, q000
Y q006
#Gate 7
Y q002
Z q002
Z q001
geophase q002, q001
Y q002
#Gate 8
Y q004
Z q004
Z q003
geophase q004, q003
Y q004
#############################################################


#############################################################
#### Logical zero encoded (QEC) #############################
#############################################################
#Gate 0
Y q202
Z q202
Z q200
geophase q202,q200
Y q202
#Gate 1
Y q205
Z q205
Z q203
geophase q205, q203
Y q205
#Gate 2
Y q206
Z q206
Z q201
geophase q206, q201
Y q206
#Gate 3
Y q204
Z q204
Z q200
geophase q204, q200
Y q204
#Gate 4
Y q206
Z q206
Z q203
geophase q206, q203
Y q206
#Gate 5
Y q205
Z q205
Z q201
geophase q205, q201
Y q205
#Gate 6
Y q206
Z q206
Z q200
geophase q206, q200
Y q206
#Gate 7
Y q202
Z q202
Z q201
geophase q202, q201
Y q202
#Gate 8
Y q204
Z q204
Z q203
geophase q204, q203
Y q204
#############################################################


#############################################################
######INTERACTION (QEC) #####################################
#############################################################
#Gate 0
Y q200
Z q200
Z q000
geophase q200, q000
Y q200
#Gate 0
Y q201
Z q201
Z q001
geophase q201, q001
Y q201
#Gate 0
Y q202
Z q202
Z q002
geophase q202, q002
Y q202
#Gate 0
Y q203
Z q203
Z q003
geophase q203, q003
Y q203
#Gate 0
Y q204
Z q204
Z q004
geophase q204, q004
Y q204
#Gate 0
Y q205
Z q205
Z q005
geophase q205, q005
Y q205
#Gate 0
Y q206
Z q206
Z q006
geophase q206, q006
Y q206
########################################################

########################################################
####MEASUREMENT TO CHECK THE RESULT (QEC) ##############
########################################################
#Gate 0
measx  q200
#Gate 0
measx  q201
#Gate 0
measx  q202
#Gate 0
measx  q203
#Gate 0
measx  q204
#Gate 0
measx  q205
#Gate 0
measx  q206
#############################################################


#############################################################
#### Logica lpos encoded (QEC) ##############################
#############################################################
#Gate 0
Y q100
Z q100
Z q102
geophase q100, q102
Y q100
#Gate 1
Y q103
Z q103
Z q105
geophase q103, q105
Y q103
#Gate 2
Y q101
Z q101
Z q106
geophase q101, q106
Y q101
#Gate 3
Y q100
Z q100
Z q104
geophase q100, q104
Y q100
#Gate 4
Y q103
Z q103
Z q106
geophase q103, q106
Y q103
#Gate 5
Y q101
Z q101
Z q105
geophase q101, q105
Y q101
#Gate 6
Y q100
Z q100
Z q106
geophase q100, q106
Y q100
#Gate 7
Y q101
Z q101
Z q102
geophase q101, q102
Y q101
#Gate 8
Y q103
Z q103
Z q104
geophase q103, q104
Y q103
#############################################################


#############################################################
#### Logica lpos encoded (QEC) ##############################
#############################################################
#Gate 0
Y q300
Z q300
Z q302
geophase q300, q302
Y q300
#Gate 1
Y q303
Z q303
Z q305
geophase q303, q305
Y q303
#Gate 2
Y q301
Z q301
Z q306
geophase q301, q306
Y q301
#Gate 3
Y q300
Z q300
Z q304
geophase q300, q304
Y q300
#Gate 4
Y q303
Z q303
Z q306
geophase q303, q306
Y q303
#Gate 5
Y q301
Z q301
Z q305
geophase q301, q305
Y q301
#Gate 6
Y q300
Z q300
Z q306
geophase q300, q306
Y q300
#Gate 7
Y q301
Z q301
Z q302
geophase q301, q302
Y q301
#Gate 8
Y q303
Z q303
Z q304
geophase q303, q304
Y q303
#############################################################

#############################################################
######INTERACTION (QEC) #####################################
#############################################################
#Gate 0
Y q100
Z q100
Z q300
geophase q100, q300
Y q100
#Gate 0
Y q101
Z q101
Z q301
geophase q101, q301
Y q101
#Gate 0
Y q102
Z q102
Z q302
geophase q102, q302
Y q102
#Gate 0
Y q103
Z q103
Z q303
geophase q103, q303
Y q103
#Gate 0
Y q104
Z q104
Z q304
geophase q104, q304
Y q104
#Gate 0
Y q105
Z q105
Z q305
geophase q105, q305
Y q105
#Gate 0
Y q106
Z q106
Z q306
geophase q106, q306
Y q106
########################################################

########################################################
####MEASUREMENT TO CHECK THE RESULT (QEC) ##############
########################################################
#Gate 0
measx  q300
#Gate 0
measx  q301
#Gate 0
measx  q302
#Gate 0
measx  q303
#Gate 0
measx  q304
#Gate 0
measx  q305
#Gate 0
measx  q306
#############################################################





#############################################################
########################   T Gate   #########################
#############################################################

########## First TXT ##########
# Logical zero encoded (main input of the TXT):
#Gate 0
Y q502
Z q502
Z q500
geophase q502,q500
Y q502
#Gate 1
Y q505
Z q505
Z q503
geophase q505, q503
Y q505
#Gate 2
Y q506
Z q506
Z q501
geophase q506, q501
Y q506
#Gate 3
Y q504
Z q504
Z q500
geophase q504, q500
Y q504
#Gate 4
Y q506
Z q506
Z q503
geophase q506, q503
Y q506
#Gate 5
Y q505
Z q505
Z q501
geophase q505, q501
Y q505
#Gate 6
Y q506
Z q506
Z q500
geophase q506, q500
Y q506
#Gate 7
Y q502
Z q502
Z q501
geophase q502, q501
Y q502
#Gate 8
Y q504
Z q504
Z q503
geophase q504, q503
Y q504

# ancila of the TXT:
#Gate 0
Y q604
Z q604
Z q603
geophase q604,q603
Y q604
#Gate 1
Y q602
Z q602
Z q603
geophase q602, q603
Y q602
#Gate 2
Y q605
Z q605
Z q604
geophase q605, q604
Y q605
#Gate 3
Y q601
Z q601
Z q602
geophase q601, q602
Y q601
#Gate 4
Y q606
Z q606
Z q605
geophase q606, q605
Y q606
#Gate 5
Y q600
Z q600
Z q601
geophase q600, q601
Y q600
#Gate 6
Y q607
Z q607
Z q606
geophase q607, q606
Y q607
#Gate 7
Y q607
Z q607
Z q600
geophase q607, q600
Y q607

# transversal T gate:
#Gate 0
rotz q500
#Gate 0
rotz q501
#Gate 0
rotz q502
#Gate 0
rotz q503
#Gate 0
rotz q504
#Gate 0
rotz q505
#Gate 0
rotz q506

# transversal CNOT gate:
#Gate 0
Y q500
Z q500
Z q600
geophase q500, q600
Y q500
#Gate 0
Y q501
Z q501
Z q601
geophase q501, q601
Y q501
#Gate 0
Y q502
Z q502
Z q602
geophase q502, q602
Y q502
#Gate 0
Y q503
Z q503
Z q603
geophase q503, q603
Y q503
#Gate 0
Y q504
Z q504
Z q604
geophase q504, q604
Y q504
#Gate 0
Y q505
Z q505
Z q605
geophase q505, q605
Y q505
#Gate 0
Y q506
Z q506
Z q606
geophase q506, q606
Y q506

# transversal Tdagger gate:
#Gate 0
rotz q500
#Gate 0
rotz q501
#Gate 0
rotz q502
#Gate 0
rotz q503
#Gate 0
rotz q504
#Gate 0
rotz q505
#Gate 0
rotz q506

# measure the ancila in X basis:
#Gate 0
measx  q600
#Gate 0
measx  q601
#Gate 0
measx  q602
#Gate 0
measx  q603
#Gate 0
measx  q604
#Gate 0
measx  q605
#Gate 0
measx  q606
###############################

########## First QED ##########
#Logical zero encoded (QED):
#Gate 0
Y q702
Z q702
Z q700
geophase q702,q700
Y q702
#Gate 1
Y q705
Z q705
Z q703
geophase q705, q703
Y q705
#Gate 2
Y q706
Z q706
Z q701
geophase q706, q701
Y q706
#Gate 3
Y q704
Z q704
Z q700
geophase q704, q700
Y q704
#Gate 4
Y q706
Z q706
Z q703
geophase q706, q703
Y q706
#Gate 5
Y q705
Z q705
Z q701
geophase q705, q701
Y q705
#Gate 6
Y q706
Z q706
Z q700
geophase q706, q700
Y q706
#Gate 7
Y q702
Z q702
Z q701
geophase q702, q701
Y q702
#Gate 8
Y q704
Z q704
Z q703
geophase q704, q703
Y q704

#Logical zero encoded (QED):
#Gate 0
Y q722
Z q722
Z q720
geophase q722,q720
Y q722
#Gate 1
Y q725
Z q725
Z q723
geophase q725, q723
Y q725
#Gate 2
Y q726
Z q726
Z q721
geophase q726, q721
Y q726
#Gate 3
Y q724
Z q724
Z q720
geophase q724, q720
Y q724
#Gate 4
Y q726
Z q726
Z q723
geophase q726, q723
Y q726
#Gate 5
Y q725
Z q725
Z q721
geophase q725, q721
Y q725
#Gate 6
Y q726
Z q726
Z q720
geophase q726, q720
Y q726
#Gate 7
Y q722
Z q722
Z q721
geophase q722, q721
Y q722
#Gate 8
Y q724
Z q724
Z q723
geophase q724, q723
Y q724

#INTERACTION (QED):
#Gate 0
Y q720
Z q720
Z q700
geophase q720, q700
Y q720
#Gate 0
Y q721
Z q721
Z q701
geophase q721, q701
Y q721
#Gate 0
Y q722
Z q722
Z q702
geophase q722, q702
Y q722
#Gate 0
Y q723
Z q723
Z q703
geophase q723, q703
Y q723
#Gate 0
Y q724
Z q724
Z q704
geophase q724, q704
Y q724
#Gate 0
Y q725
Z q725
Z q705
geophase q725, q705
Y q725
#Gate 0
Y q726
Z q726
Z q706
geophase q726, q706
Y q726

#MEASUREMENT TO CHECK THE RESULT (QED):
#Gate 0
measx  q720
#Gate 0
measx  q721
#Gate 0
measx  q722
#Gate 0
measx  q723
#Gate 0
measx  q724
#Gate 0
measx  q725
#Gate 0
measx  q726

#Logical + encoded (QED):
#Gate 0
Y q710
Z q710
Z q712
geophase q710, q712
Y q710
#Gate 1
Y q713
Z q713
Z q715
geophase q713, q715
Y q713
#Gate 2
Y q711
Z q711
Z q716
geophase q711, q716
Y q711
#Gate 3
Y q710
Z q710
Z q714
geophase q710, q714
Y q710
#Gate 4
Y q713
Z q713
Z q716
geophase q713, q716
Y q713
#Gate 5
Y q711
Z q711
Z q715
geophase q711, q715
Y q711
#Gate 6
Y q710
Z q710
Z q716
geophase q710, q716
Y q710
#Gate 7
Y q711
Z q711
Z q712
geophase q711, q712
Y q711
#Gate 8
Y q713
Z q713
Z q714
geophase q713, q714
Y q713

#Logical + encoded (QED):
#Gate 0
Y q730
Z q730
Z q732
geophase q730, q732
Y q730
#Gate 1
Y q733
Z q733
Z q735
geophase q733, q735
Y q733
#Gate 2
Y q731
Z q731
Z q736
geophase q731, q736
Y q731
#Gate 3
Y q730
Z q730
Z q734
geophase q730, q734
Y q730
#Gate 4
Y q733
Z q733
Z q736
geophase q733, q736
Y q733
#Gate 5
Y q731
Z q731
Z q735
geophase q731, q735
Y q731
#Gate 6
Y q730
Z q730
Z q736
geophase q730, q736
Y q730
#Gate 7
Y q731
Z q731
Z q732
geophase q731, q732
Y q731
#Gate 8
Y q733
Z q733
Z q734
geophase q733, q734
Y q733

#INTERACTION (QED):
#Gate 0
Y q710
Z q710
Z q730
geophase q710, q730
Y q710
#Gate 0
Y q711
Z q711
Z q731
geophase q711, q731
Y q711
#Gate 0
Y q712
Z q712
Z q732
geophase q712, q732
Y q712
#Gate 0
Y q713
Z q713
Z q733
geophase q713, q733
Y q713
#Gate 0
Y q714
Z q714
Z q734
geophase q714, q734
Y q714
#Gate 0
Y q715
Z q715
Z q735
geophase q715, q735
Y q715
#Gate 0
Y q716
Z q716
Z q736
geophase q716, q736
Y q716

#MEASUREMENT TO CHECK THE RESULT (QED):
#Gate 0
measx  q730
#Gate 0
measx  q731
#Gate 0
measx  q732
#Gate 0
measx  q733
#Gate 0
measx  q734
#Gate 0
measx  q735
#Gate 0
measx  q736

#XRECOVER (Fig.8 without recovery step):
#Gate 0
Y q700
Z q700
Z q500
geophase q700, q500
Y q700
#Gate 0
Y q701
Z q701
Z q501
geophase q701, q501
Y q701
#Gate 0
Y q702
Z q702
Z q502
geophase q702, q502
Y q702
#Gate 0
Y q703
Z q703
Z q503
geophase q703, q503
Y q703
#Gate 0
Y q704
Z q704
Z q504
geophase q704, q504
Y q704
#Gate 0
Y q705
Z q705
Z q505
geophase q705, q505
Y q705
#Gate 0
Y q706
Z q706
Z q506
geophase q706, q506
Y q706

#MEASUREMENT TO CHECK THE RESULT:
#Gate 0
measx  q700
#Gate 0
measx  q701
#Gate 0
measx  q702
#Gate 0
measx  q703
#Gate 0
measx  q704
#Gate 0
measx  q705
#Gate 0
measx  q706

#ZRECOVER (Fig.8 without recovery step):
#Gate 0
Y q500
Z q500
Z q710
geophase q500, q710
Y q500
#Gate 0
Y q501
Z q501
Z q711
geophase q501, q711
Y q501
#Gate 0
Y q502
Z q502
Z q712
geophase q502, q712
Y q502
#Gate 0
Y q503
Z q503
Z q713
geophase q503, q713
Y q503
#Gate 0
Y q504
Z q504
Z q714
geophase q504, q714
Y q504
#Gate 0
Y q505
Z q505
Z q715
geophase q505, q715
Y q505
#Gate 0
Y q506
Z q506
Z q716
geophase q506, q716
Y q506

#MEASUREMENT TO CHECK THE RESULT:
#Gate 0
measz  q710
#Gate 0
measz  q711
#Gate 0
measz  q712
#Gate 0
measz  q713
#Gate 0
measz  q714
#Gate 0
measz  q715
#Gate 0
measz  q716
###############################

########## Second TXT #########

# ancila of the TXT:
#Gate 0
Y q614
Z q614
Z q613
geophase q614,q613
Y q614
#Gate 1
Y q612
Z q612
Z q613
geophase q612, q613
Y q612
#Gate 2
Y q615
Z q615
Z q614
geophase q615, q614
Y q615
#Gate 3
Y q611
Z q611
Z q612
geophase q611, q612
Y q611
#Gate 4
Y q616
Z q616
Z q615
geophase q616, q615
Y q616
#Gate 5
Y q610
Z q610
Z q611
geophase q610, q611
Y q610
#Gate 6
Y q617
Z q617
Z q616
geophase q617, q616
Y q617
#Gate 7
Y q617
Z q617
Z q610
geophase q617, q610
Y q617

# transversal T gate:
#Gate 0
rotz q500
#Gate 0
rotz q501
#Gate 0
rotz q502
#Gate 0
rotz q503
#Gate 0
rotz q504
#Gate 0
rotz q505
#Gate 0
rotz q506

# transversal CNOT gate:
#Gate 0
Y q500
Z q500
Z q610
geophase q500, q610
Y q500
#Gate 0
Y q501
Z q501
Z q611
geophase q501, q611
Y q501
#Gate 0
Y q502
Z q502
Z q612
geophase q502, q612
Y q502
#Gate 0
Y q503
Z q503
Z q613
geophase q503, q613
Y q503
#Gate 0
Y q504
Z q504
Z q614
geophase q504, q614
Y q504
#Gate 0
Y q505
Z q505
Z q615
geophase q505, q615
Y q505
#Gate 0
Y q506
Z q506
Z q616
geophase q506, q616
Y q506

# transversal Tdagger gate:
#Gate 0
rotz q500
#Gate 0
rotz q501
#Gate 0
rotz q502
#Gate 0
rotz q503
#Gate 0
rotz q504
#Gate 0
rotz q505
#Gate 0
rotz q506

# measure the ancila in X basis:
#Gate 0
measx  q610
#Gate 0
measx  q611
#Gate 0
measx  q612
#Gate 0
measx  q613
#Gate 0
measx  q614
#Gate 0
measx  q615
#Gate 0
measx  q616
###############################

########## Second QED #########
#Logical zero encoded (QED):
#Gate 0
Y q802
Z q802
Z q800
geophase q802,q800
Y q802
#Gate 1
Y q805
Z q805
Z q803
geophase q805, q803
Y q805
#Gate 2
Y q806
Z q806
Z q801
geophase q806, q801
Y q806
#Gate 3
Y q804
Z q804
Z q800
geophase q804, q800
Y q804
#Gate 4
Y q806
Z q806
Z q803
geophase q806, q803
Y q806
#Gate 5
Y q805
Z q805
Z q801
geophase q805, q801
Y q805
#Gate 6
Y q806
Z q806
Z q800
geophase q806, q800
Y q806
#Gate 7
Y q802
Z q802
Z q801
geophase q802, q801
Y q802
#Gate 8
Y q804
Z q804
Z q803
geophase q804, q803
Y q804

#Logical zero encoded (QED):
#Gate 0
Y q822
Z q822
Z q820
geophase q822,q820
Y q822
#Gate 1
Y q825
Z q825
Z q823
geophase q825, q823
Y q825
#Gate 2
Y q826
Z q826
Z q821
geophase q826, q821
Y q826
#Gate 3
Y q824
Z q824
Z q820
geophase q824, q820
Y q824
#Gate 4
Y q826
Z q826
Z q823
geophase q826, q823
Y q826
#Gate 5
Y q825
Z q825
Z q821
geophase q825, q821
Y q825
#Gate 6
Y q826
Z q826
Z q820
geophase q826, q820
Y q826
#Gate 7
Y q822
Z q822
Z q821
geophase q822, q821
Y q822
#Gate 8
Y q824
Z q824
Z q823
geophase q824, q823
Y q824

#INTERACTION (QED):
#Gate 0
Y q820
Z q820
Z q800
geophase q820, q800
Y q820
#Gate 0
Y q821
Z q821
Z q801
geophase q821, q801
Y q821
#Gate 0
Y q822
Z q822
Z q802
geophase q822, q802
Y q822
#Gate 0
Y q823
Z q823
Z q803
geophase q823, q803
Y q823
#Gate 0
Y q824
Z q824
Z q804
geophase q824, q804
Y q824
#Gate 0
Y q825
Z q825
Z q805
geophase q825, q805
Y q825
#Gate 0
Y q826
Z q826
Z q806
geophase q826, q806
Y q826

#MEASUREMENT TO CHECK THE RESULT (QED):
#Gate 0
measx  q820
#Gate 0
measx  q821
#Gate 0
measx  q822
#Gate 0
measx  q823
#Gate 0
measx  q824
#Gate 0
measx  q825
#Gate 0
measx  q826

#Logical + encoded (QED):
#Gate 0
Y q810
Z q810
Z q812
geophase q810, q812
Y q810
#Gate 1
Y q813
Z q813
Z q815
geophase q813, q815
Y q813
#Gate 2
Y q811
Z q811
Z q816
geophase q811, q816
Y q811
#Gate 3
Y q810
Z q810
Z q814
geophase q810, q814
Y q810
#Gate 4
Y q813
Z q813
Z q816
geophase q813, q816
Y q813
#Gate 5
Y q811
Z q811
Z q815
geophase q811, q815
Y q811
#Gate 6
Y q810
Z q810
Z q816
geophase q810, q816
Y q810
#Gate 7
Y q811
Z q811
Z q812
geophase q811, q812
Y q811
#Gate 8
Y q813
Z q813
Z q814
geophase q813, q814
Y q813

#Logical + encoded (QED):
#Gate 0
Y q830
Z q830
Z q832
geophase q830, q832
Y q830
#Gate 1
Y q833
Z q833
Z q835
geophase q833, q835
Y q833
#Gate 2
Y q831
Z q831
Z q836
geophase q831, q836
Y q831
#Gate 3
Y q830
Z q830
Z q834
geophase q830, q834
Y q830
#Gate 4
Y q833
Z q833
Z q836
geophase q833, q836
Y q833
#Gate 5
Y q831
Z q831
Z q835
geophase q831, q835
Y q831
#Gate 6
Y q830
Z q830
Z q836
geophase q830, q836
Y q830
#Gate 7
Y q831
Z q831
Z q832
geophase q831, q832
Y q831
#Gate 8
Y q833
Z q833
Z q834
geophase q833, q834
Y q833

#INTERACTION (QED):
#Gate 0
Y q810
Z q810
Z q830
geophase q810, q830
Y q810
#Gate 0
Y q811
Z q811
Z q831
geophase q811, q831
Y q811
#Gate 0
Y q812
Z q812
Z q832
geophase q812, q832
Y q812
#Gate 0
Y q813
Z q813
Z q833
geophase q813, q833
Y q813
#Gate 0
Y q814
Z q814
Z q834
geophase q814, q834
Y q814
#Gate 0
Y q815
Z q815
Z q835
geophase q815, q835
Y q815
#Gate 0
Y q816
Z q816
Z q836
geophase q816, q836
Y q816

#MEASUREMENT TO CHECK THE RESULT (QED):
#Gate 0
measx  q830
#Gate 0
measx  q831
#Gate 0
measx  q832
#Gate 0
measx  q833
#Gate 0
measx  q834
#Gate 0
measx  q835
#Gate 0
measx  q836

#XRECOVER (Fig.8 without recovery step):
#Gate 0
Y q800
Z q800
Z q500
geophase q800, q500
Y q800
#Gate 0
Y q801
Z q801
Z q501
geophase q801, q501
Y q801
#Gate 0
Y q802
Z q802
Z q502
geophase q802, q502
Y q802
#Gate 0
Y q803
Z q803
Z q503
geophase q803, q503
Y q803
#Gate 0
Y q804
Z q804
Z q504
geophase q804, q504
Y q804
#Gate 0
Y q805
Z q805
Z q505
geophase q805, q505
Y q805
#Gate 0
Y q806
Z q806
Z q506
geophase q806, q506
Y q806

#MEASUREMENT TO CHECK THE RESULT:
#Gate 0
measx  q800
#Gate 0
measx  q801
#Gate 0
measx  q802
#Gate 0
measx  q803
#Gate 0
measx  q804
#Gate 0
measx  q805
#Gate 0
measx  q806

#ZRECOVER (Fig.8 without recovery step):
#Gate 0
Y q500
Z q500
Z q810
geophase q500, q810
Y q500
#Gate 0
Y q501
Z q501
Z q811
geophase q501, q811
Y q501
#Gate 0
Y q502
Z q502
Z q812
geophase q502, q812
Y q502
#Gate 0
Y q503
Z q503
Z q813
geophase q503, q813
Y q503
#Gate 0
Y q504
Z q504
Z q814
geophase q504, q814
Y q504
#Gate 0
Y q505
Z q505
Z q815
geophase q505, q815
Y q505
#Gate 0
Y q506
Z q506
Z q816
geophase q506, q816
Y q506

#MEASUREMENT TO CHECK THE RESULT:
#Gate 0
measz  q810
#Gate 0
measz  q811
#Gate 0
measz  q812
#Gate 0
measz  q813
#Gate 0
measz  q814
#Gate 0
measz  q815
#Gate 0
measz  q816
###############################

########## CNOT ###############
#Gate 0
Y q400
Z q400
Z q500
geophase q400, q500
Y q400
#Gate 0
Y q401
Z q401
Z q501
geophase q401, q501
Y q401
#Gate 0
Y q402
Z q402
Z q502
geophase q402, q502
Y q402
#Gate 0
Y q403
Z q403
Z q503
geophase q403, q503
Y q403
#Gate 0
Y q404
Z q404
Z q504
geophase q404, q504
Y q404
#Gate 0
Y q405
Z q405
Z q505
geophase q405, q505
Y q405
#Gate 0
Y q406
Z q406
Z q506
geophase q406, q506
Y q406
###############################

########## Measurement Z ######
#Gate 0
measz  q400
#Gate 0
measz  q401
#Gate 0
measz  q402
#Gate 0
measz  q403
#Gate 0
measz  q404
#Gate 0
measz  q405
#Gate 0
measz  q406
###############################

########## S Gate #############
#Gate 0
rotz  q500
#Gate 0
rotz  q501
#Gate 0
rotz  q502
#Gate 0
rotz  q503
#Gate 0
rotz  q504
#Gate 0
rotz  q505
#Gate 0
rotz  q506
###############################

########################################################

#APPLY ERROR CORRECTION#################################

#############################################################
######XRECOVER###############################################
#############################################################
#Gate 0
Y q000
Z q000
Z q500
geophase q000, q500
Y q000
#Gate 0
Y q001
Z q001
Z q501
geophase q001, q501
Y q001
#Gate 0
Y q002
Z q002
Z q502
geophase q002, q502
Y q002
#Gate 0
Y q003
Z q003
Z q503
geophase q003, q503
Y q003
#Gate 0
Y q004
Z q004
Z q504
geophase q004, q504
Y q004
#Gate 0
Y q005
Z q005
Z q505
geophase q005, q505
Y q005
#Gate 0
Y q006
Z q006
Z q506
geophase q006, q506
Y q006
########################################################

########################################################
####MEASUREMENT TO CHECK THE RESULT#####################
########################################################
#Gate 0
measx  q000
#Gate 0
measx  q001
#Gate 0
measx  q002
#Gate 0
measx  q003
#Gate 0
measx  q004
#Gate 0
measx  q005
#Gate 0
measx  q006
#############################################################

#############################################################
#Gate 0
c-x q500, q000
#Gate 0
c-x q501, q001
#Gate 0
c-x q502, q002
#Gate 0
c-x q503, q003
#Gate 0
c-x q504, q004
#Gate 0
c-x q505, q005
#Gate 0
c-x q506, q006
########################################################


#############################################################
######zRECOVER###############################################
#############################################################
#Gate 0
Y q500
Z q500
Z q100
geophase q500, q100
Y q500
#Gate 0
Y q501
Z q501
Z q101
geophase q501, q101
Y q501
#Gate 0
Y q502
Z q502
Z q102
geophase q502, q102
Y q502
#Gate 0
Y q503
Z q503
Z q103
geophase q503, q103
Y q503
#Gate 0
Y q504
Z q504
Z q104
geophase q504, q104
Y q504
#Gate 0
Y q505
Z q505
Z q105
geophase q505, q105
Y q505
#Gate 0
Y q506
Z q506
Z q106
geophase q506, q106
Y q506
########################################################

########################################################
####MEASUREMENT TO CHECK THE RESULT#####################
########################################################
#Gate 0
measz  q100
#Gate 0
measz  q101
#Gate 0
measz  q102
#Gate 0
measz  q103
#Gate 0
measz  q104
#Gate 0
measz  q105
#Gate 0
measz  q106
#############################################################

#############################################################
#Gate 0
c-z q500, q100
#Gate 0
c-z q501, q101
#Gate 0
c-z q502, q102
#Gate 0
c-z q503, q103
#Gate 0
c-z q504, q104
#Gate 0
c-z q505, q105
#Gate 0
c-z q506, q106
########################################################
