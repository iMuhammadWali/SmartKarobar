package com.example.smartkarobar;

public class HisaabItem {
    private String docId;      // Firestore document id (for delete/update)
    private double rawAmount;  // numeric amount (for re-use in clear flow)

    private String title;
    private String subtitle;
    private String amount;
    private String type;
    private int dotColor;

    public HisaabItem(String title, String subtitle, String amount, String type, int dotColor) {
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.type = type;
        this.dotColor = dotColor;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public double getRawAmount() {
        return rawAmount;
    }

    public void setRawAmount(double rawAmount) {
        this.rawAmount = rawAmount;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public int getDotColor() {
        return dotColor;
    }
}