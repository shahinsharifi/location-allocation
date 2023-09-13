import {ActionReducer, ActionReducerMap, MetaReducer} from '@ngrx/store';
import {AppState} from "./app.state";
import {localStorageSync} from "ngrx-store-localstorage";
import {mapReducer} from "../../map/state/map.reducer";
import {webSocketReducer} from "../websocket/state/websocket.reducer";
import {launcherReducer} from "../../launcher/state/launcher.reducer";
import {sessionReducer} from "../../session/state/session.reducer";
import {resultReducer} from "../../result/state/result.reducer";


export const appReducers: ActionReducerMap<AppState> = {
  websocket: webSocketReducer,
  launcher: launcherReducer,
  session: sessionReducer,
  result: resultReducer,
  map: mapReducer
};

function localStorageSyncReducer(reducer: ActionReducer<any>): ActionReducer<any> {
  return localStorageSync({keys: ['user'], rehydrate: true})(reducer);
}
export const metaReducers: Array<MetaReducer<any, any>> = [localStorageSyncReducer];

