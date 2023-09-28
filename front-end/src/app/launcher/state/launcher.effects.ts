import {Injectable} from '@angular/core';
import {tap} from 'rxjs/operators';
import {LauncherService} from "../launcher.service";
import {launcherActions} from "./launcher.actions";
import {Actions, createEffect, ofType} from "@ngrx/effects";
import {MapService} from "../../map/map.service";



@Injectable()
export class LauncherEffects {
  constructor(private actions$: Actions,
              private launcherService: LauncherService, private mapService: MapService) {}

  startProcess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(launcherActions.startProcess),
      tap(session => this.launcherService.startProcess(session))
    )
  }, {dispatch: false});

  stopProcess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(launcherActions.stopProcess),
      tap(session => this.launcherService.stopProcess(session))
    )
  }, {dispatch: false});

  resumeProcess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(launcherActions.resumeProcess),
      tap(session => this.launcherService.resumeProcess(session))
    )
  }, {dispatch: false});

  clearSelection$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(launcherActions.clearSelection),
      tap(() => this.mapService.clearSelection())
    )
  }, {dispatch: false});

  resetSession$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(launcherActions.resetSession),
      tap(() => this.mapService.resetMap())
    )
  }, {dispatch: false});


}
