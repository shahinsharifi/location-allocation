import {Component} from '@angular/core';
import { CommonModule } from '@angular/common';
import {MapComponent} from "../map/map.component";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatListModule} from "@angular/material/list";
import {LauncherComponent} from "../launcher/launcher.component";
import {CoreModule} from "../core/core.module";
import {ChartComponent} from "../report/chart/chart.component";
import {ReportComponent} from "../report/report.component";

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [CommonModule, CoreModule, ReportComponent, MapComponent, LauncherComponent, FlexLayoutModule, MatListModule, ChartComponent],
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent {


}
