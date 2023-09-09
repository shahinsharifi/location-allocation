import {createFeatureSelector, createSelector} from "@ngrx/store";
import {of} from "rxjs";
import {WebSocketMessageType, WebSocketState} from "./websocket.state";


export const selectWebSocketState = createFeatureSelector<WebSocketState>('websocket');
export const selectConnectionStatus = createSelector(selectWebSocketState, (state) => state.connected);
export const selectWebsocketMessageSend = createSelector(selectWebSocketState, (state) => state.message.type === WebSocketMessageType.SEND ? state.message : of(null));
export const selectWebsocketMessageReceive = createSelector(selectWebSocketState, (state) => state.message.type === WebSocketMessageType.RECEIVE ? state.message : of(null));


export const fromWebsocket = {
  selectWebSocketState,
  selectConnectionStatus,
  selectWebsocketMessageSend,
  selectWebsocketMessageReceive
};
