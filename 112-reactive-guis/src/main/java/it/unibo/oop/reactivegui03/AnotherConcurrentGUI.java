package it.unibo.oop.reactivegui03;

import java.awt.Desktop.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel display = new JLabel();

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        
        final JButton up = new JButton("up");
        panel.add(up);
        final JButton down = new JButton("down");
        panel.add(down);
        final JButton stop = new JButton("stop");
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);

        final Agent agent = new Agent();
        new Thread(agent).start();
        stop.addActionListener(e -> { agent.stopCounting();
            up.setEnabled(false);
            down.setEnabled(false);
         } );

        new Thread(new TimerAgent(() -> {
            agent.stopCounting();
            up.setEnabled(false);
            down.setEnabled(false);
        })).start();
        up.addActionListener(e -> agent.setFlagUpDown(true));
        down.addActionListener(e -> agent.setFlagUpDown(false));
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class Agent implements Runnable {

        private volatile boolean stop;
        private volatile boolean flagUpDown;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    this.counter += (flagUpDown ? 1 : -1);
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        /**
         * 
         * @param isUp set true for flag up active false flag down active
         */
        public void setFlagUpDown(boolean isUp)
        {
            flagUpDown = isUp;
        }
    }

    private final class TimerAgent implements Runnable {

        private final Runnable action;
        

        public TimerAgent(Runnable action) {
            this.action = action;
        }


        @Override
        public void run() {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            action.run();
        }
        
    }

}
