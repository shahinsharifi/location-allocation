import {CommonModule} from '@angular/common';
import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
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
import {MatSidenav, MatSidenavModule} from '@angular/material/sidenav';
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
import {AppState} from "./core/state/app.state";
import {Store} from "@ngrx/store";
import {SessionService} from "./session/session.service";


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
  @ViewChild('sidenav', {static: true}) sidenav!: MatSidenav;
  constructor(private store: Store<AppState>, private sessionService: SessionService) {}

  ngOnInit(): void {
 //  this.loadSessionsFromLocalStorage();
  }

  loadSessionsFromLocalStorage(): void {
    if(this.sidenav.opened){
      this.sidenav.toggle(false).then(() => console.log('sidenav closed ...'));
      return;
    }
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
        if (session) {
          appState.session.activeSession = session;
          localStorage.setItem(key, JSON.stringify(appState));
        } else {
          localStorage.removeItem(key);
        }
      });
      this.store.dispatch(sessionActions.loadStoredSessions(null));
      this.store.dispatch(sessionActions.loadStoredSessions({sessions}));
      this.sidenav.toggle(true).then(() => console.log('sidenav opened ...'));
    });
  }

  onSidenavChange() {
    console.log('sidenav changed ...');
  }

  ngOnDestroy(): void {

  }
}
