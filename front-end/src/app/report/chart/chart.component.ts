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
  progressDataStream$: Observable<any>;

  public yAxisExtent: number = undefined;
  public data: any[] = null;
  public subjectValue: string;
  public subjectProperty: string;

  constructor(private store: Store<AppState>) {
      this.data = [{ Label: '0', Value: 0 }];
      this.setSubjectValues();
      this.progressDataStream$ = this.store.pipe(select(state => state.result[this.subjectValue][this.subjectProperty]));
  }

  ngOnInit(): void {
    this.progressDataStream$.pipe(takeUntil(this.destroy$)).subscribe(this.processData.bind(this));
  }

  setSubjectValues(): void {
    switch (this.chartSubject) {
      case MessageSubject.SESSION_LOCATION_FITNESS_DATA:
        this.subjectValue = 'location';
        this.subjectProperty = 'fitness';
        break;
      case MessageSubject.SESSION_ALLOCATION_FITNESS_DATA:
        this.subjectValue = 'allocation';
        this.subjectProperty = 'fitness';
        break;
      default:
        this.subjectValue = 'allocation';
        this.subjectProperty = 'travelCostDistribution';
        break;
    }
  }

  processData(data: any): void {
    if (data == null) return;

    switch (this.chartSubject) {
      case MessageSubject.SESSION_LOCATION_FITNESS_DATA:
        console.log('processData', data.location);
        this.processFitnessData(data.location.fitness);
        break;
      case MessageSubject.SESSION_ALLOCATION_FITNESS_DATA:
        console.log('allocation.fitness', data.allocation);
        this.processFitnessData(data.allocation.fitness);
        break;
      case MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION:
        console.log('travelCostDistribution', data.allocation);
        this.processTravelCostDistributionData(data.allocation.travelCostDistribution);
        break;
      default:
        break;
    }
  }

  processFitnessData(data: any[]): void {
    console.log('processFitnessData', data);
    if (data && data.length > 0) {
      this.updateFitnessData(data);
    }
  }

  updateFitnessData(data: any[]): void {
    console.log('updateFitnessData', data);
    this.yAxisExtent = this.yAxisExtent || data[0].value;
    const newVal = { Label: String(data[data.length-1].label), Value: data[data.length-1].value };
    this.data.push(newVal);
    this.chart.notifyInsertItem(this.data, this.data.length - 1, newVal);
    if (this.data.length > 20) {
      const oldVal = this.data.shift();
      this.chart.notifyRemoveItem(this.data, 0, oldVal);
    }
  }

  processTravelCostDistributionData(data: any[]): void {
    if (data && data.length > 0) {
      const newVal: any[] = data[data.length-1];
      this.data = [];
      this.data.push(newVal);
      this.chart.bindData();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
