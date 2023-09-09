import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {Session} from "./session";
import {Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {sessionActions} from "./state/session.actions";


@Injectable({
  providedIn: 'root'
})
export class SessionService {

  private _currentSession: Session = null;
  private currentSessionState$: Observable<Session> = this.store.select(state => state.launcher.session);

  constructor(private store: Store<AppState>) {
    this.currentSessionState$.subscribe(this.currentSessionStateChange.bind(this));
  }

  public currentSessionStateChange(session: Session){
    this._currentSession = session;
    this.store.dispatch(sessionActions.selectSession(this._currentSession));
  }

  get currentSession(): Session {
    return this._currentSession;
  }
}
