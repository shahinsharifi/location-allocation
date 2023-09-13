import { createFeatureSelector, createSelector } from '@ngrx/store';
import {ResultState} from "./result.state";

export const selectResultState = createFeatureSelector<ResultState>('resultState');

export const selectLogs = createSelector(
  selectResultState,
  (state: ResultState) => state.logs
);

export const selectProgress = createSelector(
  selectResultState,
  (state: ResultState) => state.progress
);

export const fromResultSelectors = {
  selectResultState,
  selectLogs,
  selectProgress
}
