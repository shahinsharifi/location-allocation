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

  startProcess(session: Session) {
    this.commandService.execute(
      `start`, 'POST', session
    ).subscribe((session) => {
      this.store.dispatch(sessionActions.createSession({activeSession: session}));
    });
  }

  stopProcess(session: Session) {
    this.commandService.execute(
      `abort`, 'POST', session
    ).subscribe((session) => {
      this.store.dispatch(sessionActions.updateSession({activeSession: session}));
    });
  }

  resumeProcess(session: Session) {
    this.commandService.execute(
      `resume`, 'POST', session
    ).subscribe((session) => {
      this.store.dispatch({type: 'Create Session', payload: session});
    });
  }
}
