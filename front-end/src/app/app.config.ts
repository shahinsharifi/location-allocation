import {ApplicationConfig, importProvidersFrom, isDevMode} from '@angular/core';
import {provideAnimations} from '@angular/platform-browser/animations';
import {provideStore} from '@ngrx/store';
import {provideStoreDevtools} from '@ngrx/store-devtools';
import {provideEffects} from '@ngrx/effects';
import {appReducers, metaReducers} from "./core/state/app.reducer";
import {WebSocketEffects} from "./core/websocket/state/websocket.effects";
import {SessionEffects} from "./session/state/session.effects";
import {provideHttpClient} from "@angular/common/http";
import {WebsocketService} from "./core/websocket/websocket.service";


export const appConfig: ApplicationConfig = {
  providers: [
    WebsocketService,
    importProvidersFrom(),
    provideAnimations(),
    provideHttpClient(),
    provideStore(appReducers, {metaReducers}),
    provideEffects([WebSocketEffects, SessionEffects]),
    provideStoreDevtools({ maxAge: 25, logOnly: !isDevMode() })
  ]
};
