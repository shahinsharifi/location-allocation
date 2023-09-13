import {Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {tap} from 'rxjs/operators';
import {websocketActions} from "./websocket.actions";
import {WebsocketService} from "../websocket.service";


@Injectable()
export class WebSocketEffects {
  constructor(private actions$: Actions, private websocketService: WebsocketService) {
  }

  initConnectionSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(websocketActions.initConnectionSuccess),
        tap(() => console.log('initConnectionSuccess'))
      ),
    {dispatch: false}
  );


  receiveMessage$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(websocketActions.receiveMessage),
        tap(({message}) => {
          this.websocketService.handleMessage(message);
        })
      ),
    {dispatch: false}
  );

}

