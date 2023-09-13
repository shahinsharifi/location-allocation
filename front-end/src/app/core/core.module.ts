import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {CommandService} from "./http/command.service";
import {HttpClientModule} from "@angular/common/http";
import {WebsocketService} from "./websocket/websocket.service";
import {RemoteDataService} from "./http/data.service";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    HttpClientModule
  ],
  providers:[CommandService, RemoteDataService, WebsocketService],

})
export class CoreModule { }
