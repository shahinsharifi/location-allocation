import {AfterViewInit, Component, OnDestroy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from "@angular/material/button";
import {MatDividerModule} from "@angular/material/divider";
import {ConsoleComponent} from "./console/console.component";
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

@Component({
  selector: 'app-result',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatDividerModule, ConsoleComponent, FlexModule,
    LauncherComponent, FormsModule, MatButtonToggleModule, MatCardModule, MatFormFieldModule,
    MatIconModule, MatInputModule, MatSliderModule, MatStepperModule, MatTooltipModule,
    ReactiveFormsModule, ChartComponent],
  templateUrl: './result.component.html',
  styleUrls: ['./result.component.scss']
})
export class ResultComponent implements AfterViewInit, OnDestroy{

  constructor() {

  }

  ngAfterViewInit(): void {
  }

  ngOnDestroy(): void {
  }
}
