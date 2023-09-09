import {ActionReducer, ActionReducerMap, MetaReducer} from '@ngrx/store';
import {AppState} from "./app.state";
import {webSocketFeature} from "../websocket/state/websocket.reducer";
import {launcherFeature} from "../../launcher/state/launcher.reducer";
import {sessionFeature} from "../../session/state/session.reducer";
import {localStorageSync} from "ngrx-store-localstorage";


export const appReducers: ActionReducerMap<AppState> = {
  websocket: webSocketFeature.reducer,
  launcher: launcherFeature.reducer,
  session: sessionFeature.reducer,
};

function localStorageSyncReducer(reducer: ActionReducer<any>): ActionReducer<any> {
  return localStorageSync({keys: ['user'], rehydrate: true})(reducer);
}
export const metaReducers: Array<MetaReducer<any, any>> = [localStorageSyncReducer];

