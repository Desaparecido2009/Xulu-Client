package dev.xulu.settings;

import com.elementars.eclient.Xulu;
import com.elementars.eclient.enemy.Enemies;
import com.elementars.eclient.enemy.Enemy;
import com.elementars.eclient.friend.Friend;
import com.elementars.eclient.friend.Friends;
import com.elementars.eclient.guirewrite.Element;
import com.elementars.eclient.guirewrite.Frame;
import com.elementars.eclient.guirewrite.HUD;
import dev.xulu.newgui.NewGUI;
import dev.xulu.newgui.Panel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class FileManager
{
    public File Eclient;
    public File EclientSettings;
    public File EclientStorageESP;
    public File EclientCache;
    public File EclientConfigs;

    public FileManager() {
        this.Eclient = new File("Xulu");
        if (!this.Eclient.exists()) {
            this.Eclient.mkdirs();
        }
        this.EclientSettings = new File(this.Eclient, "settings");
        if (!this.EclientSettings.exists()) {
            this.EclientSettings.mkdirs();
        }
        this.EclientStorageESP = new File(this.Eclient, "StorageESP Logs");
        if (!this.EclientStorageESP.exists()) {
            this.EclientStorageESP.mkdirs();
        }
        this.EclientCache = new File(this.EclientSettings, "cache");
        if (!this.EclientCache.exists()) {
            this.EclientCache.mkdirs();
        }
        this.EclientConfigs = new File(this.Eclient, "configs");
        if (!this.EclientConfigs.exists()) {
            this.EclientConfigs.mkdirs();
        }
    }

    public boolean appendTextFile(final String data, final String file) {
        try {
            final Path path = Paths.get(file);
            Files.write(path, Collections.singletonList(data), StandardCharsets.UTF_8, Files.exists(path, new LinkOption[0]) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        }
        catch (IOException e) {
            System.out.println("WARNING: Unable to write file: " + file);
            return false;
        }
        return true;
    }

    public void saveFriends() {
        try {
            final File file = new File(this.Eclient.getAbsolutePath(), "Friends.txt");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Friend f : Friends.getFriends()) {
                try {
                    out.write(f.getUsername());
                    out.write("\r\n");
                }
                catch (Exception ex) {}
            }
            out.close();
        }
        catch (Exception ex2) {}
    }

    public void loadFriends() {
        try {
            final File file = new File(this.Eclient.getAbsolutePath(), "Friends.txt");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    Friends.addFriend(line);
                }
                catch (Exception ex) {}
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            this.saveFriends();
        }
    }

    public void saveEnemies() {
        try {
            final File file = new File(this.Eclient.getAbsolutePath(), "Enemies.txt");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Enemy e : Enemies.getEnemies()) {
                try {
                    out.write(e.getUsername());
                    out.write("\r\n");
                }
                catch (Exception ex) {}
            }
            out.close();
        }
        catch (Exception ex2) {}
    }

    public void loadEnemies() {
        try {
            final File file = new File(this.Eclient.getAbsolutePath(), "Enemies.txt");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    Enemies.addEnemy(line);
                }
                catch (Exception ex) {}
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            this.saveEnemies();
        }
    }

    public void saveNewGui() {
        try {
            final File file = new File(this.Eclient.getAbsolutePath(), "NewGui.txt");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Panel p : NewGUI.getPanels()) {
                try {
                    out.write(p.title + ":" + p.x + ":" + p.y + ":" + p.extended);
                    out.write("\r\n");
                }
                catch (Exception ex) {}
            }
            out.close();
        }
        catch (Exception ex2) {}
    }

    public void loadNewGui() {
        try {
            final File file = new File(this.Eclient.getAbsolutePath(), "NewGui.txt");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    final String curLine = line.trim();
                    final String name = curLine.split(":")[0];
                    final String x = curLine.split(":")[1];
                    final String y = curLine.split(":")[2];
                    final String e = curLine.split(":")[3];
                    final Panel p = NewGUI.getPanelByName(name);
                    if (p == null) {
                        continue;
                    }
                    p.x = Double.parseDouble(x);
                    p.y = Double.parseDouble(y);
                    p.extended = Boolean.parseBoolean(e);
                }
                catch (Exception ex) {}
            }
            br.close();
        }
        catch (Exception e2) {
            this.saveNewGui();
        }
    }

    public void saveHUD() {
        if (Xulu.hud == null) {
            return;
        }
        try {
            final File file = new File(this.Eclient.getAbsolutePath(), "HUD.txt");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Frame f : HUD.frames) {
                out.write(f.title + ":" + f.x + ":" + f.y);
                out.write("\r\n");
            }
            out.write(Xulu.hud.hudPanel.getLabel() + ":" + Xulu.hud.hudPanel.getX() + ":" + Xulu.hud.hudPanel.getY());
            out.write("\r\n");
            out.close();
        }
        catch (Exception ex) {}
    }

    public void loadHUD() {
        if (Xulu.hud == null) {
            return;
        }
        try {
            final File file = new File(this.Eclient.getAbsolutePath(), "HUD.txt");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    final String curLine = line.trim();
                    final String name = curLine.split(":")[0];
                    final String x = curLine.split(":")[1];
                    final String y = curLine.split(":")[2];
                    final double x2 = Double.parseDouble(x);
                    final double y2 = Double.parseDouble(y);
                    if (name.equalsIgnoreCase(Xulu.hud.hudPanel.getLabel())) {
                        Xulu.hud.hudPanel.setX((int)x2);
                        Xulu.hud.hudPanel.setY((int)y2);
                    }
                    final Frame p = HUD.getframeByName(name);
                    if (p != null) {
                        p.x = x2;
                        p.y = y2;
                    }
                    final Element e = (Element)Xulu.MODULE_MANAGER.getModuleByName(name);
                    if (e == null) {
                        continue;
                    }
                    e.setX(x2);
                    e.setY(y2);
                }
                catch (Exception ex) {}
            }
            br.close();
        }
        catch (Exception e2) {
            e2.printStackTrace();
            this.saveHUD();
        }
    }

    public void saveStorageESP(final String name, final String coords, final String chests) {
        try {
            final File file = new File(this.EclientStorageESP.getAbsolutePath(), name + ".txt");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(coords + " with " + chests + " chests");
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
