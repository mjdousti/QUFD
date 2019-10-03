# QUFD: Quantum ULB Factory Designer

## Description:
Quantum ULB Factory Designer (QUFD) maps a given QASM file to a supplied PMD fabric. The resultant MCL file of the mapped circuit will be generated. This tool designs Ion Trap tiles to be used by Quantum Physical Designer (such as HL-QSPR or Squash). It calculates the physical resource requirements for the mapping process.

## Directories & Files Structure
```
QUFD
|-- build.xml -> Ant build file
|-- FTGates -> QASM files for each fault-tolerant gate
|-- PMD
	|-- IonTrap.xml -> IonTrap fabric description
|-- qufd.pdf -> QUFD paper published in QIP 2014
|-- src
	|-- edu -> Java source code directory
	|-- libs
		|-- commons-cli-1.2.jar -> Appache Commons CLI library
		|-- commons-lang3-3.1.jar -> Apache Commons Lang library
		|-- commons-logging-1.1.3.jar -> Apache Commons Logging library
		|-- gurobi.jar -> Gurobi 5.6.2 Java interface
		|-- jar-in-jar-loader.zip -> Jar loader file taken from Eclipse.
		|-- javacc.jar -> Java Compiler Compiler (JavaCC)
		|-- javaoctave-0.6.4.jar ->  JavaOctave library. A bridge from Java to Octave, useful if you want to use the free solver (qpOASES). 
		|-- jgrapht-jdk1.6.jar -> JGraphT library.
		|-- qpOASES-3.0beta -> A free quadratic programming solver.
`-- README -> This readme file.
```

## Requirements
1. [Ant 1.7](http://ant.apache.org) or higher
2. [Oracle Java 7-JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or higher
3. [Gurobi Optimizer 5.6.2](http://www.gurobi.com) (free for academic use)

    or
3) [Octave 3.6](http://www.gnu.org/software/octave) (`liboctave-dev` and `octave-common` packages)

**Note:** If you intend to use any version of Gurobi other than 5.6.2, you must replace `src/libs/gurobi.jar` with the one provided in the version you have (located in the `lib` directory) and recompile the project.

## Preinstall
Make sure that all the requirements are already installed. The following environmental variables should be set before the installation/running of the program.
1. `JAVA_HOME` should point where java and javac binary files are located.
2. `GUROBI_HOME` and `GRB_LICENSE_FILE` should point to the appropriate location. Please refer to the installation readme of Gurobi. `PATH` and `LD_LIBRARY_PATH` should also be updated accordingly.

   or

2) Octave binary file should be in the system `PATH`.


## Compile:
A Makefile takes care of the build process. You may enter the following commands to build and clean the project:
```
$ make            # makes QUFD with Gurobi support
$ make no_gurobi  # makes QUFD with qpOASES support
$ make clean      # cleans the project
```

Again, note that if you intend to use any version of Gurobi other than 5.6.2, you must replace `src/libs/gurobi.jar` with the one provided in the version you have (located in the `lib` directory) and recompile the project.


## Run
Run `java -jar qufd.jar` to perform tile design for Ion-Trap PMD. The options of this command is listed below:
```
usage: qufd [-h] [-i <file>] [-m <file>] [-p <file>] [-q <path>] [-r <file>]
QUFD maps a given QASM to a supplied PMD fabric. The resultant MCL file of the mapped
circuit will be generated.
 -h,--help             Shows this help menu
 -i,--input <file>     QASM input file
 -m,--mcl <file>       MCL output file
 -p,--pmd <file>       PMD file
 -q,--qpOASES <path>   Uses qpOASES solver
 -r,--re <file>        Resource estimation file
```

**Note:** In order to use the free QP solver (qpOASES), the command should be changed as follows:
	java -jar qufd.jar -q src/libs/qpOASES-3.0beta

## Example:
Getting the physical resource estimation for the FT-H gate, Ion Trap PMD, and [[7,1,3]] Steane code:
```
$ java -jar qufd.jar -i FTGates/H.qasm -p PMD/IonTrap.xml -r H.re -m H.mcl
```

Sample Outputs:
```
 --H.re:
rotz    7
c-x     7
measx   21
Qubit   35
rotxy   7
Move    5287
c-z     7
measz   7
geophase        64
Y       128
Z       128

 --H.mcl (first 40 lines):
Total Latency: 6064 us
Qubit count: 35
Qubit q106 is placed @(5,1,5,4).
Qubit q105 is placed @(8,0,2,5).
Qubit q104 is placed @(7,0,3,5).
Qubit q103 is placed @(4,0,6,5).
Qubit q102 is placed @(7,2,3,3).
Qubit q306 is placed @(6,4,4,1).
Qubit q101 is placed @(3,0,7,5).
Qubit q305 is placed @(9,0,1,5).
Qubit q100 is placed @(8,1,2,4).
Qubit q304 is placed @(4,4,6,1).
Qubit q303 is placed @(9,1,1,4).
Qubit q302 is placed @(8,2,2,3).
Qubit q506 is placed @(6,0,4,0).
Qubit q301 is placed @(2,0,8,5).
Qubit q505 is placed @(6,0,4,0).
Qubit q300 is placed @(3,1,7,4).
Qubit q504 is placed @(6,0,4,0).
Qubit q503 is placed @(6,0,4,0).
Qubit q502 is placed @(6,0,4,0).
Qubit q501 is placed @(6,0,5,0).
Qubit q500 is placed @(6,0,5,0).
Qubit q006 is placed @(5,0,5,5).
Qubit q005 is placed @(6,0,4,5).
Qubit q004 is placed @(7,1,3,4).
Qubit q003 is placed @(6,1,4,4).
Qubit q002 is placed @(6,2,4,3).
Qubit q206 is placed @(4,2,6,3).
Qubit q001 is placed @(4,1,6,4).
Qubit q205 is placed @(6,3,4,2).
Qubit q000 is placed @(7,3,3,2).
Qubit q204 is placed @(3,2,7,3).
Qubit q203 is placed @(5,2,5,3).
Qubit q202 is placed @(4,3,6,2).
Qubit q201 is placed @(5,3,5,2).
Qubit q200 is placed @(7,4,3,1).

SimTime: 10     Move q500 (6,0,5,0)->(6,0,4,0)  Move q506 (6,0,4,0)->(6,0,5,0)  Move q501 (6,0,5,0)->(6,0,4,0)  Move q002 (6,2,4,3)->(6,2,4,2)  Move q000 (7,3,3,2)->(7,3,3,1)  Move q005 (6,0,4,5)->(6,0,4,6)  Move q003 (6,1,4,4)->(6,1,4,3)       Move q006 (5,0,5,5)->(5,0,5,4)  Move q001 (4,1,6,4)->(4,1,6,3)  Move q004 (7,1,3,4)->(7,1,3,5)  Move q202 (4,3,6,2)->(4,3,6,1)  Move q200 (7,4,3,1)->(7,4,3,0)  Move q205 (6,3,4,2)->(6,3,4,3)  Move q203 (5,2,5,3)->(5,2,5,4)  Move q206 (4,2,6,3)->(4,2,6,4)       Move q201 (5,3,5,2)->(5,3,5,3)  Move q204 (3,2,7,3)->(3,2,7,2)  Move q100 (8,1,2,4)->(8,1,2,3)  Move q102 (7,2,3,3)->(7,2,3,4)  Move q503 (6,0,4,0)->(6,0,5,0)  Move q504 (6,0,4,0)->(6,0,5,0)  Move q103 (4,0,6,5)->(4,0,6,4)       Move q505 (6,0,4,0)->(6,0,5,0)  Move q105 (8,0,2,5)->(8,0,2,4)  Move q101 (3,0,7,5)->(3,0,7,6)  Move q106 (5,1,5,4)->(5,1,5,5)  Move q104 (7,0,3,5)->(7,0,3,4)  Move q300 (3,1,7,4)->(3,1,7,3)  Move q302 (8,2,2,3)->(8,2,2,2)       Move q303 (9,1,1,4)->(9,1,1,3)  Move q305 (9,0,1,5)->(9,0,1,4)  Move q301 (2,0,8,5)->(2,0,8,6)  Move q306 (6,4,4,1)->(6,4,4,2)  Move q304 (4,4,6,1)->(4,4,6,2)  Move q502 (6,0,4,0)->(6,0,3,0)
SimTime: 20     Move q500 (6,0,4,0)->(6,0,3,0)  Move q502 (6,0,3,0)->(6,0,2,0)  Move q501 (6,0,4,0)->(6,0,4,1)  Move q002 (6,2,4,2)->(6,2,4,1)  Move q003 (6,1,4,3)->(6,1,4,2)  Move q006 (5,0,5,4)->(5,0,5,3)  Move q001 (4,1,6,3)->(4,1,6,2)       Move q200 (7,4,3,0)->(7,3,3,10) Move q205 (6,3,4,3)->(6,3,4,4)  Move q203 (5,2,5,4)->(5,2,5,5)  Move q206 (4,2,6,4)->(4,2,6,5)  Move q201 (5,3,5,3)->(5,3,5,4)  Move q204 (3,2,7,2)->(3,2,7,1)  Move q100 (8,1,2,3)->(8,1,2,2)  Move q102 (7,2,3,4)->(7,2,3,5)       Move q103 (4,0,6,4)->(4,0,6,3)  Move q106 (5,1,5,5)->(5,1,5,6)  Move q104 (7,0,3,4)->(7,0,3,3)  Move q504 (6,0,5,0)->(6,0,6,0)  Move q005 (6,0,4,6)->(6,0,4,7)  Move q503 (6,0,5,0)->(6,0,6,0)  Move q505 (6,0,5,0)->(6,0,6,0)       Move q000 (7,3,3,1)->(7,3,3,0)  Move q004 (7,1,3,5)->(7,1,3,6)  Move q202 (4,3,6,1)->(4,3,6,0)  Move q105 (8,0,2,4)->(8,0,2,3)  Move q101 (3,0,7,6)->(3,0,7,7)  Move q300 (3,1,7,3)->(3,1,7,2)  Move q302 (8,2,2,2)->(8,2,2,1)       Move q303 (9,1,1,3)->(9,1,1,2)  Move q305 (9,0,1,4)->(9,0,1,3)  Move q301 (2,0,8,6)->(2,0,8,7)  Move q306 (6,4,4,2)->(6,4,4,3)  Move q304 (4,4,6,2)->(4,4,6,3)  Move q506 (6,0,5,0)->(6,0,6,0)
```


## Benchmarking Results
The provided tool is fully tested on a server machine with the following specification:
 - OS: Debian Wheezy (Debian 7) AMD64 edition
 - CPU: 4 x Intel Xeon Processor E7-8837 (a total of 32 cores)
 - Memory: 64GB
 - Storage: 6 x HP 300GB 6G SAS 15K RPM SFF

Example runtime result: less than 1 min

**Note:** We have tested QUFD in Windows 7 and it worked flawlessly.


## Developers
* [Hadi Goudarzi](<hgoudarz@usc.edu>)
* [Mohammad Javad Dousti](<dousti@usc.edu>)
* [Alireza Shafaei](<shafaeb@usc.edu>)
* [Massoud Pedram](<pedram@usc.edu>)

## Questions or Bugs?
You may contact [Mohammad Javad Dousti](<dousti@usc.edu>) for any questions you may have or bugs that you find.

## License
Please refer to the [LICENSE](LICENSE) file.


