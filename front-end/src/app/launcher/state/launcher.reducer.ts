import { createReducer} from '@ngrx/store';
import {LauncherState} from "./launcher.state";

const initialState: LauncherState = {
    stepIndex: 0,
    selection: {
        active: false,
        wkt: null,
        selectedRegions: 0
    },
    buttons: {
        previous: true,
        next: false,
        start: false,
        stop: false,
        reset: true,
        resume: false,
        clear: false
    }
};

export const launcherReducer = createReducer<LauncherState>(
    initialState,
);
