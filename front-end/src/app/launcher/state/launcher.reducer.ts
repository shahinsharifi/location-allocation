import {createReducer, on} from '@ngrx/store';
import {LauncherState} from "./launcher.state";
import {launcherActions} from "./launcher.actions";

const initialState: LauncherState = {
  stepIndex: 0,
  selection: {
    active: false,
    wkt: null,
    selectedRegions: 0
  },
  buttons: {
    previous: true,
    next: false,
    start: false,
    stop: false,
    reset: true,
    resume: false,
    clear: false
  }
};

export const launcherReducer = createReducer<LauncherState>(
  initialState,
  on(launcherActions.changeStep, (state, {stepIndex}) => ({...state, stepIndex})),
  on(launcherActions.toggleSelection, (state, {active}) => ({
    ...state,
    selection: {
      ...state.selection,
      active
    }
  })),
  on(launcherActions.resetSession, () => initialState)
);
