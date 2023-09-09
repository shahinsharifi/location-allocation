import {createFeature, createReducer, on} from '@ngrx/store';
import {sessionActions} from "./session.actions";
import {SessionState} from "./session.state";


const initialState: SessionState = {
  error: undefined,
  loaded: false,
  selected: undefined,
  sessions: undefined,
}

export const sessionFeature = createFeature({
  name: 'session',
  reducer: createReducer<SessionState>(
    initialState,
    on(sessionActions.sessionSelected, (state, selected) => {
      return {...state, selected: selected};
    }),
  ),
});
