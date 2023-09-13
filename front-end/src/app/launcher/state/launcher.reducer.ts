import { createFeature, createReducer} from '@ngrx/store';
import {LauncherState} from "./launcher.state";

const initialState: LauncherState = {
  step: 1
}

export const launcherFeature = createFeature({
  name: 'launcher',
  reducer: createReducer<LauncherState>(
    initialState,
  ),
});
