import { Injectable } from '@angular/core';
import {Actions, createEffect, ofType} from "@ngrx/effects";
import {tap} from "rxjs/operators";
import {sessionActions} from "./session.actions";


@Injectable()
export class SessionEffects {

  sessionSelected$ = createEffect(() =>
    this.actions$.pipe(
      ofType(sessionActions.sessionSelected),
      tap(session =>
        sessionActions.sessionSelected(session)
      )
    )
  );

  constructor(private actions$: Actions) {}

}
