import {ReportState} from "./report.state";
import {createReducer, on} from "@ngrx/store";
import {reportActions} from "./report.actions";


export const initialState: ReportState = {
  logs: undefined,
  location: {
    fitness: undefined,
  },
  allocation: {
    fitness: undefined,
    travelCostDistribution: undefined,
  },
};

export const reportReducer = createReducer<ReportState>(
  initialState,
  on(reportActions.updateLogs, (state, {log}) => {
    const logs = state.logs ? [...state.logs, log] : [log];
    return {...state, logs};
  }),
  on(reportActions.updateLocationFitness, (state, {fitness}) => {
    const _fitness = state.location.fitness ? [...state.location.fitness, fitness] : [fitness];
    return {
      ...state,
      ...state.location,
      fitness: _fitness
    };
  }),
  on(reportActions.updateAllocationFitness, (state, {fitness}) => {
      const _fitness = state.allocation.fitness ? [...state.allocation.fitness, fitness] : [fitness];
      return {
        ...state,
        ...state.allocation,
        fitness: _fitness
      };
    }),
  on(reportActions.updateAllocationTravelCostDistribution, (state, {travelCostDistribution}) => {
      return {
        ...state,
        ...state.allocation,
        travelCostDistribution: travelCostDistribution
      };
    }
  ),
);
