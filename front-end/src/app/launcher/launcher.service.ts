import {Injectable} from '@angular/core';
import {Session} from "../session/session";
import {Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {launcherActions} from "./state/launcher.actions";

@Injectable({
  providedIn: 'root'
})
export class LauncherService {
  constructor(private store: Store<AppState>) { }
  startSession(session: Session) {
    this.store.dispatch(launcherActions.startProcess(session));
  }

  stopSession(session: Session) {
    this.store.dispatch(launcherActions.stopProcess(session));
  }

}
