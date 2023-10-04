import {createReducer, on} from '@ngrx/store';
import {sessionActions} from './session.actions';
import {SessionState} from './session.state';

const initialState: SessionState = {
	activeSession: null,
	sessions: [],
};

export const sessionReducer = createReducer<SessionState>(
		initialState,
		on(sessionActions.createSession, (state, {activeSession}) => {
			const sessions = state.sessions ? [...state.sessions, activeSession] : [activeSession];
			return {...state, activeSession: activeSession, sessions: sessions};
		}),
    on(sessionActions.updateSessionSTATUS, (state, {status}) => {
      const activeSession = {...state.activeSession, status};
      return {...state, activeSession};
    }),
    on(sessionActions.deleteSession, (state, {id}) => {
      const sessions = state.sessions.filter(session => session.id !== id);
      return {...state, sessions};
    }),
    on(sessionActions.activateSession, (state, {id}) => {
      const activeSession = state.sessions.find(session => session.id === id);
      return {...state, activeSession};
    }),
    on(sessionActions.resetActiveSession, (state) => {
      return {...state, activeSession: null};
    }),
    on(sessionActions.loadStoredSessions, (state, {sessions}) => {
      return {...state, sessions: sessions};
    })
);
