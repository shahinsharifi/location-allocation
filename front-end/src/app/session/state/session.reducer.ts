import { createReducer, on } from '@ngrx/store';
import { sessionActions } from './session.actions';
import { SessionState } from './session.state';
import {SessionStatus} from "../session";

const initialState: SessionState = {
  activeSession: undefined,
  sessions: [],
};

export const sessionReducer = createReducer<SessionState>(
  initialState,
  on(sessionActions.createSession, (state, {activeSession}) => {
    const sessions = state.sessions ? [...state.sessions, activeSession] : [activeSession];
    return { ...state, sessions, activeSession };
  }),
  on(sessionActions.updateSession, (state, {activeSession}) => {
    return { ...state, activeSession };
  }),
  on(sessionActions.deleteSession, (state, {activeSession}) => {
    const sessions = state.sessions.filter(session => session.id !== activeSession.id);
    return { ...state, sessions, activeSession: undefined };
  }),
  on(sessionActions.activateSession, (state, {activeSession}) => {
    return { ...state, activeSession };
  }),
  on(sessionActions.resetSession, (state) => {
    return {
      ...state,
      activeSession: {
        ...initialState.activeSession,
        status: SessionStatus.INIT
      }
    };
  }),
);
