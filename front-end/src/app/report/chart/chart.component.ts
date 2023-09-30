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
	private isFirstTime: boolean = true;
	private destroy$ = new Subject<void>();

	constructor() {}

	ngOnInit() {
		this.generateInitData(this.chartType);

		this.chartData$
		.pipe(takeUntil(this.destroy$))
		.subscribe(this.processData.bind(this));
	}

	processData(chartData: ChartData): void {
		if (chartData == null || chartData.data == null || chartData.data.length == 0) return;
		if (this.chartType === 'Spline') {
			if (this.isFirstTime) {
				const metadata = chartData.metadata;
				this.chart.yAxisTitle = metadata.yAxisTitle;
				this.chart.xAxisTitle = metadata.xAxisTitle;
				this.chart.notifyVisualPropertiesChanged();
				const initVal = {x: 0, y: metadata.yMax};
				this.data.push(initVal);
				const oldVal = this.data.shift();
				this.chart.notifySetItem(this.data, 0, oldVal, initVal);
				this.isFirstTime = false;
			}
			this.updateFitnessData(chartData.data);
		} else if (this.chartType === 'Area') {
			const metadata = chartData.metadata;
			this.chart.yAxisTitle = metadata.yAxisTitle;
			this.chart.xAxisTitle = metadata.xAxisTitle;
			this.chart.yAxisMaximumValue = metadata.yMax;
			this.chart.xAxisInterval = 1;
			this.chart.notifyVisualPropertiesChanged();
			this.updateTravelCostDistributionData(chartData.data);
		}
	}


	updateFitnessData(data: any[]): void {
		if (data == null) return;
		const newVal = data[data.length - 1];
		if (this.data.length == 1 && this.data[0].x == 0 && data.length == 1) {
			this.data.push(newVal);
			const oldVal = this.data.shift();
			this.chart.notifySetItem(this.data, this.data.length - 1, oldVal, newVal);
		} else {
			this.data.push(newVal);
			this.chart.notifyInsertItem(this.data, this.data.length - 1, newVal);
		}
	}


	updateTravelCostDistributionData(data: any[]): void {
		if (data == null) return;
		this.chart.notifyClearItems(this.data);
		for (let i = 0; i < data.length; i++) {
			const newItem = data[i];
			const oldItem = this.data.shift();
			this.chart.notifySetItem(this.data, i, oldItem, newItem);
		}
		this.chart.notifyVisualPropertiesChanged();
	}


	generateInitData(chartType: string): void {
		this.chart.yAxisTitle = 'Y';
		this.chart.xAxisTitle = 'X';
		if (chartType === 'Spline') {
			const data = [];
			for (let i = 0; i <= 20; i += 2) {
				data.push({x: i, y: 0});
			}
			this.data = data;
		} else if (chartType === 'Area') {
			const data = [];
			for (let i = 0; i <= 60; i += 5) {
				data.push({x: i, y: 0});
			}
			this.data = data;
		}
	}


	reset(): void {
		this.data = [];
		for (let i = 0; i <= 60; i += 5) {
			this.data.push({x: i, y: 0});
		}
		this.isFirstTime = true;
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
