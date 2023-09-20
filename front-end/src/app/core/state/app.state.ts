import {WebSocketState} from "../websocket/state/websocket.state";
import {SessionState} from "../../session/state/session.state";
import {LauncherState} from "../../launcher/state/launcher.state";
import {MapState} from "../../map/state/map.state";
import {ReportState} from "../../report/state/report.state";



export interface AppState {
  websocket: WebSocketState;
  session: SessionState;
  launcher: LauncherState;
  result: ReportState;
  map: MapState;
}

