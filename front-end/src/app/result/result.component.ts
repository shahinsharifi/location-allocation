import {AfterViewInit, Component, EventEmitter, OnDestroy, Output, ViewChild} from '@angular/core';
import { CommonModule } from '@angular/common';
import {MatButtonModule} from "@angular/material/button";
import {TimelineChartComponent} from "./timeline-chart/timeline-chart.component";
import {MatDividerModule} from "@angular/material/divider";
import {ConsoleComponent} from "./console/console.component";
import {FlexModule} from "@angular/flex-layout";
import {LauncherComponent} from "../launcher/launcher.component";
import {RemoteDataService} from "../core/http/data.service";
import {forkJoin} from "rxjs";

@Component({
  selector: 'app-result',
  standalone: true,
  imports: [CommonModule, MatButtonModule, TimelineChartComponent, MatDividerModule, ConsoleComponent, FlexModule, LauncherComponent],
  templateUrl: './result.component.html',
  styleUrls: ['./result.component.scss']
})
export class ResultComponent implements AfterViewInit, OnDestroy{

  @ViewChild('chartComponent') public charts: TimelineChartComponent;

  @Output() messageEvent = new EventEmitter<string>();
  @Output() updateTimeRetrieved = new EventEmitter<number>();

  private dataRequestConfirmed$: any;
  private dataRequestDeaths$: any;

  constructor(private dataService: RemoteDataService) {

  }

  ngAfterViewInit() {
    // const lastCommitTime$ = this.dataService.getLatestCommits();
    // lastCommitTime$.subscribe(data => {
    //   const lastCommit = new Date(data[0].commit.author.date).getTime();
    //   this.updateTimeRetrieved.emit(lastCommit);
    //   this.loadDataSets(lastCommit);
    // });
  }

  /**
   * Fetches the corresponding Confirmed and Deaths cases data.
   */
  public loadDataSets(lastCommit: number) {
    this.dataRequestConfirmed$ = this.dataService.getDataSet(0, lastCommit);
    this.dataRequestDeaths$ = this.dataService.getDataSet(1, lastCommit);

    forkJoin([this.dataRequestConfirmed$, this.dataRequestDeaths$]).subscribe(results => {
      const data = results[0].toString();
      this.charts.transformChartConfirmedCases(data);
      // const jsonDataConfirmed = this.dataService.csvToJson(results[0].toString());
      // const jsonDataDeaths = this.dataService.csvToJson(results[1].toString());
      //
      // const worldData: ICasesData = {
      //   totalConfirmed: jsonDataConfirmed,
      //   totalDeaths: jsonDataDeaths
      // };
      //
      // this.confirmedList.data = jsonDataConfirmed;
      // this.deathsList.data = jsonDataDeaths;
      // this.map.data = worldData;
      // this.map.onDataSetSelected({index: 0});

      // Hide splash screen after the data is loaded
      this.messageEvent.emit('splash-screen--hidden');
    });
  }


  public formatDateLabel(item: any): string {
    return item.date.toLocaleDateString();
  }

  public onRegionSelected(region: any) {
    console.log(region);
    //this.map.zoomMapToLoc(region.lat, region.lon);
  }


  public ngOnDestroy() {
    if (this.dataRequestConfirmed$) {
      this.dataRequestConfirmed$.unsubscribe();
    }

    if (this.dataRequestDeaths$) {
      this.dataRequestDeaths$.unsubscribe();
    }
  }
}
