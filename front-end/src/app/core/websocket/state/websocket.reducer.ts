import { createFeature, createReducer, on } from '@ngrx/store';
import {Message, WebSocketMessageType, WebSocketState} from "./websocket.state";
import {websocketActions} from "./websocket.actions";



export const initialState: WebSocketState = {
  connected: false,
  error: null,
  message: null
};

export const webSocketFeature = createFeature({
  name: 'websocket',
  reducer: createReducer<WebSocketState>(
    initialState,

    on(websocketActions.initConnectionSuccess, state => ({
      ...state,
      connected: true
    })),

    on(websocketActions.initConnectionFailure, (state, { error }) => ({
      ...state,
      connected: false,
      error
    })),

    on(websocketActions.closeConnectionSuccess, state => ({
      ...state,
      connected: false
    })),

    on(websocketActions.sendMessage, (state, { topic, message}) => {
      const sendMessage: Message = {
        type: WebSocketMessageType.SEND,
        topic: topic,
        data: message
      };
      return {
        ...state,
        message: sendMessage
      };
    }),

    on(websocketActions.receiveMessage, (state, { message}) => {
      const receiveMessage: Message = {
        type: WebSocketMessageType.RECEIVE,
        topic: message.topic,
        action: message.action,
        data: message.data
      };
      return {
        ...state,
        message: receiveMessage
      };
    }),
  )
});
