import {ReportState} from "./report.state";
import {createReducer, on} from "@ngrx/store";
import {reportActions} from "./report.actions";


const initialState: ReportState = {
  logs: [],
  locationFitnessChart: {
    data: [],
    metadata: {}
  },
  allocationFitnessChart: {
    data: [],
    metadata: {}
  },
  costDistributionChart: {
    data: [],
    metadata: {}
  }
}

export const reportReducer = createReducer<ReportState>(
  initialState,
  on(reportActions.updateLogs, (state, {log}) => {
    const logs = state.logs ? [...state.logs, log] : [log];
    return {...state, logs};
  }),
  on(reportActions.updateAllocationFitnessChart, (state, {metadata, data}) => {
    return {...state, allocationFitnessChart: {metadata, data}};
  }),
  on(reportActions.updateLocationFitnessChart, (state, {metadata, data}) => {
    return {...state, locationFitnessChart: {metadata, data}};
  }),
  on(reportActions.updateCostDistributionChart, (state, {metadata, data}) => {
    return {...state, costDistributionChart: {metadata, data}};
  }),
  on(reportActions.resetCharts, (state) => {
    return {
      ...state,
      locationFitnessChart: {metadata: {}, data: []},
      allocationFitnessChart: {metadata: {}, data: []},
      costDistributionChart: {metadata: {}, data: []}
    };
  })
);
