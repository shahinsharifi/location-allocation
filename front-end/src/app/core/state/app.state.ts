import {WebSocketState} from "../websocket/state/websocket.state";
import {SessionState} from "../../session/state/session.state";
import {LauncherState} from "../../launcher/state/launcher.state";


export interface AppState {
  websocket: WebSocketState;
  session: SessionState;
  launcher: LauncherState;
}

