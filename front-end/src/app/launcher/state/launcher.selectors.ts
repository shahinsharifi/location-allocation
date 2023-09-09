import { createFeatureSelector, createSelector } from '@ngrx/store';
import {LauncherState} from "./launcher.state";

export const selectLauncherState = createFeatureSelector<LauncherState>('launcher');
export const selectLauncherSession = createSelector(selectLauncherState, (state) => state.session);
export const fromLauncher = {
  selectLauncherState,
  selectLauncherSession
};
