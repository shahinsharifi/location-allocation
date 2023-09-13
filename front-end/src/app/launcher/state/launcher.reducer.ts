import { createReducer} from '@ngrx/store';
import {LauncherState} from "./launcher.state";

const initialState: LauncherState = {
  step: 1
}

export const launcherReducer = createReducer<LauncherState>(
    initialState,
);
