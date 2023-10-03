import {Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from "@ngrx/effects";
import {SessionService} from "../session.service";
import {tap} from "rxjs/operators";
import {sessionActions} from "./session.actions";


@Injectable()
export class SessionEffects {

  constructor(private actions$: Actions, private sessionService: SessionService) {}

  startSession$ = createEffect(() =>
      this.actions$.pipe(
        ofType(sessionActions.startSession),
        tap((activeSession) => {
          this.sessionService.startSession(activeSession);
        }
      )),
    {dispatch: false}
  );

  stopSession$ = createEffect(() =>
      this.actions$.pipe(
        ofType(sessionActions.stopSession),
        tap((activeSession) => {
          this.sessionService.stopSession(activeSession);
        }
      )),
    {dispatch: false}
  );

}
