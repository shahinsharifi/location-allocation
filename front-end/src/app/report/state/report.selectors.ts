import { createFeatureSelector, createSelector } from '@ngrx/store';
import {ReportState} from "./report.state";

export const selectReportState = createFeatureSelector<ReportState>('reportState');

export const selectLogs = createSelector(
  selectReportState,
  (state: ReportState) => state.logs
);

export const selectLocationFitness = createSelector(
  selectReportState,
  (state: ReportState) => state.location && state.location.fitness
);

export const selectAllocationFitness = createSelector(
  selectReportState,
  (state: ReportState) => state.allocation && state.allocation.fitness
);

export const selectAllocationTravelCostDistribution = createSelector(
  selectReportState,
  (state: ReportState) => state.allocation && state.allocation.travelCostDistribution
);

export const fromResultSelectors = {
  selectReportState,
  selectLogs,
  selectLocationFitness,
  selectAllocationFitness,
  selectAllocationTravelCostDistribution
}
