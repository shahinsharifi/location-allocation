import {ReportState} from "./report.state";
import {createReducer, on} from "@ngrx/store";
import {reportActions} from "./report.actions";


export const initialState: ReportState = {
  logs: [],
  locationFitness: [],
  allocationFitness: [],
  travelCostDistribution: []
};

export const reportReducer = createReducer<ReportState>(
  initialState,
  on(reportActions.updateLogs, (state, {log}) => {
    const logs = state.logs ? [...state.logs, log] : [log];
    return {...state, logs};
  }),
  on(reportActions.updateLocationFitness, (state, {locationFitness}) => {
    const newLocationFitness = state.locationFitness ? [...state.locationFitness, locationFitness] : [locationFitness];
    return { ...state, locationFitness: newLocationFitness };
  }),
  on(reportActions.updateAllocationFitness, (state, {allocationFitness}) => {
    const newAllocationFitness = state.allocationFitness ? [...state.allocationFitness, allocationFitness] : [allocationFitness];
    return { ...state, allocationFitness: newAllocationFitness };
  }),
  on(reportActions.updateAllocationTravelCostDistribution, (state, {travelCostDistribution}) => {
    const newTravelCostDistribution = state.travelCostDistribution ? [...state.travelCostDistribution, travelCostDistribution] : [travelCostDistribution];
    return { ...state, travelCostDistribution: newTravelCostDistribution };
  })
);
