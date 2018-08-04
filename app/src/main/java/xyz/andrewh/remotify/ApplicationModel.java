package xyz.andrewh.remotify;

public class ApplicationModel {

        String name;
        String packageName;
        String type;
        boolean isChecked;

    public ApplicationModel() {
    }

    public ApplicationModel(String name, String packageName, boolean isChecked) {
        this.name = name;
        this.packageName = packageName;
        this.isChecked = isChecked;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
