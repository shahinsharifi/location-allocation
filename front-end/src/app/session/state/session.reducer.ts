import { createReducer, on } from '@ngrx/store';
import { sessionActions } from './session.actions';
import { SessionState } from './session.state';

const initialState: SessionState = {
  activeSession: undefined,
  sessions: undefined,
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
);
