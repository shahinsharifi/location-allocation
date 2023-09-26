import {createReducer, on} from '@ngrx/store';
import {LauncherState} from "./launcher.state";
import {launcherActions} from "./launcher.actions";


const initialState: LauncherState = {
  sessionId: null,
  stepIndex: 0,
  selection: null,
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

  on(launcherActions.changeStep, (state, {stepIndex}) => ({
    ...state,
    stepIndex
  })),

  on(launcherActions.toggleSelection, (state,{active}) => {
    return {
      ...state,
      selection: {
        ...state.selection,
        active: active
      }
    };
  }),

  on(launcherActions.startProcess, (state, session) => ({
    ...state,
    sessionId: session.id,
    buttons: {
      ...state.buttons,
      start: false,
      stop: true
    }
  })),

  on(launcherActions.stopProcess, state => ({
    ...state,
    buttons: {
      ...state.buttons,
      stop: false,
      resume: true,
      clear: true
    }
  })),

  on(launcherActions.resumeProcess, state => ({
    ...state,
    buttons: {
      ...state.buttons,
      resume: false,
      stop: true,
      clear: false
    }
  })),

  on(launcherActions.clearSelection, (state) => {
    return {
      ...state,
      selection: {
        ...state.selection,
        wkt: null,
        selectedRegions: 0
      }
    };
  }),

  on(launcherActions.resetSession, () => ({ ...initialState })),
);
