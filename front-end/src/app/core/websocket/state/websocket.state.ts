import {Message} from "../message";

export interface WebSocketState {
  connected: boolean;
  error: string | null;
  message: Message | null;
}
