package it.echo.kira.account;

/**
 * Stored account. Refresh token is only present for Microsoft accounts.
 */
public final class Account {

    public enum Type {
        OFFLINE,
        MICROSOFT
    }

    private final Type type;
    private String name;
    private String uuid;
    private String refreshToken;

    public Account(Type type, String name, String uuid, String refreshToken) {
        this.type = type;
        this.name = name;
        this.uuid = uuid;
        this.refreshToken = refreshToken;
    }

    public Type type() {
        return type;
    }

    public String name() {
        return name;
    }

    public String uuid() {
        return uuid;
    }

    public String refreshToken() {
        return refreshToken;
    }

    public void name(String name) {
        this.name = name;
    }

    public void uuid(String uuid) {
        this.uuid = uuid;
    }

    public void refreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
