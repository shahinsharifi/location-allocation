export enum MessageType {
  LOG = "SEND",
  DATA = "RECEIVE"
}

export enum MessageSubject {
  SESSION_STATUS = "SESSION_STATUS",
  SESSION_LOG = "SESSION_LOG",
  SESSION_PROGRESS_DATA = "SESSION_PROGRESS_DATA"
}

export interface Message {
  type: MessageType | null
  subject: MessageSubject | null;
  message: string | null;
  data: any;
}
