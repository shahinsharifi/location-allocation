import {CommonModule} from '@angular/common';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatRadioModule} from '@angular/material/radio';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {RouterModule} from '@angular/router';
import {NgxMapLibreGLModule} from "@maplibre/ngx-maplibre-gl";
import {MapComponent} from "./map/map.component";

import {MatToolbarModule} from "@angular/material/toolbar";
import {MainComponent} from "./main/main.component";
import {SessionComponent} from "./session/session.component";
import {LauncherComponent} from "./launcher/launcher.component";
import {ReportComponent} from "./report/report.component";
import {sessionActions} from "./session/state/session.actions";
import {Session} from "./session/session";
import {AppState} from "./core/state/app.state";
import {Store} from "@ngrx/store";


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule,
    CommonModule,
    FormsModule,
    NgxMapLibreGLModule,
    MatRadioModule,
    MatButtonToggleModule,
    MatButtonModule,
    MatListModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatInputModule,
    MatIconModule,
    MatSidenavModule,
    MatPaginatorModule,
    MatSlideToggleModule,
    MapComponent, MainComponent, SessionComponent, ReportComponent, LauncherComponent, MatToolbarModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],

})
export class AppComponent implements OnInit, OnDestroy {

  sidenavIsOpen = false;

  constructor(private store: Store<AppState>) {}

  ngOnInit(): void {
   this.loadSessionsFromLocalStorage();
  }

  loadSessionsFromLocalStorage(): void {
    const keys = Object.keys(localStorage);
    const sessionKeys = keys.filter(key => key.startsWith('appState_'));
    const sessions: Session[] = sessionKeys.map(key => {
      const appState: AppState = JSON.parse(localStorage.getItem(key));
      return appState.session.activeSession;
    });
    this.store.dispatch(sessionActions.loadStoredSessions({ sessions }));
  }

  onSidenavChange() {
    console.log('sidenav changed ...');
  }

  ngOnDestroy(): void {

  }
}
