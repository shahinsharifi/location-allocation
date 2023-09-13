import {createFeatureSelector, createSelector} from '@ngrx/store';
import {SessionState} from "./session.state";


export const selectSessionState = createFeatureSelector<SessionState>('session');

export const selectActiveSession = createSelector(
  selectSessionState,
  (state: SessionState) => state.activeSession
);


export const fromSessionSelectors = {
  selectSessionState,
  selectActiveSession
};
