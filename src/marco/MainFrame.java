package marco;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        System.out.println("Created GUI on EDT? "+
                SwingUtilities.isEventDispatchThread());
        JFrame f = new JFrame("JavaFX/Swing 3D app");

        f.getContentPane().setLayout(new BorderLayout());
        ThreeDeePanel fxPanel = new ThreeDeePanel();
        fxPanel.initState("pallet", "tire");
        f.getContentPane().add(fxPanel, BorderLayout.CENTER);
        f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        MigLayout layout = new MigLayout("wrap 1");


        JPanel buttons = new JPanel(layout);
        JButton btnMoveIn = new JButton("Move in");
        btnMoveIn.addActionListener(e -> fxPanel.movePalletIn());
        JButton btnMoveOut = new JButton("Move out");
        btnMoveOut.addActionListener(e -> fxPanel.movePalletOut());
        JButton btnMoveOutIn = new JButton("Move out and in");
        btnMoveOutIn.addActionListener(e -> fxPanel.movePalletOutIn("pallet", "tire"));
        buttons.add(btnMoveIn);
        buttons.add(btnMoveOut);
        buttons.add(btnMoveOutIn);

        fxPanel.setDebugBoxVisible(true);
        f.getContentPane().add(buttons, BorderLayout.EAST);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(250,250);
        f.setVisible(true);
    }

}
