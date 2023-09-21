import {ReportState} from "./report.state";
import {createReducer, on} from "@ngrx/store";
import {reportActions} from "./report.actions";



export const initialState: ReportState = {
  logs: null,
  progress: null
};

export const reportReducer = createReducer<ReportState>(
  initialState,
  on(reportActions.updateLogs, (state, {log}) => {
    const logs = state.logs ? [...state.logs, log] : [log];
    return { ...state, logs };
  }),
  on(reportActions.updateProgress, (state, {progress}) => {
    const newProgress = state.progress ? [...state.progress, progress] : [progress];
    return { ...state, progress: newProgress };
  })
);
