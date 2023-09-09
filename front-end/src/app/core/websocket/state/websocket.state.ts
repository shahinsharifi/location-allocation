export enum WebSocketMessageType {
  SEND = "SEND",
  RECEIVE = "RECEIVE",
  ERROR = "ERROR"
}

export enum MessageTopic {
  ADMIN = "ADMIN",
  USER = "USER",
  PROJECT = "PROJECT",
  SYSTEM = "SYSTEM"
}

export enum MessageAction {
  UPLOAD = "UPLOAD",
  DOWNLOAD = "DOWNLOAD",
  CREATE = "CREATE",
  DELETE = "DELETE",
  EDIT = "EDIT",
  GEOCODING = "GEOCODING",
}

export interface Message {
  type: WebSocketMessageType | null
  action?: MessageAction | null;
  topic?: MessageTopic | null;
  data: any;

}

export interface WebSocketState {
  connected: boolean;
  error: string | null;
  message: Message | null;
}
