package de.wigeogis.pmedian.database.entity;

public enum SessionStatus {
  PENDING,
  STARTING,
  RUNNING,
  ABORTING,
  ABORTED,
  FAILED,
  COMPLETED
}
