import {Component, OnDestroy, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {MatButtonModule} from "@angular/material/button";
import {FlexModule} from "@angular/flex-layout";

@Component({
  selector: 'app-console',
  standalone: true,
  imports: [CommonModule, MatButtonModule, FlexModule],
  templateUrl: './console.component.html',
  styleUrls: ['./console.component.scss']
})
export class ConsoleComponent implements  OnInit, OnDestroy {

  ngOnInit() {

  }

  ngOnDestroy() {
  }
}
