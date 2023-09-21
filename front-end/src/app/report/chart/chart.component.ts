import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnDestroy,
  OnInit,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {
  IgxCategoryChartComponent,
  IgxCategoryChartModule,
  IgxLegendModule
} from 'igniteui-angular-charts';
import {CommonModule} from "@angular/common";
import {FlexModule} from "@angular/flex-layout";
import {MatCardModule} from "@angular/material/card";
import {OptimizationProgress} from "./optimization-progress";
import {Observable, Subject} from "rxjs";
import {select, Store} from "@ngrx/store";
import {AppState} from "../../core/state/app.state";


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

  @Input() chartTitle: string;
  @Input() xAxisTitle: string;
  @Input() yAxisTitle: string;
  @ViewChild('chart', {static: true}) chart: IgxCategoryChartComponent;

  _optimizationProgress: OptimizationProgress = null;

  destroy$ = new Subject<void>();
  progressDataStream$: Observable<any>;

  public data: any[];

  constructor(private store: Store<AppState>) {
    const data: any[] = [];
    data.push({ Label: '0', Value: 0 });
    this.data = data;
    this._optimizationProgress = new OptimizationProgress();
    this.progressDataStream$ = this.store.pipe(select(state => state.result.progress));
  }

  ngOnInit(): void {
    this.progressDataStream$.subscribe(progress => {
      if (progress == null || progress.length === 0) return;

      const newItems = progress[progress.length - 1];
      const newVal = {
        Label: String(newItems.generation.toString()),
        Value: newItems.value
      };
      this.data.push(newVal);
      this.chart.notifyInsertItem(this.data, this.data.length - 1, newVal);
      if (this.data.length > 20) {
        const oldVal = this.data[0];
        this.data.shift();
        this.chart.notifyRemoveItem(this.data, 0, oldVal);
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
