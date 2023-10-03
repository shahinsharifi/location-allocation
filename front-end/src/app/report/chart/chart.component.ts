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
  IgxLegendModule,
} from 'igniteui-angular-charts';
import {CommonModule} from "@angular/common";
import {FlexModule} from "@angular/flex-layout";
import {MatCardModule} from "@angular/material/card";
import {ChartData} from "./chart-data";
import {Observable, Subject, takeUntil} from "rxjs";


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
	@Input() chartType: string;
	@Input("chartData") chartData$: Observable<ChartData>;
	@ViewChild('chart', {static: true}) chart: IgxCategoryChartComponent;

	public data: any[] = [];
	//private isFirstTime: boolean = true;
	private destroy$ = new Subject<void>();

	constructor() {}

	ngOnInit() {
		this.generateInitData(this.chartType);

    this.chartData$
    .pipe(takeUntil(this.destroy$))
    .subscribe((chartData: ChartData) => {
      if(chartData == null || chartData.data == null || chartData.data.length == 0) {
        this.reset();
      } else {
        this.processData(chartData);
      }
    });
	}

	processData(chartData: ChartData): void {
		if (chartData == null || chartData.data == null || chartData.data.length == 0) return;
		const metadata = chartData.metadata;
		this.chart.yAxisTitle = metadata.yAxisTitle;
		this.chart.xAxisTitle = metadata.xAxisTitle;
		if (this.chartType === 'Spline') {
			this.chart.markerMaxCount = 15;
			this.updateFitnessData(chartData.data);
		} else if (this.chartType === 'Area') {
			this.chart.yAxisMaximumValue = metadata.yMax;
			this.chart.yAxisMinimumValue = metadata.yMin;
			this.updateTravelCostDistributionData(chartData.data);
		}
		this.chart.notifyVisualPropertiesChanged();
	}


	updateFitnessData(data: any[]): void {
		if (data == null) return;
		if(this.data.length == 1 && this.data[0].x == 0 && this.data[0].y == 0) {
      this.data[0] = {x: 0, y: data[0].y};
      //	this.data.push(initVal);
		//	const oldVal = this.data.shift();
		//	this.chart.notifySetItem(this.data, 0, oldVal, initVal);
		}
		const newVal = data[data.length - 1];
		this.data.push(newVal);
	  this.chart.bindData();
    //	this.chart.notifyInsertItem(this.data, this.data.length - 1, newVal);
	}


	updateTravelCostDistributionData(data: any[]): void {
		if (data == null) return;
	//	this.chart.notifyClearItems(this.data);
		for (let i = 0; i < data.length; i++) {
	//		const newItem = data[i];
			const oldItem = this.data[i];
      const newItem = {x: oldItem.x, y: data[i].y};
      this.chart.notifySetItem(this.data, i, oldItem, newItem);
		}
//		this.chart.notifyVisualPropertiesChanged();
	}


	generateInitData(chartType: string): void {
		this.chart.yAxisTitle = 'Y';
		this.chart.xAxisTitle = 'X';
		if (chartType === 'Spline') {
			this.data = [];
			this.data.push({x: 0, y: 0});
		} else if (chartType === 'Area') {
			const data = [];
			for (let i = 0; i <= 60; i += 5) {
				data.push({x: i, y: 0});
			}
			this.data = data;
		}
//		this.isFirstTime = true;
	}


	reset(): void {
		this.data = [];
    this.chart.notifyClearItems(this.data);
		if (this.chartType === 'Spline') {
			this.data.push({x: 0, y: 0});
		}else {
			for (let i = 0; i <= 60; i += 5) {
				this.data.push({x: i, y: 0});
			}
		}
	//	this.isFirstTime = true;
	}

	getChartColor() {
		switch (this.chartType) {
			case 'Spline':
				return 'rgba(140, 231, 217, 1)';
			case 'Area':
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
