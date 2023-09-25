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

@Component({
  selector: 'app-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './session.component.html',
  styleUrls: ['./session.component.scss']
})
export class SessionComponent implements  OnInit, OnDestroy {

  destroy$ = new Subject<void>();
  launcherState$: Observable<string>;

  constructor(
    private store: Store<AppState>
  ) {
    this.launcherState$ = this.store.pipe(select(state => state.launcher.sessionId));
  }

  ngOnInit() {
    this.launcherState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(sessionId => {
      if (sessionId != null) {
        this.store.dispatch({type: 'Create Session', payload: sessionId});
      }else {
        this.store.dispatch(sessionActions.resetSession());
      }
    });
  }


  ngOnDestroy() {
  }

}
