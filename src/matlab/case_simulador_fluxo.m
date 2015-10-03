function fluxo = case_simulador_fluxo(mpcBus, mpcGen, mpcBranch, potenciaBase)

resultado1 = runpf(case_simulador(mpcBus, mpcGen, mpcBranch, potenciaBase), mpoption('OUT_ALL', 0));

fluxo.resultado = resultado1;
end