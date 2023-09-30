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
import {Session} from "./session";
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
  launcherState$: Observable<string>;
  sessionListState$: Observable<Session[]>;

  constructor(private store: Store<AppState>) {
    this.launcherState$ = this.store.pipe(select(state => state.launcher.sessionId));
    this.sessionListState$ = this.store.pipe(select(state => state.session.sessions));
  }

  ngOnInit(): void {
    this.launcherState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(sessionId => {
      if (sessionId != null) {
        this.store.dispatch({type: 'Create Session', payload: sessionId});
      } else {
        this.store.dispatch(sessionActions.resetSession());
      }
    });
  }

  loadSessionState(session: Session): void {
    // Check and load the entire app state related to this session
    const storedState: AppState = JSON.parse(localStorage.getItem(`appState_${session.id}`));
    if (storedState) {
      this.store.dispatch(sessionActions.activateSession({activeSession: storedState.session.activeSession}));
    }

    // Set this session as active
    this.store.dispatch(sessionActions.activateSession({activeSession: session}));

    // Load related component states based on this session's id
    this.loadRelatedComponentStates(session.id);
  }

  loadRelatedComponentStates(sessionId: string): void {
    console.log('loadRelatedComponentStates', sessionId);
  }

  formatDate(date: string) {
    const parts = date.split(' ')[0].split('-');
    const time = date.split(' ')[1];
    return `${parts[2]}-${parts[1]}-${parts[0]}T${time}`;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
