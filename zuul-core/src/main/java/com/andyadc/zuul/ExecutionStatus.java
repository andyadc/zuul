package com.andyadc.zuul;

public enum ExecutionStatus {

    SUCCESS(1), SKIPPED(-1), DISABLED(-2), FAILED(-3);

    int status;

    ExecutionStatus(int status) {
        this.status = status;
    }
}
