import {Injectable} from '@angular/core';
import {tap} from 'rxjs/operators';
import {LauncherService} from "../launcher.service";
import {launcherActions} from "./launcher.actions";
import {Actions, createEffect, ofType} from "@ngrx/effects";



@Injectable()
export class LauncherEffects {
  constructor(private actions$: Actions, private launcherService: LauncherService) {}

  run$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(launcherActions.run),
      tap(session => this.launcherService.start(session)),
    )
  }, {dispatch: false});

  stop$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(launcherActions.stop),
      tap(session => this.launcherService.stop(session)),
    )
  }, {dispatch: false});

}

