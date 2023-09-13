import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { StyleSpecification } from 'maplibre-gl';
import {Observable, Subject, takeUntil} from 'rxjs';
import { NgxMapLibreGLModule } from '@maplibre/ngx-maplibre-gl';
import { MatDialogModule } from '@angular/material/dialog';
import style from '../../assets/style-de-at.json';
import { Session } from '../session/session';
import { Store, select } from '@ngrx/store';
import { AppState } from '../core/state/app.state';
import {MapService} from "./map.service";
import { Map } from 'maplibre-gl';

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
  private destroy$ = new Subject<void>();

  sessionState$: Observable<Session>
  selectionActive$: Observable<boolean>;

  constructor(
    private store: Store<AppState>,
    private mapService: MapService
  ) {
    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));
    this.selectionActive$ = this.store.pipe(select(state => state.map.selectionActive));
  }

  ngOnInit(): void {
    this.sessionState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(session => {
      this.mapService.sessionStateChange(session);
    });

    this.selectionActive$
    .pipe(takeUntil(this.destroy$))
    .subscribe(active => {
      if (active) {
        this.mapService.activateDrawing();
      } else {
        this.mapService.deActivateDrawing();
      }
    });
  }

  initializeMap(map: Map): void {
    this.mapService.initializeMap(map, null); // Handle session accordingly
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
