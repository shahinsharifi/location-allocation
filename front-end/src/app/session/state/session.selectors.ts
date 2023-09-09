import {createFeatureSelector, createSelector} from '@ngrx/store';
import {SessionState} from "./session.state";


export const selectSessionState = createFeatureSelector<SessionState>('session');

export const selectCurrentSession = createSelector(
  selectSessionState,
  (state: SessionState) => state.currentSession
);
