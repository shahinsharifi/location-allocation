import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnDestroy,
  OnInit, ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {
  IgxCategoryChartComponent,
  IgxCategoryChartModule,
  IgxLegendModule
} from 'igniteui-angular-charts';
import {CommonModule} from "@angular/common";
import {OptimizationProgress} from './optimization-progress';
import {FlexModule} from "@angular/flex-layout";
import {MatCardModule} from "@angular/material/card";
import {Observable, Subject} from "rxjs";
import {AppState} from "../../core/state/app.state";
import {select, Store} from "@ngrx/store";

@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [CommonModule, IgxCategoryChartModule, IgxLegendModule, FlexModule, MatCardModule],
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None
})
export class ChartComponent implements OnInit, OnDestroy {

  @Input()
  includedProperties: string;

  @ViewChild('chart', {static: true})
  public chart: IgxCategoryChartComponent;

  _optimizationProgress: OptimizationProgress = null;

  destroy$ = new Subject<void>();
  progressDataStream$: Observable<any>;

  constructor(private store: Store<AppState>) {
    this._optimizationProgress = new OptimizationProgress();
    this.progressDataStream$ = this.store.pipe(select(state => state.result.progress));
  }

  ngOnInit(): void {
    this.progressDataStream$.subscribe(progress => {
      if (this._optimizationProgress != null) {
        this._optimizationProgress.updateData({generation: progress.generation, value: progress.value});
        if(this.optimizationProgress.length > 0) {
          this.chart.notifyInsertItem(this.optimizationProgress, this._optimizationProgress.length - 1, progress);
        }
      }
    });
  }

  public get optimizationProgress(): OptimizationProgress {
    return this._optimizationProgress;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
