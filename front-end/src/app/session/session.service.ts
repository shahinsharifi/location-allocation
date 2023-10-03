import {Injectable} from '@angular/core';
import {Session} from "./session";
import {CommandService} from "../core/http/command.service";
import {Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {sessionActions} from "./state/session.actions";


@Injectable({
  providedIn: 'root'
})
export class SessionService {

  constructor(private commandService: CommandService, private store: Store<AppState>) { }

  startSession(session: Session) {
    this.commandService.execute(
      `start`, 'POST', session
    ).subscribe((session) => {
      this.store.dispatch(sessionActions.startSession(session));
    });
  }

  stopSession(session: Session) {
    this.commandService.execute(
      `abort`, 'POST', session
    ).subscribe((session) => {
      this.store.dispatch(sessionActions.stopSession(session));
    });
  }

}
