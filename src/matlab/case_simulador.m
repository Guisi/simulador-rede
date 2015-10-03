function mpc = case_simulador(mpcBus, mpcGen, mpcBranch, potenciaBase)
%% CASE BASE   Power flow data de um sistema com 4 feeders 
  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%% MATPOWER Case Format : Version 2 %%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
mpc.version = '2';

%%-----  Power Flow Data  -----%%
%% system MVA base
mpc.baseMVA = potenciaBase/1e6;

%% bus data
mpc.bus = mpcBus; 
	   

%% generator data
mpc.gen = mpcGen;

%% branch data
mpc.branch = mpcBranch;
clc;
