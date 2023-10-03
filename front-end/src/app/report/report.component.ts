import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
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
import {filter, Observable, Subscription} from "rxjs";
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
  @ViewChild('chart1', {static: true}) chartComponent1!: ChartComponent;
  @ViewChild('chart2', {static: true}) chartComponent2!: ChartComponent;

  sessionState$! : Observable<Session>;
  allocationFitness$! : Observable<ChartData>;
  travelCostDistribution$! : Observable<ChartData>;

  private subscription1!: Subscription;
  private subscription2!: Subscription;
  private subscription3!: Subscription;

  constructor(private store: Store<AppState>) {

    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));

    this.allocationFitness$ = this.store.pipe(
      select(state => state.report.allocationFitnessChart),
      filter(chart => !!chart)
    );

    this.travelCostDistribution$ = this.store.pipe(
      select(state => state.report.costDistributionChart),
      filter(chart => !!chart)
    );
  }

  ngOnInit(): void {
    this.subscription1 = this.allocationFitness$.subscribe(data => {
      this.chartComponent1.updateData(data);
    });

    this.subscription2 = this.travelCostDistribution$.subscribe(data => {
      this.chartComponent2.updateData(data);
    });

    this.subscription3 = this.sessionState$.subscribe(session => {
      if(session == null) {
      this.chartComponent1.reset();
      this.chartComponent2.reset();
    }});

  }

  ngOnDestroy(): void {
    this.subscription1.unsubscribe();
    this.subscription2.unsubscribe();
    this.subscription3.unsubscribe();
  }
}
