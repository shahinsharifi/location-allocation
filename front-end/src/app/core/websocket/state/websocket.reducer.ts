import {createReducer, on} from '@ngrx/store';
import {websocketActions} from "./websocket.actions";
import {WebSocketState} from "./websocket.state";


export const initialState: WebSocketState = {
  connected: false,
  error: null,
  message: null,
};
export const webSocketReducer = createReducer<WebSocketState>(
  initialState,
  on(websocketActions.initConnectionSuccess, (state) => ({ ...state, connected: true })),
  on(websocketActions.initConnectionFailure, (state, { error }) => ({ ...state, connected: false, error })),
  on(websocketActions.closeConnectionSuccess, (state) => ({ ...state, connected: false })),
  on(websocketActions.receiveMessage, (state, { message }) => ({ ...state, message })),
);
