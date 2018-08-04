package xyz.andrewh.remotify;

public class NotificationModel {
    private String tag;
    private int id;
    private String uniqueId;
    private String title;
    private String text;
    private String packageName;
    private String applicationName;
    private String bigText;
    private long timestamp;
    private boolean isBigNotification;
    private String notificationId;
    private boolean hasLargeIcon;

    public NotificationModel() {
    }

    public boolean isHasLargeIcon() {
        return hasLargeIcon;
    }

    public void setHasLargeIcon(boolean hasLargeIcon) {
        this.hasLargeIcon = hasLargeIcon;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public boolean isBigNotification() {
        return isBigNotification;
    }

    public void setBigNotification(boolean bigNotification) {
        isBigNotification = bigNotification;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBigText() {
        return bigText;
    }

    public void setBigText(String bigText) {
        this.bigText = bigText;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
