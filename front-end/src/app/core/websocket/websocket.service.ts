import {Injectable} from '@angular/core';
import {RxStomp, RxStompState} from "@stomp/rx-stomp";
import {Store} from "@ngrx/store";
import {websocketConfig} from "./websocket.config";
import {AppState} from "../state/app.state";
import {websocketActions} from "./state/websocket.actions";
import {BehaviorSubject, Observable, Subscription} from "rxjs";
import {Session} from "../../session/session";
import {Message, MessageSubject} from "./message";
import {distinctUntilChanged} from 'rxjs/operators';
import {sessionActions} from "../../session/state/session.actions";
import {fromSessionSelectors} from "../../session/state/session.selectors";
import {reportActions} from "../../report/state/report.actions";

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
    this.sessionState$ = this.store.select(state => fromSessionSelectors.selectActiveSession(state));
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
        this.store.dispatch(sessionActions.updateSession({activeSession: message.data as Session}));
        break;
      case MessageSubject.SESSION_LOG:
        this.store.dispatch(reportActions.updateLogs({log: message.message}));
        break;
      case MessageSubject.SESSION_PROGRESS_DATA:
        this.store.dispatch(reportActions.updateProgress({progress: message.data}));
        break;
      default:
        console.warn('Unhandled WebSocket message type:', message.subject);
    }
  }
}
