import {Component, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {Map, StyleSpecification} from 'maplibre-gl';
import {Observable, Subject, takeUntil} from 'rxjs';
import {NgxMapLibreGLModule} from '@maplibre/ngx-maplibre-gl';
import {MatDialogModule} from '@angular/material/dialog';
import style from '../../assets/style-de-at.json';
import {MapService} from "./map.service";
import {Session, SessionStatus} from "../session/session";
import {select, Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {MatCardModule} from "@angular/material/card";
import {FlexModule} from "@angular/flex-layout";

@Component({
  selector: 'app-map',
  imports: [
    NgxMapLibreGLModule,
    MatDialogModule,
    MatCardModule,
    FlexModule,
  ],
  standalone: true,
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class MapComponent implements OnInit, OnDestroy {

  style = style as StyleSpecification;
  destroy$ = new Subject<void>();
  sessionState$:Observable<Session>;
  launcherDrawingState$: Observable<any>;

  constructor(
    private store: Store<AppState>,
    private mapService: MapService
  ) {
    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));
    this.launcherDrawingState$ = this.store.pipe(select(state => state.launcher.selection));
  }

  ngOnInit(): void {
    this.subscribeToLauncherStateChanges();
    this.subscribeToSessionStateChanges();
  }

  private subscribeToLauncherStateChanges(): void {

    this.launcherDrawingState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(selection => {
      if (selection && selection.active) {
        this.mapService.enableDrawing(selection);
      } else {
        this.mapService.disableDrawing();
      }
    });
  }

  private subscribeToSessionStateChanges(): void {
    this.sessionState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(session => {
      if(!session) return;
      if (session.id) {
        if (['RUNNING', 'COMPLETED', 'INTERRUPTED'].includes(session.status)) {
          this.mapService.loadResultLayer(session.id).then(() => console.log('Loading allocation layer'));
          this.mapService.updateLayerVisibility({
            region: false,
            location: true,
            allocation: true
          });
        }
      }
    });
  }

  initializeMap(map: Map): void {
    console.log(SessionStatus.COMPLETED);
    // const session: Session = {
    //   id: 'fb73fe0b-26fc-4ecf-91a0-2dc4bda2a83c',
    //   status: SessionStatus.COMPLETED
    // };
    // this.mapService.initializeMap(map, session);
    this.mapService.initializeMap(map, null);
  }

  ngOnDestroy() {
    this.mapService.destroyMap(); // Ensure map resources are cleaned up
    this.destroy$.next();
    this.destroy$.complete();
  }
}
