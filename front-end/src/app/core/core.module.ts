import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {CommandService} from "./http/command.service";
import {HttpClientModule} from "@angular/common/http";
import {WebsocketService} from "./websocket/websocket.service";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    HttpClientModule
  ],
  providers:[CommandService, WebsocketService]
})
export class CoreModule { }
