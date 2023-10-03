import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCardModule} from "@angular/material/card";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatListModule} from "@angular/material/list";
import {MatButtonModule} from "@angular/material/button";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatStepper, MatStepperModule} from "@angular/material/stepper";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatExpansionModule} from "@angular/material/expansion";
import {Session, SessionStatus} from "../session/session";
import {MatIconModule} from "@angular/material/icon";
import {FlexModule} from "@angular/flex-layout";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatButtonToggle, MatButtonToggleModule} from "@angular/material/button-toggle";
import {Observable, Subject, takeUntil} from "rxjs";
import {select, Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {launcherActions} from "./state/launcher.actions";
import {MatSliderModule} from "@angular/material/slider";
import {RegionSelection} from "../map/region-selection";
import {LauncherService} from "./launcher.service";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";

@Component({
  selector: 'app-launcher',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressBarModule, MatListModule, MatButtonModule,
    MatButtonModule, MatStepperModule, FormsModule, ReactiveFormsModule, MatFormFieldModule,
    MatInputModule, MatExpansionModule, MatIconModule, FlexModule, MatTooltipModule,
    MatButtonToggleModule, MatSliderModule, MatSlideToggleModule],
  templateUrl: './launcher.component.html',
  styleUrls: ['./launcher.component.scss'],
})
export class LauncherComponent implements OnInit, OnDestroy {

  @ViewChild('stepper') stepper: MatStepper;
  @ViewChild('toggleBtn') toggleBtn: MatButtonToggle;

  sessionId: string = null;
  activeSession: Session = null;
  regionSelectionFormGroup: FormGroup;
  parametersFormGroup: FormGroup;
  runningTimeFormGroup: FormGroup;
  destroy$ = new Subject<void>();
  sessionState$: Observable<Session>;
  mapSelectionState$: Observable<RegionSelection>;

  constructor(
    private store: Store<AppState>,
    private formBuilder: FormBuilder, private launcherService: LauncherService) {
    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));
    this.mapSelectionState$ = this.store.pipe(select(state => state.map.selection));
  }

  ngOnInit(): void {
    this.initForms();
    this.mapSelectionState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(selection => {
      if (!selection) return;
      this.regionSelectionFormGroup.patchValue({
        wkt: selection.wkt,
        selectedRegions: selection.selectedRegions
      });
    });

    this.sessionState$
    .pipe(takeUntil(this.destroy$))
    .subscribe(session => {
        if (session && session.id) this.initSession(session);
      }
    );
  }

  initSession(session: Session) {
    this.activeSession = session;
  }

  initForms(): void {
    this.regionSelectionFormGroup = this.formBuilder.group({
      activeDrawing: [],
      wkt: [null, Validators.required],
      selectedRegions: []
    });
    this.parametersFormGroup = this.formBuilder.group({
      numberOfFacilities: [30, Validators.required],
      maxTravelTimeInMinutes: [25, Validators.required]
    });
    this.runningTimeFormGroup = this.formBuilder.group({
      maxRunningTimeInMinutes: [30, Validators.required]
    });
  }


  start() {
    if (!this.regionSelectionFormGroup.invalid && !this.parametersFormGroup.invalid) {
      this.activeSession = {
        ...this.regionSelectionFormGroup.value,
        ...this.parametersFormGroup.value,
        ...this.runningTimeFormGroup.value,
        status: SessionStatus.START
      };
      this.activeSession.status = SessionStatus.START;
      this.store.dispatch(launcherActions.startProcess(this.activeSession));
    } else {
      console.log('Form is not valid')
    }
  }

  stop() {
    this.activeSession.status = SessionStatus.ABORT;
    this.launcherService.stopSession(this.activeSession);
  }

  reset() {
    this.stepper.reset();
    this.store.dispatch(launcherActions.resetSession());
  }


  toggleSelection(activeDrawing: boolean): void {
    this.store.dispatch(launcherActions.activateDrawing({activeDrawing: activeDrawing}));
  }

  clearSelection() {
    this.store.dispatch(launcherActions.clearSelection());
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

}
