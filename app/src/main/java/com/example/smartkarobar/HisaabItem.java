package com.example.smartkarobar;
public class HisaabItem {
    private final String title;
    private final String subtitle;   // e.g. "Aaj, 2:30pm"
    private final String amount;     // e.g. "+Rs. 5,000"
    private final String type;       // e.g. "NAQD", "BAQI", "EXPENSE"
    private final int dotColor;      // color int

    public HisaabItem(String title, String subtitle, String amount, String type, int dotColor) {
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.type = type;
        this.dotColor = dotColor;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getAmount() { return amount; }
    public String getType() { return type; }
    public int getDotColor() { return dotColor; }
}