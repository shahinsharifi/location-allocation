import {Message} from "../message";

export interface WebSocketState {
  connected: boolean;
  error: Error | null;
  message: Message | null;
}
