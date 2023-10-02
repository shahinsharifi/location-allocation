import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCardModule} from "@angular/material/card";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatListModule} from "@angular/material/list";
import {MatButtonModule} from "@angular/material/button";
import {
  FormBuilder,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatStepper, MatStepperModule} from "@angular/material/stepper";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatExpansionModule} from "@angular/material/expansion";
import {Session} from "../session/session";
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
      this.regionSelectionFormGroup.patchValue(selection);
    });
  }

  initForms(): void {
    this.regionSelectionFormGroup = this.formBuilder.group({
      activeDrawing: new FormControl(false),
      wkt: new FormControl(null, Validators.required),
      selectedRegions: new FormControl(0)
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
      const session: Session = {
        ...this.regionSelectionFormGroup.value,
        ...this.parametersFormGroup.value,
        ...this.runningTimeFormGroup.value
      };
      this.store.dispatch(launcherActions.startProcess(session));
    } else {
      console.log('Form is not valid')
    }
  }

  stop() {
    if(!this.activeSession || !this.activeSession.id) return;
    this.launcherService.stopProcess(this.activeSession);
  }

  reset() {
    this.initForms();
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
