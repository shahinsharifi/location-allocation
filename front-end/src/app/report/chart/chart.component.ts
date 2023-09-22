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
import {Observable, Subject} from "rxjs";
import {select, Store} from "@ngrx/store";
import {AppState} from "../../core/state/app.state";
import {MessageSubject} from "../../core/websocket/message";


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
  @Input() chartSubject: MessageSubject;
  @ViewChild('chart', {static: true}) chart: IgxCategoryChartComponent;

  destroy$ = new Subject<void>();
  progressDataStream$: Observable<any>;

  public yAxisExtent: number = undefined;
  public data: any[];

  constructor(private store: Store<AppState>) {
    const data: any[] = [];
    data.push({Label: '0', Value: 0});
    this.data = data;
    this.progressDataStream$ = this.store.pipe(select(state => state.result));
  }

  ngOnInit(): void {
    this.progressDataStream$.subscribe(data => {
      if (data) {
        switch (this.chartSubject) {
          case MessageSubject.SESSION_LOCATION_FITNESS_DATA:
            if (data.location) {
              this.processFitnessData.bind(this)(data.location.fitness);
            }
            break;
          case MessageSubject.SESSION_ALLOCATION_FITNESS_DATA:
            if (data.allocation) {
              this.processFitnessData.bind(this)(data.allocation.fitness);
            }
            break;
          case MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION:
            if (data.allocation) {
              this.processTravelCostDistributionData.bind(this)(data.allocation.travelCostDistribution);
            }
            break;
          default:
            break;
        }
      }
    });
  }


  processFitnessData(data: any[]) {
    if (data == null || data.length === 0) return;

    if(this.yAxisExtent == null)
      this.yAxisExtent = data[data.length - 1].value;

    const newItems = data[data.length - 1];
    const newVal = {
      Label: String(newItems.label),
      Value: newItems.value
    };
    this.data.push(newVal);
    this.chart.notifyInsertItem(this.data, this.data.length - 1, newVal);
    if (this.data.length > 20) {
      const oldVal = this.data[0];
      this.data.shift();
      this.chart.notifyRemoveItem(this.data, 0, oldVal);
    }
  }

  processTravelCostDistributionData(data: any[]) {
    if (data == null || data.length === 0) return;
    this.data = data;
    this.chart.bindData();
  }


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
