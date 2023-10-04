import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
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
import {SessionService} from "./session.service";
import {FlexModule} from "@angular/flex-layout";

@Component({
  selector: 'app-session-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatListModule, MatCardModule, FlexModule],
  templateUrl: './session.component.html',
  styleUrls: ['./session.component.scss']
})
export class SessionComponent implements OnInit, OnDestroy {

  destroy$ = new Subject<void>();
  launcherState$: Observable<Session>;
  sessionListState$: Observable<Session[]>;

  @Output() sessionSelected = new EventEmitter<string>();

  constructor(private store: Store<AppState>, private sessionService: SessionService) {
    this.launcherState$ = this.store.pipe(select(state => state.launcher.activeSession));
    this.sessionListState$ = this.store.pipe(select(state => state.session.sessions));
  }

  ngOnInit(): void {

    this.loadSessionsFromLocalStorage();

    this.launcherState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(session => {
      if (session == null) return;
      if (session.status === SessionStatus.START) {
        const input: Session = {
          wkt: session.wkt,
          numberOfFacilities: session.numberOfFacilities,
          maxTravelTimeInMinutes: session.maxTravelTimeInMinutes,
          maxRunningTimeInMinutes: session.maxRunningTimeInMinutes
        };
        this.sessionService.startSession(input);
      } else if (session.status === SessionStatus.ABORT) {
        const input: Session = {
          id: session.id,
          status: session.status
        };
        this.sessionService.stopSession(input);
      } else if (session.status === SessionStatus.RESET) {
        this.store.dispatch(sessionActions.resetActiveSession());
      }
    });
  }

  loadSessionsFromLocalStorage(): void {
    const keys = Object.keys(localStorage);
    const sessionKeys = keys.filter(key => key.startsWith('appState_'));
    this.sessionService.loadSessions(sessionKeys).subscribe(sessions => {
      sessionKeys.map(key => {
        const appState: AppState = JSON.parse(localStorage.getItem(key));
        // Check if the session exists in sessions
        const session = sessions.find((session: {
          id: string;
        }) => session.id === appState.session.activeSession.id);
        // If not found, delete it
        if (!session) {
          localStorage.removeItem(key);
        }
      });
      this.store.dispatch(sessionActions.loadStoredSessions({sessions}));
    });
  }


  activateSession(sessionId: string): void {
    // Check and load the entire app state related to this session
    const storedState: AppState = JSON.parse(localStorage.getItem(`appState_${sessionId}`));
    if (storedState) {
      this.store.dispatch(sessionActions.activateSession({id: sessionId}));
      this.sessionSelected.emit(sessionId);
    }
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
