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
import {RegionSelection} from "./region-selection";
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
        if (selection.wkt != null || selection.selectedRegions === 0) {
          this.mapService.clearSelection();
        }
      }
    });

    this.sessionState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(session => {
      console.log('Here');
      if (session != null && session.id != null) {
        if (session.status === 'RUNNING' || session.status === 'COMPLETED' || session.status === 'INTERRUPTED') {
          this.mapService.updateLayerVisibility({
            region: false,
            location: true,
            allocation: true
          })
          this.mapService.loadResultLayer(session.id).then(() => console.log('Loading allocation layer'));
        }else if(session.status === 'INIT'){
          this.mapService.resetMap();
        }
      }
    });
  }

  initializeMap(map: Map): void {
    console.log(SessionStatus.COMPLETED);
    // const session: Session = {
    //   id: '15ddeffc-1a99-455c-a829-5c345736ea2c',
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
