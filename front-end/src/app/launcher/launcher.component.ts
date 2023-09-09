import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCardModule} from "@angular/material/card";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatListModule} from "@angular/material/list";
import {MatButtonModule} from "@angular/material/button";
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {MatStepperModule} from "@angular/material/stepper";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatExpansionModule} from "@angular/material/expansion";
import {Store} from "@ngrx/store";
import {AppState} from "../core/state/app.state";
import {launcherActions} from "./state/launcher.actions";
import {Session} from "../session/session";


@Component({
  selector: 'app-input',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressBarModule, MatListModule, MatButtonModule, MatButtonModule,
    MatStepperModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule, MatExpansionModule],
  templateUrl: './launcher.component.html',
  styleUrls: ['./launcher.component.scss']
})
export class LauncherComponent implements OnInit, OnDestroy {

  my_dummy_sessions: Session[] = [
    {id: 'a06861b9-51db-4dcf-aab2-302764a51b52'},
    {id: 'f3737e00-f6b1-4e8f-b3e7-7dd96fc66de4'},
    {id: 'ee4a0379-b9c7-473b-bfe7-72f31dbb83bd'}
  ];

  my_flag: number = 0;

  firstFormGroup = this._formBuilder.group({
    facilityNumber: ['', [Validators.required, Validators.pattern("^[0-9]*$")]]
  });

  secondFormGroup = this._formBuilder.group({
    secondCtrl: '',
  });

  isOptional = false;

  sessionId: string | null = null;

  @Output() sessionEvent = new EventEmitter<string>();

  constructor(private store: Store<AppState>, private _formBuilder: FormBuilder) {
  }

  ngOnInit() {

  }

  start() {
      const parameters: Session = {
        numberOfFacilities: Number(this.firstFormGroup.controls['facilityNumber'].value),
        maxTravelTimeInMinutes: 30
      };
    this.store.dispatch(launcherActions.run(parameters));
  }

  stop() {
    if (this.my_flag == 0) {
      this.store.dispatch(launcherActions.runSuccess({session: this.my_dummy_sessions[this.my_flag]}));
      this.my_flag = 1;
    } else {
      this.store.dispatch(launcherActions.runSuccess({session: this.my_dummy_sessions[this.my_flag]}));
      this.my_flag = 0;
    }
  }

  ngOnDestroy() {
  }

}
