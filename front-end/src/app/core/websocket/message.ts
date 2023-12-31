export enum MessageType {
  LOG = "LOG",
  DATA = "DATA"
}

export enum MessageSubject {
  SESSION_STATUS = "SESSION_STATUS",
  SESSION_LOG = "SESSION_LOG",
  SESSION_ALLOCATION_TRAVEL_COST_TOTAL_PROGRESS = "SESSION_ALLOCATION_TRAVEL_COST_TOTAL_PROGRESS",
  SESSION_ALLOCATION_TRAVEL_COST_TOTAL_FINAL = "SESSION_ALLOCATION_TRAVEL_COST_TOTAL_PROGRESS",
  SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION_PROGRESS = "SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION_PROGRESS",
  SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION_FINAL = "SESSION_ALLOCATION_TRAVEL_COST_DISTRIBUTION_PROGRESS",
}

export interface Message {
  type: MessageType | null
  subject: MessageSubject | null;
  message: string | null;
  metadata: any;
  data: any;
}
