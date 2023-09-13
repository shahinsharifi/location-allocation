import { Injectable } from '@angular/core';
import {CommandService} from "../core/http/command.service";
import {Session} from "../session/session";
import {Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {sessionActions} from "../session/state/session.actions";

@Injectable({
  providedIn: 'root'
})
export class LauncherService {

  constructor(private commandService: CommandService, private store: Store<AppState>) { }

  start(payload: Session) {
    this.commandService.execute(
      `start`, 'POST', 'json', payload, true
    ).subscribe((session) => {
      this.store.dispatch(sessionActions.createSession({activeSession: session}));
    });
  }

  stop(payload: Session) {
    this.commandService.execute(
      `stop`, 'POST', 'json', payload, true
    ).subscribe((session) => {
      this.store.dispatch({type: 'Create Session', payload: session});
    });
  }
}
