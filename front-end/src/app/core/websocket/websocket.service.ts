import { Injectable } from '@angular/core';
import { RxStomp, RxStompState } from "@stomp/rx-stomp";
import { Store } from "@ngrx/store";
import { websocketConfig } from "./websocket.config";
import { AppState } from "../state/app.state";
import { websocketActions } from "./state/websocket.actions";
import { Observable, Subscription } from "rxjs";
import { Session } from "../../session/session";
import {Message, MessageSubject} from "./message";
import { distinctUntilChanged } from 'rxjs/operators';
import { BehaviorSubject } from 'rxjs';
import { fromSession } from "../../session/state/session.selectors";

@Injectable({
  providedIn: 'root',
})
export class WebsocketService extends RxStomp {

  private topicEndpoint$: BehaviorSubject<string> = new BehaviorSubject(null);
  private sessionState$: Observable<Session>;
  private subscriptionRef: Subscription;

  constructor(private store: Store<AppState>) {
    super();
    this.configure(websocketConfig);
    this.connectionState$.pipe(distinctUntilChanged())
    .subscribe(this.connectionStateChange.bind(this));
    this.sessionState$ = this.store.select(state => fromSession.selectActiveSession(state));
    this.sessionState$.subscribe(this.sessionStateChange.bind(this));
    this.activate();
  }

  connectionStateChange(state: RxStompState) {
    if (state === RxStompState.OPEN && this.topicEndpoint$.getValue()) {
      this.store.dispatch(websocketActions.initConnectionSuccess());
    } else if (state === RxStompState.CLOSED) {
      this.store.dispatch(websocketActions.closeConnectionSuccess());
    }
  }

  sessionStateChange(session: Session) {
    if (session && session.id) {
      console.log('Session state change and we are connecting...');
      this.topicEndpoint$.next('/topic/' + session.id);
      this.subscriptionRef = this.watch(this.topicEndpoint$.getValue()).subscribe(this.receiveMessage.bind(this));
    } else {
      this.topicEndpoint$.next(null);
      if (this.subscriptionRef) {
        this.subscriptionRef.unsubscribe();
      }
    }
  }

  receiveMessage(message: any) {
    const messageBody: Message = JSON.parse(message.body);
    this.store.dispatch(websocketActions.receiveMessage({message: messageBody}));
  }

  handleMessage(message: Message) {
    switch (message.subject) {
      case MessageSubject.SESSION_STATUS:
        console.log('SESSION_STATUS', message.data);
        break;
      case MessageSubject.SESSION_LOG:
        console.log('SESSION_LOG', message.data);
        break;
      case MessageSubject.SESSION_PROGRESS_DATA:
        console.log('SESSION_PROGRESS_DATA', message.data);
        break;
      default:
        console.warn('Unhandled WebSocket message type:', message.subject);
    }
  }
}
