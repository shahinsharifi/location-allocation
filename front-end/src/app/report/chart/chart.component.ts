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
	MarkerAutomaticBehavior
} from 'igniteui-angular-charts';
import {CommonModule} from "@angular/common";
import {FlexModule} from "@angular/flex-layout";
import {MatCardModule} from "@angular/material/card";
import {MessageSubject} from "../../core/websocket/message";
import {Observable, Subject, takeUntil} from "rxjs";
import {Session} from "../../session/session";
import {select, Store} from "@ngrx/store";
import {AppState} from "../../core/state/app.state";
import {ChartData} from "./chart-data";


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
	sessionState$: Observable<Session>;
	locationFitness$: Observable<any>;
	allocationFitness$: Observable<any>;
	travelCostDistribution$: Observable<any>;

	public data: any[] = [];
	public chartType: string = 'Spline';
	private isFirstTime: boolean = true;

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

		if (this.chartSubject === MessageSubject.SESSION_LOCATION_FITNESS_DATA) {
			this.chartType = 'Spline';
			this.locationFitness$
			.pipe(takeUntil(this.destroy$))
			.subscribe(this.processData.bind(this));
		} else if (this.chartSubject === MessageSubject.SESSION_ALLOCATION_FITNESS_DATA) {
			this.data = [{x: 0, y: 0}];
			this.chartType = 'Spline';
			this.allocationFitness$
			.pipe(takeUntil(this.destroy$))
			.subscribe(this.processData.bind(this));
		} else if (this.chartSubject === MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION) {
			this.data = [];
			for (let i = 0; i <= 60; i += 5) {
				this.data.push({x: i, y: 0});
			}
			this.chartType = 'Area';
			this.travelCostDistribution$
			.pipe(takeUntil(this.destroy$))
			.subscribe(this.processData.bind(this));
		}
	}


	processData(chartData: ChartData): void {
		if (chartData == null || chartData.data == null || chartData.data.length == 0) return;
		if (this.chartSubject === MessageSubject.SESSION_LOCATION_FITNESS_DATA ||
				this.chartSubject === MessageSubject.SESSION_ALLOCATION_FITNESS_DATA) {
			if (this.isFirstTime) {
				const metadata = chartData.metadata;
				this.chart.yAxisTitle = metadata.yAxisTitle;
				this.chart.xAxisTitle = metadata.xAxisTitle;
				this.chart.markerAutomaticBehavior = MarkerAutomaticBehavior.CircleSmart;
				this.chart.markerMaxCount = 10;
				this.chart.notifyVisualPropertiesChanged();
				const initVal = {x: 0, y: metadata.yMax};
				this.data.push(initVal);
				const oldVal = this.data.shift();
				this.chart.notifySetItem(this.data, 0, oldVal, initVal);
				this.isFirstTime = false;
			}
			this.updateFitnessData(chartData.data);
		} else if (this.chartSubject === MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION) {
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
		if(data == null) return;
		this.chart.notifyClearItems(this.data);
		for (let i = 0; i < data.length; i++) {
			const newItem = data[i];
			const oldItem = this.data.shift();
			this.chart.notifySetItem(this.data, i, oldItem, newItem);
		}
		this.chart.notifyVisualPropertiesChanged();
	}


	reset(): void {
		this.isFirstTime = true;
		this.data = [];
		this.chart.notifyClearItems(this.data);
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
