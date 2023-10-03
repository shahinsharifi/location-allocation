import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {select, Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {Observable, Subject, takeUntil} from "rxjs";
import {sessionActions} from "./state/session.actions";
import {Session, SessionStatus} from "./session";
import {MatListModule} from "@angular/material/list";
import {MatCardModule} from "@angular/material/card";

@Component({
  selector: 'app-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatListModule, MatCardModule],
  templateUrl: './session.component.html',
  styleUrls: ['./session.component.scss']
})
export class SessionComponent implements OnInit, OnDestroy {

  destroy$ = new Subject<void>();
  launcherState$: Observable<Session>;
  sessionListState$: Observable<Session[]>;

  constructor(private store: Store<AppState>) {
    this.launcherState$ = this.store.pipe(select(state => state.launcher.activeSession));
    this.sessionListState$ = this.store.pipe(select(state => state.session.sessions));
  }

  ngOnInit(): void {
    this.launcherState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(session => {
      if (session == null) return;
      if (session.status === SessionStatus.START) {
        this.store.dispatch(sessionActions.startSession(session));
      } else if (session.status === SessionStatus.ABORT) {
        this.store.dispatch(sessionActions.stopSession(session));
      }else if (session.status === SessionStatus.RESET) {
        this.store.dispatch(sessionActions.resetActiveSession());
      }
    });
  }

  loadSessionState(session: Session): void {
    // Check and load the entire app state related to this session
    const storedState: AppState = JSON.parse(localStorage.getItem(`appState_${session.id}`));
    if (storedState) {
      this.store.dispatch(sessionActions.activateSession({id: session.id}));
    }
  }

  loadRelatedComponentStates(sessionId: string): void {
    console.log('loadRelatedComponentStates', sessionId);
  }

  formatDate(date: string) {
    if (date) {
      const parts = date.split(' ')[0].split('-');
      const time = date.split(' ')[1];
      return `${parts[2]}-${parts[1]}-${parts[0]}T${time}`;
    }
    return null;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
