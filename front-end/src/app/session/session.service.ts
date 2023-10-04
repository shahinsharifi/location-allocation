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
    ).subscribe((newSession) => {
      this.store.dispatch(sessionActions.createSession({activeSession: newSession}));
    });
  }

  stopSession(session: Session) {
    this.commandService.execute(
      `abort`, 'POST', session
    ).subscribe((newSession) => {
      this.store.dispatch(sessionActions.updateSessionSTATUS({status: newSession.status}));
    });
  }

  loadSessions(sessionIds: string[]){
    return this.commandService.execute(
      `get_sessions`, 'POST', sessionIds
    );
  }

}
