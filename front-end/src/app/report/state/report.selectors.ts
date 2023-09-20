import { createFeatureSelector, createSelector } from '@ngrx/store';
import {ReportState} from "./report.state";

export const selectReportState = createFeatureSelector<ReportState>('reportState');

export const selectLogs = createSelector(
  selectReportState,
  (state: ReportState) => state.logs
);

export const selectProgress = createSelector(
  selectReportState,
  (state: ReportState) => state.progress
);

export const fromResultSelectors = {
  selectReportState,
  selectLogs,
  selectProgress
}
