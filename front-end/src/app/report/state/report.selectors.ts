import {createFeatureSelector, createSelector} from '@ngrx/store';
import {ReportState} from "./report.state";

export const selectReportState = createFeatureSelector<ReportState>('reportState');

export const selectLogs = createSelector(
  selectReportState,
  (state: ReportState) => state.logs
);

export const selectLocationFitnessChart = createSelector(
  selectReportState,
  (state: ReportState) => state.locationFitnessChart
);

export const selectAllocationFitnessChart = createSelector(
  selectReportState,
  (state: ReportState) => state.allocationFitnessChart
);

export const selectCostDistributionChart = createSelector(
  selectReportState,
  (state: ReportState) => state.costDistributionChart
);

export const fromReportSelectors = {
  selectReportState,
  selectLogs,
  selectLocationFitnessChart,
  selectAllocationFitnessChart,
  selectCostDistributionChart
}
