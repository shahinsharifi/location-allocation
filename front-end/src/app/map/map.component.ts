import {Component, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {Map, StyleSpecification} from 'maplibre-gl';
import {Observable, Subject, takeUntil} from 'rxjs';
import {NgxMapLibreGLModule} from '@maplibre/ngx-maplibre-gl';
import {MatDialogModule} from '@angular/material/dialog';
import style from '../../assets/style-de-at.json';
import {MapService} from "./map.service";
import {Session} from "../session/session";
import {select, Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {RegionSelection} from "./region-selection";

@Component({
  selector: 'app-map',
  imports: [
    NgxMapLibreGLModule,
    MatDialogModule,
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
  launcherState$:Observable<RegionSelection>;

  constructor(
    private store: Store<AppState>,
    private mapService: MapService
  ) {
    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));
    this.launcherState$ = this.store.pipe(select(state => state.launcher.selection));
  }

  ngOnInit(): void {
    this.launcherState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(selection => {
      if (selection.active) {
        this.mapService.enableDrawing();
      } else {
        this.mapService.disableDrawing();
        if (selection.wkt == null && selection.selectedRegions === 0) {
          this.mapService.clearSelection();
        }
      }
    });

    this.sessionState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(session => {
      if (session != null && session.id != null) {
        if (session.status === 'RUNNING' || session.status === 'COMPLETED' || session.status === 'INTERRUPTED') {
          this.mapService.loadResultLayer(session.id).then(() => console.log('Loaded result layer'));
        }
      }
    });
  }

  initializeMap(map: Map): void {
    this.mapService.initializeMap(map, null);
  }

  ngOnDestroy() {
    this.mapService.destroyMap(); // Ensure map resources are cleaned up
    this.destroy$.next();
    this.destroy$.complete();
  }
}
