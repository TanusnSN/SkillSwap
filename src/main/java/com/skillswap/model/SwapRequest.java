package com.skillswap.model;

public class SwapRequest {
    public int id;
    public int senderId;
    public int receiverId;
    public int skillId;
    public String status; // "PENDING", "ACCEPTED", "REJECTED"
    public String createdAt;

    public SwapRequest() {}

    public SwapRequest(int id, int senderId, int receiverId, int skillId, String status, String createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.skillId = skillId;
        this.status = status;
        this.createdAt = createdAt;
    }
}