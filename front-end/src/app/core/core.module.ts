import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {CommandService} from "./http/command.service";
import {HttpClientModule} from "@angular/common/http";
import {WebsocketService} from "./websocket/websocket.service";
import {RemoteDataService} from "./http/data.service";
import {MatIconRegistry} from "@angular/material/icon";
import {DomSanitizer} from "@angular/platform-browser";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    HttpClientModule
  ],
  providers:[CommandService, RemoteDataService, WebsocketService],

})
export class CoreModule {
  constructor(
    private domSanitizer: DomSanitizer,
    private matIconRegistry: MatIconRegistry
  ) {
    this.matIconRegistry.addSvgIconSetInNamespace('mat_outline', this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/material-outline.svg'));
    this.matIconRegistry.addSvgIconSetInNamespace('mat_solid', this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/material-solid.svg'));
    this.matIconRegistry.addSvgIconSetInNamespace('wgg_icons', this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/wgg.svg'));
  }
}
