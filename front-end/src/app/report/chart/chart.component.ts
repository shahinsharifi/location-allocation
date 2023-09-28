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
  AutoMarginsAndAngleUpdateMode,
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
import {Session} from "../../session/session";
import {ChartData, ChartMetaData} from "./chart-data";


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
  sessionState$:Observable<Session>;
  locationFitness$: Observable<any>;
  allocationFitness$: Observable<any>;
  travelCostDistribution$: Observable<any>;

  public data: any[] = null;
  public chartType: string = 'Spline';
  public yAxisMaximumValue: number = undefined;

  constructor(private store: Store<AppState>) {
    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));
    this.locationFitness$ = this.store.pipe(select(state => state.report.locationFitnessChart));
    this.allocationFitness$ = this.store.pipe(select(state => state.report.allocationFitnessChart));
    this.travelCostDistribution$ = this.store.pipe(select(state => state.report.costDistributionChart));
  }

  ngOnInit(): void {

    this.sessionState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(session => {
      if (session == null || session.id == null) {
        this.reset();
      }
    });

    this.init();
    if (this.chartSubject === MessageSubject.SESSION_LOCATION_FITNESS_DATA) {
      this.locationFitness$
      .pipe(takeUntil(this.destroy$))
      .subscribe(this.processData.bind(this));
    } else if (this.chartSubject === MessageSubject.SESSION_ALLOCATION_FITNESS_DATA) {
      this.allocationFitness$
      .pipe(takeUntil(this.destroy$))
      .subscribe(this.processData.bind(this));
    } else if (this.chartSubject === MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION) {
      this.travelCostDistribution$
      .pipe(takeUntil(this.destroy$))
      .subscribe(this.processData.bind(this));
    }
  }


  processData(chartData: ChartData): void {
    if (chartData == null) return;
    if(this.data == null) this.init();
    switch (this.chartSubject) {
      case MessageSubject.SESSION_LOCATION_FITNESS_DATA:
      case MessageSubject.SESSION_ALLOCATION_FITNESS_DATA:
        this.processFitnessData(chartData.metadata, chartData.data);
        break;
      case MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION:
        this.processTravelCostDistributionData(chartData.metadata, chartData.data);
        break;
      default:
        break;
    }
  }

  processFitnessData(metaData: ChartMetaData, data: any[]): void {
    if (data && data.length > 0) {
      this.updateFitnessData(metaData, data);
    }
  }

  updateFitnessData(metaData: ChartMetaData, data: any[]): void {
    if (this.yAxisMaximumValue == null && metaData.yMax == null) {
      this.yAxisMaximumValue = 100;
    } else {
      // this.chart.yAxisMaximumValue = metaData.yMax;
      this.chart.yAxisTitle = metaData.yAxisTitle;
      this.chart.xAxisTitle = metaData.xAxisTitle;
      this.yAxisMaximumValue = Math.abs(metaData.yMax * 1.1);
      this.chart.yAxisMinimumValue = Math.abs(metaData.yMax * 0.95);
      //    this.chart.shouldConsiderAutoRotationForInitialLabels = true;
//      this.chart.isHorizontalZoomEnabled = true;
      this.chart.shouldAutoExpandMarginForInitialLabels = true;
      this.chart.xAxisEnhancedIntervalPreferMoreCategoryLabels = true;
      this.chart.autoMarginAndAngleUpdateMode = AutoMarginsAndAngleUpdateMode.SizeChangingAndZoom;
      this.chart.notifyVisualPropertiesChanged();
    }

    // if (data != null && data.length == 1) {
    //   this.chart.notifyClearItems(this.data);
    // }
    const newVal = data[data.length - 1];
    this.chart.notifyInsertItem(this.data, this.data.length - 1, newVal);
    if (data.length == 1) {
      const oldVal = this.data.shift();
      this.chart.notifyRemoveItem(this.data, 0, oldVal);
    }

  }

  processTravelCostDistributionData(metaData: ChartMetaData, data: any[]): void {
    if(this.yAxisMaximumValue == null || metaData.yMax == null) {
      this.yAxisMaximumValue = 1000;
    }else{
    //  this.chart.yAxisMaximumValue = metaData.yMax ;
      this.chart.yAxisTitle = metaData.yAxisTitle;
      this.chart.xAxisTitle = metaData.xAxisTitle;
      this.chart.notifyVisualPropertiesChanged();
    }

    if (data && data.length > 0) {

      this.chart.notifyClearItems(this.data);

      const dataItem = data[data.length-1];
      for(let i = 0; i < dataItem.length; i++){
        const newItem = dataItem[i];
       // this.data.push(newItem);
        const oldItem = this.data.shift();
        this.chart.notifySetItem(this.data, i, oldItem, newItem);
      }
      this.chart.notifyVisualPropertiesChanged();
    }
  }

  init(): void {
    if (this.chartSubject === MessageSubject.SESSION_LOCATION_FITNESS_DATA ||
        this.chartSubject === MessageSubject.SESSION_ALLOCATION_FITNESS_DATA) {
      this.chartType = 'Spline';
      this.yAxisMaximumValue = 100;
      this.data = [{x: 0, y: 0}];
    } else if (this.chartSubject === MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION) {
      this.data = [];
      this.chartType = 'Area';
      this.yAxisMaximumValue = 1000;
      for (let i = 0; i <= 60; i += 5) {
        this.data.push({x: i, y: 0});
      }
    }
  }

  reset(): void {
    this.init();
  }

  getChartColor() {
    switch (this.chartSubject) {
      case MessageSubject.SESSION_LOCATION_FITNESS_DATA:
      case MessageSubject.SESSION_ALLOCATION_FITNESS_DATA:
        return 'rgba(140, 231, 217, 1)';
      case MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION:
        return 'rgba(110, 177, 255, 1)';
      default:
        return 'rgba(238, 88, 121, 1)';
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
