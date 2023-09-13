import {createActionGroup, emptyProps, props} from "@ngrx/store";
import {Message} from "../message";


export enum WebSocketActionTypes {
  InitConnection = 'Init Connection',
  InitConnectionSuccess = 'Init Connection Success',
  InitConnectionFailure = 'Init Connection Failure',
  CloseConnection = 'Close Connection',
  CloseConnectionSuccess = 'Close Connection Success',
  CloseConnectionFailure = 'Close Connection Failure',
  SendMessage = 'Send Message',
  ReceiveMessage = 'Receive Message'
}

export const websocketActions = createActionGroup({
  source: 'Websocket',
  events: {
    [WebSocketActionTypes.InitConnection]: emptyProps(),
    [WebSocketActionTypes.InitConnectionSuccess]: emptyProps(),
    [WebSocketActionTypes.InitConnectionFailure]: props<{ error }>(),

    [WebSocketActionTypes.CloseConnection]: emptyProps(),
    [WebSocketActionTypes.CloseConnectionSuccess]: emptyProps(),
    [WebSocketActionTypes.CloseConnectionFailure]: props<{ error }>(),

    [WebSocketActionTypes.SendMessage]: props<{ topic, message: Message }>(),
    [WebSocketActionTypes.ReceiveMessage]: props<{message: Message}>()
  }
});



