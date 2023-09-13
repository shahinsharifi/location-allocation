import {ResultState} from "./result.state";
import {createReducer, on} from "@ngrx/store";
import {resultActions} from "./result.actions";



export const initialState: ResultState = {
  logs: [],
  progress: []
};

export const resultReducer = createReducer<ResultState>(
  initialState,
  on(resultActions.updateLogs, (state, {log}) => {
    const logs = state.logs ? [...state.logs, log] : [log];
    return { ...state, logs };
  })
);
