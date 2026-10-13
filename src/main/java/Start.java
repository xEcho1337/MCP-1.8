import net.minecraft.client.main.Main;

void main(String[] args) {
    String os = System.getProperty("os.name").toLowerCase();
    String arch = System.getProperty("os.arch").toLowerCase();
    String dir;
    if (os.startsWith("windows")) dir = "windows";
    else if (os.contains("mac")) dir = arch.contains("aarch64") || arch.contains("arm64") ? "macosx-arm64" : "macosx-x64";
    else dir = "linux";
    System.setProperty("org.lwjgl.librarypath", new File("../natives/" + dir).getAbsolutePath());

    Main.main(concat(new String[]{ "--version", "MavenMCP", "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}" }, args));
}

public static <T> T[] concat(T[] first, T[] second) {
    T[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
}
