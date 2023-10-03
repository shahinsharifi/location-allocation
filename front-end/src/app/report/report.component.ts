import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from "@angular/material/button";
import {MatDividerModule} from "@angular/material/divider";
import {FlexModule} from "@angular/flex-layout";
import {LauncherComponent} from "../launcher/launcher.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {MatCardModule} from "@angular/material/card";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatIconModule} from "@angular/material/icon";
import {MatInputModule} from "@angular/material/input";
import {MatSliderModule} from "@angular/material/slider";
import {MatStepperModule} from "@angular/material/stepper";
import {MatTooltipModule} from "@angular/material/tooltip";
import {ChartComponent} from "./chart/chart.component";
import {filter, Observable, Subject} from "rxjs";
import {Session} from "../session/session";
import {ChartData} from "./chart/chart-data";
import {select, Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";

@Component({
	selector: 'app-report',
	standalone: true,
	imports: [CommonModule, MatButtonModule, MatDividerModule, FlexModule,
		LauncherComponent, FormsModule, MatButtonToggleModule, MatCardModule, MatFormFieldModule,
		MatIconModule, MatInputModule, MatSliderModule, MatStepperModule, MatTooltipModule,
		ReactiveFormsModule, ChartComponent],
	templateUrl: './report.component.html',
	styleUrls: ['./report.component.scss']
})
export class ReportComponent implements OnInit, OnDestroy {

	destroy$ = new Subject<void>();
	sessionState$: Observable<Session>;
	locationFitness$: Observable<ChartData>;
	allocationFitness$: Observable<ChartData>;
	travelCostDistribution$: Observable<ChartData>;

	constructor(private store: Store<AppState>) {

		this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));

		this.locationFitness$ = this.store.pipe(
				select(state => state.report.locationFitnessChart),
				filter(chart => !!chart)
		);

		this.allocationFitness$ = this.store.pipe(
				select(state => state.report.allocationFitnessChart),
				filter(chart => !!chart)
		);

		this.travelCostDistribution$ = this.store.pipe(
				select(state => state.report.costDistributionChart),
				filter(chart => !!chart)
		);
	}

	ngOnInit(): void {}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
	}
}
