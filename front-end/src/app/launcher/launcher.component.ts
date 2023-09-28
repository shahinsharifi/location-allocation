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
import {Session} from "../session/session";
import {MatIconModule} from "@angular/material/icon";
import {FlexModule} from "@angular/flex-layout";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatButtonToggle, MatButtonToggleModule} from "@angular/material/button-toggle";
import {Observable, Subject} from "rxjs";
import {select, Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {launcherActions} from "./state/launcher.actions";
import {MatSliderModule} from "@angular/material/slider";
import {RegionSelection} from "../map/region-selection";
import {LauncherService} from "./launcher.service";

@Component({
  selector: 'app-launcher',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressBarModule, MatListModule, MatButtonModule,
    MatButtonModule, MatStepperModule, FormsModule, ReactiveFormsModule, MatFormFieldModule,
    MatInputModule, MatExpansionModule, MatIconModule, FlexModule, MatTooltipModule,
    MatButtonToggleModule, MatSliderModule],
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
  regionSelection: RegionSelection = {};

  constructor(
    private store: Store<AppState>,
    private formBuilder: FormBuilder, private launcherService: LauncherService) {
    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));
    this.mapSelectionState$ = this.store.pipe(select(state => state.map.selection));
  }

  ngOnInit(): void {
    this.initializeForm();
    this.mapSelectionState$.subscribe(selection => {
      if (!selection) return;
      this.regionSelection = {
        ...this.regionSelection,
        active: selection.active,
        wkt: selection.wkt,
        selectedRegions: selection.selectedRegions
      };
      this.regionSelectionFormGroup.setValue({wkt: selection.wkt});
    });
    this.sessionState$.subscribe(session => {
      if (!session || !session.wkt) return;
      this.activeSession = Object.assign({}, session);
      this.regionSelectionFormGroup.setValue({wkt: session.wkt});
      this.parametersFormGroup.setValue({
        numberOfFacilities: session.numberOfFacilities,
        maxTravelTimeInMinutes: session.maxTravelTimeInMinutes
      });
    });
  }

  initializeForm(): void {
    this.regionSelectionFormGroup = this.formBuilder.group({
      wkt: [null, Validators.required]
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
    if(this.sessionId == null) return;
    const session: Session = {id: this.sessionId};
    this.launcherService.stopProcess(session);
  }

  reset() {
    this.stepper.reset();
    this.toggleBtn.checked = false;
    this.regionSelection = null;
    this.regionSelectionFormGroup.reset();
    this.parametersFormGroup.reset();
    this.runningTimeFormGroup.reset();
    this.store.dispatch(launcherActions.resetSession());
  }


  toggleSelection(active: boolean): void {
    if (!this.regionSelection) this.regionSelection = {};
    this.regionSelection.active = active;
    this.store.dispatch(launcherActions.toggleSelection({active: active}));
  }

  clearSelection() {
    this.store.dispatch(launcherActions.clearSelection());
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

}
