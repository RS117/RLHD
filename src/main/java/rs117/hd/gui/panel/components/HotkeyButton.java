package rs117.hd.gui.panel.components;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.*;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.ui.FontManager;
import rs117.hd.HdPlugin;

public class HotkeyButton extends JButton implements KeyListener
{
    @Getter
    private Keybind value;

    @Getter
    @Setter
    private String key;

    @Setter
    private Consumer consumer;

    @Getter
    @Setter
    private HdPlugin plugin;

    public HotkeyButton(Keybind value, String key, Consumer consumer)
    {

        setValue(value);
        setConsumer(consumer);
        setKey(key);
        setFont(FontManager.getDefaultFont().deriveFont(12.f));
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                // We have to use a mouse adapter instead of an action listener so the press action key (space) can be bound
                setValue(Keybind.NOT_SET);
            }
        });

        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                setValue(new Keybind(e));
            }
        });
    }

    public void init()
    {
        String keyFormatted = key.replace(" ","_").toUpperCase();
        Keybind bind = null;

        if (plugin.getConfigManager().getConfiguration("117Debug",keyFormatted) != null) {
            bind = plugin.getConfigManager().getConfiguration("117Debug", keyFormatted, Keybind.class);
        }
        setValue(bind == null ? value : bind);
    }

    public void setValue(Keybind value)
    {
        if (value == null)
        {
            value = Keybind.NOT_SET;
        }

        this.value = value;
        setText(value.toString());
    }

    @Override
    public void keyPressed(KeyEvent event)
    {
        if(consumer != null && value.matches(event)) {
            consumer.accept(null);
            plugin.getConfigManager().setConfiguration("117Debug",key.replace(" ","_").toUpperCase(),value);
        }
    }

    @Override
    public void keyReleased(KeyEvent event)
    {

    }

    @Override
    public void keyTyped(KeyEvent event)
    {

    }

}