#############################################################
####################Qubit declarations#######################
#######################FT-CNOT(q6 is control)################
#############################################################

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
qubit q500,1,0,50,0,50
qubit q501,1,0,50,0,50
qubit q502,1,0,50,0,50
qubit q503,1,0,50,0,50
qubit q504,1,0,50,0,50
qubit q505,1,0,50,0,50
qubit q506,1,0,50,0,50

#############################################################
#### Logical zero encoded####################################
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
#### Logical zero encoded####################################
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
######INTERACTION############################################
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
####MEASUREMENT TO CHECK THE RESULT#####################
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
#### Logica lpos encoded####################################
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
#### Logica lpos encoded####################################
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
######INTERACTION############################################
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
####MEASUREMENT TO CHECK THE RESULT#####################
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
###### XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX ####################
#############################################################
#Gate 0
rotxy q500
#Gate 0
rotxy q501
#Gate 0
rotxy q502
#Gate 0
rotxy q503
#Gate 0
rotxy q504
#Gate 0
rotxy q505
#Gate 0
rotxy q506
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
