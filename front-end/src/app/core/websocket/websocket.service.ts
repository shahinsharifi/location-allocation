import {Injectable,} from '@angular/core';
import {RxStomp, RxStompState} from "@stomp/rx-stomp";
import {Store} from "@ngrx/store";
import {websocketConfig} from "./websocket.config";
import {AppState} from "../state/app.state";
import {websocketActions} from "./state/websocket.actions";
import {Message, MessageTopic} from "./state/websocket.state";
import {Observable} from "rxjs";
import {Session} from "../../session/session";
import {fromLauncher} from "../../launcher/state/launcher.selectors";

@Injectable({
  providedIn: 'root',
})
export class WebsocketService extends RxStomp {

  private topicEndpoint: string = null;
  private sessionState$: Observable<Session> = this.store.select(fromLauncher.selectLauncherSession);

  constructor(private store: Store<AppState>) {
    super();
    this.connectionState$.subscribe(this.connectionStateChange.bind(this));
    this.sessionState$.subscribe(this.sessionStateChange.bind(this));
    this.configure(websocketConfig);
    this.activate();
  }

  connectionStateChange(state: RxStompState) {
    if (state === RxStompState.OPEN && this.topicEndpoint) {
      this.store.dispatch(websocketActions.initConnectionSuccess());
    } else if (state === RxStompState.CLOSED) {
      this.store.dispatch(websocketActions.closeConnectionSuccess());
    }
  }


  sessionStateChange(session: Session) {
    console.log("we are here!!!")
    if (session && session.id) {
      this.topicEndpoint = '/topic/' + session.id;
      this.watch(this.topicEndpoint).subscribe(this.receiveMessage.bind(this));
    } else {
      this.topicEndpoint = null;
      if (this.stompClient && this.connected()) {
        this.stompClient.unsubscribe(this.topicEndpoint);
      }
    }
  }

  sendMessage(topic: MessageTopic, data: any) {
    const message: any = {
      topic: topic,
      data: data
    };
    if (this.connected() && this.topicEndpoint) {
      this.publish({destination: this.topicEndpoint, body: message});
    }
  }


  receiveMessage(message: any) {
    let messageBody: Message = JSON.parse(message.body);
    this.store.dispatch(websocketActions.receiveMessage({message: messageBody}));
  }

}

