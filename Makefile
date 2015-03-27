.PHONY: all clean

#Gurobi path
ifndef GUROBI_HOME
GUROBI_HOME = /opt/gurobi/linux64
endif

all: init

init:
	@echo "****Building Quantum Physical Designer****"
	ant

no_gurobi: init
	@echo "****Building qpOASES Octave Modules****"
	cd src/libs/qpOASES-3.0beta/interfaces/octave; octave -q --no-window-system make.m

	
clean:
	ant clean
	cd src/libs/qpOASES-3.0beta/interfaces/octave && sh ./clean.sh
