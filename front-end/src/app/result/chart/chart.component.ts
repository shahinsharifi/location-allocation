import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';


@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.scss']
})
export class ChartComponent implements OnInit, OnDestroy {

  constructor() {

  }

  ngOnInit() {

  }


  ngOnDestroy() {
  }

}
