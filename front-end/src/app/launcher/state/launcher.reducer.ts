import {createReducer, on} from '@ngrx/store';
import {LauncherState} from "./launcher.state";
import {launcherActions} from "./launcher.actions";
import {SessionStatus} from "../../session/session";


const initialState: LauncherState = {
  stepIndex: 0,
  activeSession: null,
  selection: {
    activeDrawing: false,
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

  on(launcherActions.changeStep, (state, {stepIndex}) => ({
    ...state,
    stepIndex
  })),

  on(launcherActions.activateDrawing, (state,{activeDrawing}) => {
    return {
      ...state,
      selection: {
        ...state.selection,
        activeDrawing: activeDrawing
      }
    };
  }),

  on(launcherActions.startProcess, (state, session) => ({
    ...state,
    activeSession: session,
    buttons: {
      ...state.buttons,
      start: false,
      stop: true
    }
  })),
  on(launcherActions.stopProcess, (state, session) => ({
    ...state,
    activeSession: {
      ...state.activeSession,
      id: session.id,
      status: SessionStatus.ABORT
    },
    buttons: {
      ...state.buttons,
      start: true,
      stop: false,
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

  on(launcherActions.resetSession, () => ({
    ...initialState,
    activeSession: {
      status: SessionStatus.RESET
    }
  })),
);
