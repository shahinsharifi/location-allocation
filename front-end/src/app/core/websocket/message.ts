export enum MessageType {
  LOG = "LOG",
  DATA = "DATA"
}

export enum MessageSubject {
  SESSION_STATUS = "SESSION_STATUS",
  SESSION_LOG = "SESSION_LOG",
  SESSION_LOCATION_FITNESS_DATA = "SESSION_LOCATION_FITNESS_DATA",
  SESSION_ALLOCATION_FITNESS_DATA = "SESSION_ALLOCATION_FITNESS_DATA",
  SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION = "SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION",
}

export interface Message {
  type: MessageType | null
  subject: MessageSubject | null;
  message: string | null;
  metadata: any;
  data: any;
}
