import {Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {tap} from 'rxjs/operators';
import {websocketActions} from "./websocket.actions";
import {WebsocketService} from "../websocket.service";



@Injectable()
export class WebSocketEffects {
  constructor(private actions$: Actions, private webSocketService: WebsocketService) {
  }

  initConnectionSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(websocketActions.initConnectionSuccess),
        tap(() => console.log('initConnectionSuccess'))
      ),
    {dispatch: false}
  );

  sendMessage$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(websocketActions.sendMessage),
        tap(({topic, message}) => this.webSocketService.sendMessage(topic, message))
      ),
    {dispatch: false}
  );


  receiveMessage$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(websocketActions.receiveMessage),
        tap(({message}) => {
          console.log(message.topic);
          switch (message.topic) {
            default:
              console.warn('Unhandled WebSocket message type:', message.topic);
          }
        })
      ),
    {dispatch: false}
  );

}

