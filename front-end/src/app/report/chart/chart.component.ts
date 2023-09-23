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
import {Observable, Subject, takeUntil} from "rxjs";
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
  locationFitness$: Observable<any>;
  allocationFitness$: Observable<any>;
  travelCostDistribution$: Observable<any>;


  public yAxisMaximumValue: number = undefined;
  public data: any[] = null;


  constructor(private store: Store<AppState>) {
    this.locationFitness$ = this.store.pipe(select(state => state.report.locationFitness));
    this.allocationFitness$ = this.store.pipe(select(state => state.report.allocationFitness));
    this.travelCostDistribution$ = this.store.pipe(select(state => state.report.travelCostDistribution));
  }

  ngOnInit(): void {

    if (this.chartSubject === MessageSubject.SESSION_LOCATION_FITNESS_DATA) {
      this.data = [{generation: 0, value: 0}];
      this.locationFitness$
      .pipe(takeUntil(this.destroy$))
      .subscribe(this.processData.bind(this));

    } else if (this.chartSubject === MessageSubject.SESSION_ALLOCATION_FITNESS_DATA) {
      this.data = [{generation: 0, value: 0}];
      this.allocationFitness$
      .pipe(takeUntil(this.destroy$))
      .subscribe(this.processData.bind(this));

    } else if (this.chartSubject === MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION) {
      this.data = [];
      for (let i = 0; i <= 60; i += 5) {
        this.data.push({label: i.toString(), value: 0});
      }
      this.travelCostDistribution$
      .pipe(takeUntil(this.destroy$))
      .subscribe(this.processData.bind(this));

    }
  }


  processData(data: any): void {
    if (data == null) return;
    switch (this.chartSubject) {
      case MessageSubject.SESSION_LOCATION_FITNESS_DATA:
      case MessageSubject.SESSION_ALLOCATION_FITNESS_DATA:
        this.processFitnessData(data);
        break;
      case MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION:
        this.processTravelCostDistributionData(data);
        break;
      default:
        break;
    }
  }

  processFitnessData(data: any[]): void {
    if (data && data.length > 0) {
      this.updateFitnessData(data);
    }
  }

  updateFitnessData(data: any[]): void {
    if(this.yAxisMaximumValue == null) {
      this.yAxisMaximumValue = data[0].value;
    }
    const newVal = data[data.length-1];
    this.data.push(newVal);
    this.chart.notifyInsertItem(this.data, this.data.length - 1, newVal);
    if (this.data.length > 10) {
      const oldVal = this.data.shift();
      this.chart.notifyRemoveItem(this.data, 0, oldVal);
    }
  }

  processTravelCostDistributionData(data: any[]): void {
    if(this.yAxisMaximumValue == null) {
      this.yAxisMaximumValue = 1000;
    }
    if (data && data.length > 0) {
      const dataItem = data[data.length-1];
      for(let i = 0; i < dataItem.length; i++){
        const newItem = dataItem[i];
        this.data.push(newItem);
        const oldItem = this.data.shift();
        this.chart.notifySetItem(this.data, i, oldItem, newItem);
      }
    }
  }


  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
