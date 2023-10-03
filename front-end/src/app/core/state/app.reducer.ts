import {ActionReducer, ActionReducerMap, MetaReducer} from '@ngrx/store';
import {AppState} from "./app.state";
import {mapReducer} from "../../map/state/map.reducer";
import {webSocketReducer} from "../websocket/state/websocket.reducer";
import {launcherReducer} from "../../launcher/state/launcher.reducer";
import {sessionReducer} from "../../session/state/session.reducer";
import {reportReducer} from "../../report/state/report.reducer";


export const appReducers: ActionReducerMap<AppState> = {
  websocket: webSocketReducer,
  launcher: launcherReducer,
  session: sessionReducer,
  report: reportReducer,
  map: mapReducer
};

function localStorageSyncReducer(reducer: ActionReducer<any>): ActionReducer<any> {
  return (state, action) => {
    const nextState = reducer(state, action);
    if (nextState.session.activeSession) {
      localStorage.setItem(`appState_${nextState.session.activeSession.id}`, JSON.stringify(nextState));
    }
    return nextState;
  };
}
export const metaReducers: Array<MetaReducer<any, any>> = [localStorageSyncReducer];

