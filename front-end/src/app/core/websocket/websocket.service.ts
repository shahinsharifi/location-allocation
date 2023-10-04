import {Injectable} from '@angular/core';
import {RxStomp, RxStompState} from "@stomp/rx-stomp";
import {select, Store} from "@ngrx/store";
import {websocketConfig} from "./websocket.config";
import {AppState} from "../state/app.state";
import {websocketActions} from "./state/websocket.actions";
import {BehaviorSubject, filter, Observable, Subscription, take} from "rxjs";
import {Session} from "../../session/session";
import {Message, MessageSubject} from "./message";
import {distinctUntilChanged} from 'rxjs/operators';
import {sessionActions} from "../../session/state/session.actions";
import {reportActions} from "../../report/state/report.actions";

@Injectable({
  providedIn: 'root',
})
export class WebsocketService extends RxStomp {

  private topicEndpoint$: BehaviorSubject<string> = new BehaviorSubject(null);
  private sessionState$: Observable<Session>;
  private subscriptionRef: Subscription;
  private activeSession: Session;

  constructor(private store: Store<AppState>) {
    super();
    this._setupWebSocketConnection();
    this._setupSessionStateMonitoring();
    this.activate();
  }

  private _setupWebSocketConnection() {
    this.configure(websocketConfig);
    this.connectionState$.pipe(distinctUntilChanged())
    .subscribe(this._manageConnectionState.bind(this));
  }

  private _setupSessionStateMonitoring() {
    this.sessionState$ = this.store.pipe(select(state => state.session.activeSession));
    this.sessionState$.subscribe(this._manageSessionState.bind(this));
  }

  private _manageConnectionState(state: RxStompState) {
    if (state === RxStompState.OPEN && this.topicEndpoint$.getValue()) {
      this.store.dispatch(websocketActions.initConnectionSuccess());
    } else if (state === RxStompState.CLOSED) {
      this.store.dispatch(websocketActions.closeConnectionSuccess());
    }
  }

  private _manageSessionState(session: Session) {
    console.log('Session state changed:', session);
    if (!session || !session.id) {
      this._resetConnectionForNewSession(); // new method called
    } else if(session && session.id && (this.activeSession || this.activeSession?.id != session.id)){
      this._configureConnectionForSession(session); // new method called
    }
  }

  private _configureConnectionForSession(session: Session) {
    console.log('activeSession: ' + this.activeSession?.id + ' New session: ' + session.id);
    if (session && session.id) {
      if(!this.activeSession || this.activeSession.id != session.id) {
        this.topicEndpoint$.next('/topic/' + session.id);
        this.subscriptionRef = this.watch(this.topicEndpoint$.getValue()).subscribe(this.receiveMessage.bind(this));

        if (!this.connected()) {
          this.activate();
        }

        this.activeSession = session;
      }
    }
  }

  private _resetConnectionForNewSession() {
    this._disconnectWebSocket().then(() =>
      this._subscribeToNewSessionState()
    );
  }

  private _disconnectWebSocket(): Promise<void> {
    return new Promise((resolve) => {
      if (this.subscriptionRef) {
        this.subscriptionRef.unsubscribe();
        this.subscriptionRef = null; // Release the reference
      }
      if (this.connected()) {
        this.deactivate().then(() => {
          console.log('Deactivated WebSocket connection');
          resolve();
        });
      } else {
        resolve();
      }
    });
  }

  private _subscribeToNewSessionState() {
    this.sessionState$.pipe(
      filter(session => !!session && !!session.id),
      take(1)
    ).subscribe((session) => {
      this._configureConnectionForSession(session);  // Reactive the connection with new session
    });
  }

  receiveMessage(message: any) {
    this.store.dispatch(websocketActions.receiveMessage({message: JSON.parse(message.body)}));
  }

  handleMessage(message: Message) {
    switch (message.subject) {
      case MessageSubject.SESSION_STATUS:
        this._updateSessionStatus(message);
        break;
      case MessageSubject.SESSION_LOG:
        this._updateSessionLog(message);
        break;
      case MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_TOTAL_PROGRESS:
        this._updateAllocationFitnessChart(message);
        break;
      case MessageSubject.SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION_PROGRESS:
        this._updateCostDistributionChart(message);
        break;
      default:
        console.warn('Unhandled WebSocket message type:', message.subject);
    }
  }

  _updateSessionStatus(message: Message) {
    this.store.dispatch(sessionActions.updateSessionSTATUS({status: message.data.status}));
  }

  _updateSessionLog(message: Message) {
    this.store.dispatch(reportActions.updateLogs({log: message.message}));
  }

  _updateAllocationFitnessChart(message: Message) {
    this.store.dispatch(reportActions.updateAllocationFitnessChart({metadata: message.metadata, data: message.data}));
  }

  _updateCostDistributionChart(message: Message) {
    this.store.dispatch(reportActions.updateCostDistributionChart({metadata: message.metadata, data: message.data}));
  }

}
