import {createFeature, createReducer, on} from '@ngrx/store';
import {sessionActions} from "./session.actions";
import {SessionState} from "./session.state";


const initialState: SessionState = {
  activeSession: undefined,
  sessions: undefined,
}

export const sessionFeature = createFeature({
  name: 'session',
  reducer: createReducer<SessionState>(
    initialState,
    on(sessionActions.createSession, (state, newState) => {
      let sessions = state.sessions;
      if (sessions === undefined) {
        sessions = [];
        sessions.push(newState.activeSession);
      }
      return {...state, sessions: sessions, activeSession: newState.activeSession};
    }),
  ),
});
