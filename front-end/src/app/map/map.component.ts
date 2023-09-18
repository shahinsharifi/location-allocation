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

  drawing$: Observable<boolean>;
  sessionState$: Observable<Session>
  regionSelection$: Observable<object>;
  layerVisibility$: Observable<object>;

  constructor(
    private store: Store<AppState>,
    private mapService: MapService
  ) {
    this.drawing$ = this.store.pipe(select(state => state.map.drawing));
    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));
    this.regionSelection$ = this.store.pipe(select(state => state.map.regionSelection));
    this.layerVisibility$ = this.store.pipe(select(state => state.map.visibility));
  }

  ngOnInit(): void {

this.drawing$
    .pipe(takeUntil(this.destroy$))
    .subscribe(drawing => {
      if (drawing) {
        this.mapService.enableDrawing();
      } else {
        this.mapService.disableDrawing();
      }
    }
    );

    this.sessionState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(session => {
      if (session != null && session.id != null) {
        if (session.status === 'RUNNING' || session.status === 'COMPLETED' || session.status === 'INTERRUPTED') {
          this.mapService.loadResultLayer(session.id).then(() => console.log('Loaded result layer'));
        }
      }
    });


    this.layerVisibility$
    .pipe(takeUntil(this.destroy$))
    .subscribe(visibility => {
      this.mapService.updateLayerVisibility(visibility)
    }
    );


  }

  initializeMap(map: Map): void {
    this.mapService.initializeMap(map, null); // Handle session accordingly
  }



  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
