import {Injectable} from '@angular/core';
import {tap} from 'rxjs/operators';
import {LauncherService} from "../launcher.service";
import {launcherActions} from "./launcher.actions";
import {Actions, createEffect, ofType} from "@ngrx/effects";



@Injectable()
export class LauncherEffects {
  constructor(private actions$: Actions, private launcherService: LauncherService) {}

  startProcess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(launcherActions.startProcess),
      tap(session => this.launcherService.startProcess(session))
    )
  }, {dispatch: false});

  stopProcess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(launcherActions.stopProcess),
      tap(sessionId => this.launcherService.stopProcess(sessionId))
    )
  }, {dispatch: false});

}

