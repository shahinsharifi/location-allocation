import {Injectable} from '@angular/core';
import {switchMap, map} from 'rxjs/operators';
import {LauncherService} from "../launcher.service";
import {launcherActions} from "./launcher.actions";
import {Actions, createEffect, ofType} from "@ngrx/effects";


@Injectable()
export class LauncherEffects {

  run$ = createEffect(() => {
    return this.actions$.pipe(
      ofType(launcherActions.run),
      switchMap((session) => this.launcherService.start(session)),
      map((session) => launcherActions.runSuccess({session}))
    );
  });

  stop$ = createEffect(() =>
    this.actions$.pipe(
      ofType(launcherActions.stop),
      switchMap(session =>
        this.launcherService.stop(session).pipe(
          map(session => launcherActions.stopSuccess({session}))
        )
      )
    )
  );

  constructor(private actions$: Actions, private launcherService: LauncherService) {}
}

