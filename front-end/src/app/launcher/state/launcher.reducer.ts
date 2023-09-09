import { createFeature, createReducer, on } from '@ngrx/store';
import {LauncherState} from "./launcher.state";
import {launcherActions} from "./launcher.actions";

const initialState: LauncherState = {
  error: undefined,
  currentStep: 1,
  session: undefined
}

export const launcherFeature = createFeature({
  name: 'launcher',
  reducer: createReducer<LauncherState>(
    initialState,
    on(
      launcherActions.runSuccess,
      (state, { session }): LauncherState => ({
        ...state,
        session: session,
      })
    )
  ),
});
