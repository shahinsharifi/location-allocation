import { createFeatureSelector } from '@ngrx/store';
import {LauncherState} from "./launcher.state";

export const selectLauncherState = createFeatureSelector<LauncherState>('launcher');
export const fromLauncher = {
  selectLauncherState
};
