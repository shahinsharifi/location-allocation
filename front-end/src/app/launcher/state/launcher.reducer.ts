import {createReducer, on} from '@ngrx/store';
import {LauncherState} from "./launcher.state";
import {launcherActions} from "./launcher.actions";


const initialState: LauncherState = {
  sessionId: null,
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

  on(launcherActions.changeStep, (state, { stepIndex }) => ({
    ...state,
    stepIndex
  })),

  on(launcherActions.toggleSelection, (state, { active }) => ({
    ...state,
    selection: {
      ...state.selection,
      active
    }
  })),

  on(launcherActions.startProcess, (state, session ) => ({
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

  on(launcherActions.clearSelection, state => ({
    ...state,
    selection: {
      active: false,
      wkt: null,
      selectedRegions: 0
    },
    buttons: {
      ...state.buttons,
      clear: false
    }
  })),

  on(launcherActions.resetSession, () => initialState)
);
