import {AfterViewChecked, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { CommonModule } from '@angular/common';
import {MatButtonModule} from "@angular/material/button";
import {FlexModule} from "@angular/flex-layout";
import {Observable} from "rxjs";
import {select, Store} from "@ngrx/store";

@Component({
  selector: 'app-console',
  standalone: true,
  imports: [CommonModule, MatButtonModule, FlexModule],
  templateUrl: './console.component.html',
  styleUrls: ['./console.component.scss']
})
export class ConsoleComponent implements OnInit, OnDestroy, AfterViewChecked {

  logs: string[] = [];
  @ViewChild('scrollBottom') private appConsole: ElementRef;

  private updateLogs$: Observable<Array<string>>;

  constructor(private store: Store<any>) {
    this.updateLogs$ = this.store.pipe(select(state => state.result.logs));
  }

  ngOnInit(): void {
    this.updateLogs$.subscribe(this.updateLogs.bind(this));
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  updateLogs(logs: Array<string>) {
    this.logs = logs;
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.appConsole.nativeElement.scrollTop = this.appConsole.nativeElement.scrollHeight;
    } catch(err) { }
  }

  ngOnDestroy(): void {
    // Clean up
  }
}
