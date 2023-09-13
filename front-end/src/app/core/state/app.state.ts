import {WebSocketState} from "../websocket/state/websocket.state";
import {SessionState} from "../../session/state/session.state";
import {LauncherState} from "../../launcher/state/launcher.state";
import {MapState} from "../../map/state/map.state";
import {ResultState} from "../../result/state/result.state";


export interface AppState {
  websocket: WebSocketState;
  session: SessionState;
  launcher: LauncherState;
  result: ResultState;
  map: MapState;
}

