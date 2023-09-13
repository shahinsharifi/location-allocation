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
import {Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {Session} from "../session/session";
import {MatIconModule} from "@angular/material/icon";
import {FlexModule} from "@angular/flex-layout";
import {MatTooltipModule} from "@angular/material/tooltip";
import {mapActions} from "../map/state/map.actions";
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {Observable, Subscription} from "rxjs";
import {launcherActions} from "./state/launcher.actions";



@Component({
  selector: 'app-input',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressBarModule, MatListModule, MatButtonModule, MatButtonModule,
    MatStepperModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule, MatExpansionModule, MatIconModule, FlexModule, MatTooltipModule, MatButtonToggleModule],
  templateUrl: './launcher.component.html',
  styleUrls: ['./launcher.component.scss']
})
export class LauncherComponent implements OnInit, OnDestroy {
  @ViewChild('stepper') private myStepper: MatStepper;
  firstFormGroup: FormGroup;
  secondFormGroup: FormGroup;
  sessionState$: Observable<Session>;
  spatialQuery$: Observable<string>; // polygon
  numSelectedRegions$: Observable<number>;

  constructor(private _formBuilder: FormBuilder, private store: Store<AppState>) {
    this.sessionState$ = this.store.select(state => state.session.activeSession);
    this.spatialQuery$ = this.store.select(state => state.map.spatialQuery);
    this.numSelectedRegions$ = this.store.select(state => state.map.numSelectedRegions);
  }

  private subs: Subscription[] = [];  // to hold all active subscriptions

  ngOnInit() {
    this.subs.push(
        this.sessionState$.subscribe(session => {
          if (!session) return;
          this.firstFormGroup.setValue({spatialQuery: session.spatialQuery || []});
          this.secondFormGroup.setValue({
            numberOfFacilities: session.numberOfFacilities || '',
            maxTravelTimeInMinutes: session.maxTravelTimeInMinutes || ''
          });
        })
    );

    this.firstFormGroup = this._formBuilder.group({
      spatialQuery: [[], Validators.required]
    });

    this.secondFormGroup = this._formBuilder.group({
      numberOfFacilities: ['65', Validators.required],
      maxTravelTimeInMinutes: ['30', Validators.required]
    });

    this.subs.push(
        this.spatialQuery$.subscribe(coordinates => {
          this.firstFormGroup.controls['spatialQuery'].setValue(coordinates);
        })
    );
  }

  startCalculation() {
    if(!this.firstFormGroup.invalid && !this.secondFormGroup.invalid){
      const session: Session = {...this.firstFormGroup.value, ...this.secondFormGroup.value};
      this.store.dispatch(launcherActions.run(session));
    } else {
      console.log('Form is not valid')
    }
  }

  resetForm(){
    this.myStepper.reset();
    this.firstFormGroup.reset();
    this.secondFormGroup.reset();
   // this.store.dispatch(resetSession()); // Reset the state
  }

  activateSelectByPolygon(){
    this.store.dispatch(mapActions.activatePolygonDrawing());
  }

  clearSelection(){
    this.store.dispatch(mapActions.clearSelection());
  }

  ngOnDestroy() {
    this.subs.forEach(sub => sub.unsubscribe());
  }

}
