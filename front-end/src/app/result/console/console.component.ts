import {Component, OnDestroy, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-console',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './console.component.html',
  styleUrls: ['./console.component.scss']
})
export class ConsoleComponent implements  OnInit, OnDestroy {

  constructor() {

  }

  ngOnInit() {

  }


  ngOnDestroy() {
  }
}
