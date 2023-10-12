package e.g.jme3hudltest;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.style.BaseStyles;
import e.g.jme3hudl.CardLayout;
import e.g.jme3hudl.GroupLayout;
import e.g.jme3hudl.LayoutStyle;

public class GuiApplication extends SimpleApplication {

    public static void main(String[] args) {
        GuiApplication app = new GuiApplication();
        AppSettings appSettings = new AppSettings(true);

        appSettings.setResolution(1524, 824);
        appSettings.setResizable(true);
        appSettings.setGammaCorrection(false);

        app.setSettings(appSettings);
        app.start();
    }

    private Container jPanel1;
    protected boolean initialized = false;

    @Override
    public void simpleInitApp() {
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        jPanel1 = new Container();
        Container jPanel2 = new Container();
        Container jPanel3 = new Container();
        Container jPanel4 = new Container();

        Label jLabel1 = new Label("");
        Label jLabel2 = new Label("");
        Label jLabel3 = new Label("");
        Label jLabel4 = new Label("");
        Label jLabel5 = new Label("");
        Label jLabel6 = new Label("");
        Label jLabel7 = new Label("");
        Label jLabel8 = new Label("");

        Checkbox jCheckBox1 = new Checkbox("");
        Checkbox jCheckBox2 = new Checkbox("");
        Checkbox jCheckBox3 = new Checkbox("");
        Checkbox jCheckBox4 = new Checkbox("");

        Button jButton1 = new Button("");
        Button jButton2 = new Button("");
        
        QuadBackgroundComponent background = new QuadBackgroundComponent(GuiGlobals.getInstance().loadTexture("res/e.g.jme3hudl/Gui-Container.png", true, false));
        background.setColor(new ColorRGBA(0.235f, 0.247f, 0.255f, 1.0f));

        jPanel1.setBackground(background);

        background = new QuadBackgroundComponent(GuiGlobals.getInstance().loadTexture("res/e.g.jme3hudl/Gui-Container.png", true, false));
        background.setColor(new ColorRGBA(0.200f, 0.200f, 0.200f, 1.0f));
        jPanel2.setBackground(background);
        jPanel2.setLocalTranslation(0, 0, 1);

        jLabel1.setText("jMonkeyEngine");
        jLabel1.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/CantarellExtraBold.fnt"));
        jLabel1.setFontSize(18);
        jLabel1.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jLabel1.setLocalTranslation(0, 0, 1);
        jLabel1.setTextHAlignment(HAlignment.Left);
        jLabel1.setTextVAlignment(VAlignment.Center);

        /*jLabel2.setText("jLabel2");*/
        jLabel2.setBackground(new QuadBackgroundComponent(GuiGlobals.getInstance().loadTexture("res/e.g.jme3hudl/jmonkeyplatform.png", false, false)));
        jLabel2.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jLabel2.setFontSize(15);
        jLabel2.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jLabel2.setLocalTranslation(0, 0, 1);
        jLabel2.setTextHAlignment(HAlignment.Center);
        jLabel2.setTextVAlignment(VAlignment.Center);

        jLabel3.setText("v3.6.1-stable+");
        jLabel3.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jLabel3.setFontSize(13);
        jLabel3.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jLabel3.setLocalTranslation(0, 0, 1);
        jLabel3.setTextHAlignment(HAlignment.Left);
        jLabel3.setTextVAlignment(VAlignment.Center);

        jLabel4.setText("Platform");
        jLabel4.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jLabel4.setFontSize(18);
        jLabel4.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jLabel4.setLocalTranslation(0, 0, 1);
        jLabel4.setTextVAlignment(VAlignment.Center);
        jLabel4.setPreferredSize(new Vector3f(71, 42, 0));

        jCheckBox1.setText("Android");
        jCheckBox1.setPreferredSize(new Vector3f(72, 23, 0));
        jCheckBox1.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jCheckBox1.setFontSize(15);
        jCheckBox1.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jCheckBox1.setLocalTranslation(0, 0, 1);
        jCheckBox1.setTextVAlignment(VAlignment.Center);
        jCheckBox1.setOnView(new IconComponent("res/e.g.jme3hudl/GuiChecked.png", 1f, 5, 0, 1f, false ));
        jCheckBox1.setOffView(new IconComponent("res/e.g.jme3hudl/GuiUnchecked.png", 1f, 5, 0, 1f, false ));
        jCheckBox1.setChecked(true);

        jCheckBox2.setText("Desktop");
        jCheckBox2.setPreferredSize(new Vector3f(72, 23, 0));
        jCheckBox2.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jCheckBox2.setFontSize(15);
        jCheckBox2.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jCheckBox2.setLocalTranslation(0, 0, 1);
        jCheckBox2.setTextVAlignment(VAlignment.Center);
        jCheckBox2.setOnView(new IconComponent("res/e.g.jme3hudl/GuiChecked.png", 1f, 5, 0, 1f, false ));
        jCheckBox2.setOffView(new IconComponent("res/e.g.jme3hudl/GuiUnchecked.png", 1f, 5, 0, 1f, false ));
        jCheckBox2.setChecked(true);

        jCheckBox3.setText("VR");
        jCheckBox3.setPreferredSize(new Vector3f(72, 26, 0));
        jCheckBox3.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jCheckBox3.setFontSize(15);
        jCheckBox3.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jCheckBox3.setLocalTranslation(0, 0, 1);
        jCheckBox3.setTextVAlignment(VAlignment.Center);
        jCheckBox3.setOnView(new IconComponent("res/e.g.jme3hudl/GuiChecked.png", 1f, 5, 0, 1f, false ));
        jCheckBox3.setOffView(new IconComponent("res/e.g.jme3hudl/GuiUnchecked.png", 1f, 5, 0, 1f, false ));
        jCheckBox3.setChecked(true);

        jLabel5.setText("Other");
        jLabel5.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jLabel5.setFontSize(15);
        jLabel5.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jLabel5.setLocalTranslation(0, 0, 1);
        jLabel5.setTextVAlignment(VAlignment.Center);
        jLabel5.setPreferredSize(new Vector3f(71, 42, 0));

        jCheckBox4.setText("IOS");
        jCheckBox4.setPreferredSize(new Vector3f(72, 23, 0));
        jCheckBox4.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jCheckBox4.setFontSize(15);
        jCheckBox4.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jCheckBox4.setLocalTranslation(0, 0, 1);
        jCheckBox4.setTextVAlignment(VAlignment.Center);
        jCheckBox4.setOnView(new IconComponent("res/e.g.jme3hudl/GuiChecked.png", 1f, 5, 0, 1f, false ));
        jCheckBox4.setOffView(new IconComponent("res/e.g.jme3hudl/GuiUnchecked.png", 1f, 5, 0, 1f, false ));
        jCheckBox4.setChecked(true);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                                                        .addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jLabel4)
                                                        .addComponent(jLabel5)
                                                        .addComponent(jCheckBox2, GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                                                        .addComponent(jCheckBox1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jCheckBox3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jCheckBox4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGap(11, 11, 11)
                                                .addComponent(jLabel1)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel3)))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel4)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBox1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox2)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox3)
                                .addGap(33, 33, 33)
                                .addComponent(jLabel5)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox4)
                                .addContainerGap(295, Short.MAX_VALUE))
        );

        jLabel6.setText("GUI - Lemur & Jme3Hudl");
        jLabel6.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/CantarellExtraBold.fnt"));
        jLabel6.setFontSize(18);
        jLabel6.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jLabel6.setLocalTranslation(0, 0, 1);
        jLabel6.setTextHAlignment(HAlignment.Center);
        jLabel6.setTextVAlignment(VAlignment.Center);

        background = new QuadBackgroundComponent(GuiGlobals.getInstance().loadTexture("res/e.g.jme3hudl/Gui-Container.png", true, false));
        background.setColor(new ColorRGBA(0.314f, 0.322f, 0.329f, 1.0f));
        jPanel3.setBackground(background);
        jPanel3.setLocalTranslation(0, 0, 1);

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        jButton1.setText("Back");
        jButton1.setBackground(new QuadBackgroundComponent(GuiGlobals.getInstance().loadTexture("res/e.g.jme3hudl/Gui-ContainerRound.png", true, false)));
        //jButton1.setInsets(new Insets3f(new Vector3f(15, 3, 0), new Vector3f(15, 3, 0)));
        jButton1.setPreferredSize(new Vector3f(72, 25, 0));
        jButton1.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jButton1.setFontSize(15);
        jButton1.setLocalTranslation(0, 0, 1);
        jButton1.setTextVAlignment(VAlignment.Center);
        jButton1.setTextHAlignment(HAlignment.Center);
        jButton1.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));

        jButton2.setText("Next");
        jButton2.setBackground(new QuadBackgroundComponent(GuiGlobals.getInstance().loadTexture("res/e.g.jme3hudl/Gui-ContainerRound.png", true, false)));
        //jButton2.setInsets(new Insets3f(new Vector3f(15, 3, 0), new Vector3f(15, 3, 0)));
        jButton2.setPreferredSize(new Vector3f(72, 25, 0));
        jButton2.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jButton2.setFontSize(15);
        jButton2.setLocalTranslation(0, 0, 1);
        jButton2.setTextVAlignment(VAlignment.Center);
        jButton2.setTextHAlignment(HAlignment.Center);
        jButton2.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));

        jLabel7.setText("[ Title ]");
        jLabel7.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jLabel7.setFontSize(15);
        jLabel7.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jLabel7.setLocalTranslation(0, 0, 1);
        jLabel7.setTextVAlignment(VAlignment.Center);
        jLabel7.setPreferredSize(new Vector3f(45, 19, 4));

        background = new QuadBackgroundComponent(GuiGlobals.getInstance().loadTexture("res/e.g.jme3hudl/Gui-Container.png", true, false));
        background.setColor(new ColorRGBA(0.200f, 0.200f, 0.200f, 1.0f));
        jPanel4.setBackground(background);
        jPanel4.setPreferredSize(new Vector3f(100,100,0));
        jPanel4.setLocalTranslation(0, 0, 1);
        jPanel4.setLayout(new CardLayout());

        jLabel8.setText("OpenSource");
        jLabel8.setFont(GuiGlobals.getInstance().loadFont("res/e.g.jme3hudl/Fonts/Cantarell.fnt"));
        jLabel8.setFontSize(15);
        jLabel8.setPreferredSize(new Vector3f(80, 29, 0));
        jLabel8.setColor(new ColorRGBA(0.733f, 0.733f, 0.733f, 1.0f));
        jLabel8.setLocalTranslation(0, 0, 1);
        jLabel8.setTextHAlignment(HAlignment.Right);
        jLabel8.setTextVAlignment(VAlignment.Center);

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel7, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 333, Short.MAX_VALUE)
                                                .addComponent(jButton1)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton2))
                                        .addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel8, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel6, GroupLayout.PREFERRED_SIZE, 46, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton2)
                                        .addComponent(jButton1))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8)
                                .addContainerGap())
        );

        guiNode.attachChild(jPanel1);
        initialized = true;

        pack();
    }

    private void pack() {
        if (!initialized) {
            return;
        }

        int w = settings.getWidth(),
            h = settings.getHeight();

        jPanel1.setPreferredSize(new Vector3f(w, h,0));
        jPanel1.setLocalTranslation(w * 0.5f, h *0.5f, 0);
        jPanel1.move(-w * 0.5f, h * 0.5f, 0);
    }

    @Override
    public void reshape(int w, int h) {
        super.reshape(w, h);
        pack();
    }
}
