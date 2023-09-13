import {Component} from '@angular/core';
import { CommonModule } from '@angular/common';
import {MapComponent} from "../map/map.component";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatListModule} from "@angular/material/list";
import {ResultComponent} from "../result/result.component";
import {LauncherComponent} from "../launcher/launcher.component";
import {ChartComponent} from "../result/chart/chart.component";
import {CoreModule} from "../core/core.module";

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [CommonModule, CoreModule, ResultComponent, MapComponent, LauncherComponent, FlexLayoutModule, MatListModule, ChartComponent],
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent {


}
