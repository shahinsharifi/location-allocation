import {createFeatureSelector, createSelector} from "@ngrx/store";
import {WebSocketState} from "./websocket.state";


export const selectWebSocketState = createFeatureSelector<WebSocketState>('websocket');
export const selectConnectionStatus = createSelector(selectWebSocketState, (state) => state.connected);
export const selectWebsocketMessageReceive = createSelector(selectWebSocketState, (state) => state.message);

export const fromWebsocket = {
  selectWebSocketState,
  selectConnectionStatus,
  selectWebsocketMessageReceive
};
