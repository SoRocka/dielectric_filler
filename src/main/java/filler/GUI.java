package filler;

import filler.entities.Figure;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static filler.utils.GCodeUtils.G_CODE_UTILS;
import static filler.utils.ModelParameters.MODEL_SETTINGS;

public class GUI extends JFrame {
    private JPanel panel1;
    public JSpinner wx;
    public JSpinner wy;
    public JSpinner wz;
    public JSpinner defaultCellSize;
    public JSpinner layerHeight;
    public JSpinner epsilonZero;
    public JSpinner epsilonMinimum;
    public JSpinner extruderSize;
    public JSpinner extruderTemperature;
    public JSpinner tableTemperature;
    public JButton buttonCalculate;
    public JComboBox comboBox1;
    public JLabel statusBar;
    private JSpinner fwdExtr;
    private JSpinner bckExtr;

    private SpinnerNumberModel m_wx;
    private SpinnerNumberModel m_wy;
    private SpinnerNumberModel m_wz;
    private SpinnerNumberModel m_layer_height;
    private SpinnerNumberModel m_default_cell;
    private SpinnerNumberModel m_extruder_size;
    private SpinnerNumberModel m_epsilon_0;
    private SpinnerNumberModel m_epsilon_minimum;
    private SpinnerNumberModel m_extruder_temp;
    private SpinnerNumberModel m_table_temp;
    private SpinnerNumberModel fwd;
    private SpinnerNumberModel bck;

    public GUI() {
        setContentPane(panel1);
        panel1.setBorder(new EmptyBorder(20, 10, 10, 10));
        setSize(430, 280);
        setResizable(false);
        setTitle("Dielectric filler");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setSpinnerModels();
        buttonCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (saveSettings()) {
                    statusBar.setText("Error. Epsilon rate over 1.0. Choose another material (Epsilon 0).");
                    return;
                }
                if (comboBox1.getSelectedIndex() == 0) {
                    statusBar.setText("Calculating... Pleas wait.");
                    G_CODE_UTILS.setDefaultAValue();
                    Figure figure = new Figure();
                    figure.makePyramid();
                    statusBar.setText("Done");
                }
            }
        });
    }

    private boolean saveSettings() {
        MODEL_SETTINGS.setWidthX(m_wx.getNumber().doubleValue());
        MODEL_SETTINGS.setWidthY(m_wy.getNumber().doubleValue());
        MODEL_SETTINGS.setWidthZ(m_wz.getNumber().doubleValue());

        MODEL_SETTINGS.setDefaultCellSize(m_default_cell.getNumber().doubleValue());
        MODEL_SETTINGS.setLayerHeight(m_layer_height.getNumber().doubleValue());
        MODEL_SETTINGS.setEpsilonZero(m_epsilon_0.getNumber().doubleValue());
        MODEL_SETTINGS.setEpsilonMinimum(m_epsilon_minimum.getNumber().doubleValue());

        MODEL_SETTINGS.setExtruderSize(m_extruder_size.getNumber().doubleValue());
        MODEL_SETTINGS.setExtruderTemp(m_extruder_temp.getNumber().intValue());
        MODEL_SETTINGS.setTableTemp(m_table_temp.getNumber().intValue());

        MODEL_SETTINGS.setFwd(fwd.getNumber().doubleValue());
        MODEL_SETTINGS.setBck(bck.getNumber().doubleValue());

        // Calculating Epsilon rate
        double w = Math.pow(MODEL_SETTINGS.getWidthX() / 2, 2) + Math.pow(MODEL_SETTINGS.getWidthY() / 2, 2);
        double h_sq = Math.pow(MODEL_SETTINGS.getWidthZ(), 2);
        double cos_2 = h_sq / (h_sq + w);
        MODEL_SETTINGS.setEpsilonRate(MODEL_SETTINGS.getEpsilonMinimum() / cos_2 / MODEL_SETTINGS.getEpsilonZero());

        return MODEL_SETTINGS.getEpsilonRate() > 1;
    }

    private void setSpinnerModels() {
        m_wx = new SpinnerNumberModel(36.0, 0.0, 1000, 0.05);
        m_wy = new SpinnerNumberModel(28.0, 0.0, 1000, 0.05);
        m_wz = new SpinnerNumberModel(36.0, 0.0, 1000, 0.05);

        m_layer_height = new SpinnerNumberModel(0.2, 0.1, 0.6, 0.1);
        m_default_cell = new SpinnerNumberModel(3.6, 2.0, 6.0, 0.1);
        m_epsilon_0 = new SpinnerNumberModel(2.400, 1, 100, 0.01);
        m_epsilon_minimum = new SpinnerNumberModel(1.3, 1, 100, 0.01);

        m_extruder_size = new SpinnerNumberModel(0.4, 0.1, 1, 0.1);
        m_extruder_temp = new SpinnerNumberModel(220, 80, 350, 1);
        m_table_temp = new SpinnerNumberModel(130, 50, 200, 1);

        fwd = new SpinnerNumberModel(0.0, 0.0, 10.0, 0.001);
        bck = new SpinnerNumberModel(0.0001, 0.0, 10.0, 0.001);

        fwdExtr.setModel(fwd);
        bckExtr.setModel(bck);

        wx.setModel(m_wx);
        wy.setModel(m_wy);
        wz.setModel(m_wz);

        defaultCellSize.setModel(m_default_cell);
        extruderSize.setModel(m_extruder_size);
        epsilonZero.setModel(m_epsilon_0);
        epsilonMinimum.setModel(m_epsilon_minimum);

        extruderTemperature.setModel(m_extruder_temp);
        tableTemperature.setModel(m_table_temp);
        layerHeight.setModel(m_layer_height);
    }

}
