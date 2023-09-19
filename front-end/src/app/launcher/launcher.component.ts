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
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {Observable, Subject} from "rxjs";
import {select, Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {launcherActions} from "./state/launcher.actions";
import {mapActions} from "../map/state/map.actions";
import {MatSliderModule} from "@angular/material/slider";
import {RegionSelection} from "../map/region-selection";

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
  isChecked: boolean = false;
  regionSelectionFormGroup: FormGroup;
  parametersFormGroup: FormGroup;

  destroy$ = new Subject<void>();
  sessionState$:Observable<Session>;
  mapSelectionState$:Observable<RegionSelection>;

  constructor(
    private store: Store<AppState>,
    private formBuilder: FormBuilder) {
    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));
    this.mapSelectionState$ = this.store.pipe(select(state => state.map.selection));
  }

  ngOnInit(): void {
    this.initializeForm();
    this.mapSelectionState$.subscribe(selection => {
      if (!selection) return;
      this.regionSelectionFormGroup.setValue(selection);
    });
    this.sessionState$.subscribe(session => {
      if (!session) return;
      this.regionSelectionFormGroup.setValue(session);
      this.parametersFormGroup.setValue(session);
    });
  }

  private initializeForm(): void {
    this.regionSelectionFormGroup = this.formBuilder.group({spatialQuery: [null, Validators.required]});
    this.parametersFormGroup = this.formBuilder.group({
      numberOfFacilities: [null, Validators.required],
      maxTravelTimeInMinutes: [null, Validators.required]
    });
  }


  start() {
    if (!this.regionSelectionFormGroup.invalid && !this.parametersFormGroup.invalid) {
      const session: Session = {...this.regionSelectionFormGroup.value, ...this.parametersFormGroup.value};
      this.store.dispatch(launcherActions.startProcess(session));
    } else {
      console.log('Form is not valid')
    }
  }

  stop() {

  }

  reset() {
    this.stepper.reset();
    this.regionSelectionFormGroup.reset();
    this.parametersFormGroup.reset();
    this.store.dispatch(mapActions.resetMap());
  }



  toggleSelection(isChecked: boolean): void {
    this.isChecked = isChecked;
    if (isChecked) {
      this.store.dispatch(mapActions.enableSelection());
    } else {
      this.store.dispatch(mapActions.disableSelection());
    }
  }

  clearSelection() {
    this.store.dispatch(mapActions.clearSelection());
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  protected readonly SessionStatus = SessionStatus;
}
