package com.queueless.dto;

public class AdminLiveQueueResponse {

    private AdminTokenResponse current;
    private AdminTokenResponse next;
    private long waitingCount;

    public AdminLiveQueueResponse(
            AdminTokenResponse current,
            AdminTokenResponse next,
            long waitingCount
    ) {
        this.current = current;
        this.next = next;
        this.waitingCount = waitingCount;
    }

    public AdminTokenResponse getCurrent() {
        return current;
    }

    public AdminTokenResponse getNext() {
        return next;
    }

    public long getWaitingCount() {
        return waitingCount;
    }
}
