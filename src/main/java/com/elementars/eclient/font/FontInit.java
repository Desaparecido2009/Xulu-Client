package com.elementars.eclient.font;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class FontInit
{
    public void initFonts() {
        try {
            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            this.tryRegisterFont(ge, "/fonts/Comfortaa-Regular.ttf");
            this.tryRegisterFont(ge, "/fonts/GOTHIC.TTF");
            this.tryRegisterFont(ge, "/fonts/MODERN SPACE.ttf");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryRegisterFont(final GraphicsEnvironment ge, final String path) {
        try (InputStream is = FontInit.class.getResourceAsStream(path)) {
            if (is != null) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
