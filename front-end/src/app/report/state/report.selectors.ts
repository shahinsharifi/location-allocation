import { createFeatureSelector, createSelector } from '@ngrx/store';
import {ReportState} from "./report.state";

export const selectReportState = createFeatureSelector<ReportState>('reportState');

export const selectLogs = createSelector(
  selectReportState,
  (state: ReportState) => state.logs
);

export const selectLocationFitness = createSelector(
  selectReportState,
  (state: ReportState) => state.locationFitness
);

export const selectAllocationFitness = createSelector(
  selectReportState,
  (state: ReportState) => state.allocationFitness
);

export const selectTravelCostDistribution = createSelector(
  selectReportState,
  (state: ReportState) => state.travelCostDistribution
);

export const fromReportSelectors = {
  selectReportState,
  selectLogs,
  selectLocationFitness,
  selectAllocationFitness,
  selectTravelCostDistribution
}
