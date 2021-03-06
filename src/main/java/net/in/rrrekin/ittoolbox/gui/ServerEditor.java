package net.in.rrrekin.ittoolbox.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import lombok.Getter;
import lombok.NonNull;
import net.in.rrrekin.ittoolbox.configuration.nodes.Server;
import net.in.rrrekin.ittoolbox.utilities.LocaleUtil;
import org.jetbrains.annotations.Nullable;

public class ServerEditor extends JDialog {

  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JButton resetButton;
  private JTextField nameField;
  private JTextField addressField;
  private JTextArea descriptionField;
  private JList servicesList;
  private JLabel iconLabel;
  private JButton changeButton;
  private JTable propertiesList;
  private JButton serviceAdd;
  private JButton serviceRemove;

  private @Nullable Server server = null;
  @Getter
  private boolean changed = false;

  public ServerEditor() {
    $$$setupUI$$$();
    setContentPane(contentPane);
    setModal(true);
    setMinimumSize(new Dimension(600, 400));
    setTitle(LocaleUtil.localMessage("SE_TITLE"));
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onOK();
          }
        });

    buttonCancel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onCancel();
          }
        });

    resetButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onReset();
          }
        });

    resetButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onIconChange();
          }
        });

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            onCancel();
          }
        });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            onCancel();
          }
        },
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  public void openForServer(final @NonNull Server server) {
    this.server = server;
    changed = false;
    populate();
    setVisible(true);
  }

  private void onReset() {
    populate();
  }

  private void onIconChange() {
    // TODO
  }

  private void onOK() {
    // validate
    setModelData();
    changed = true;
    setVisible(false);
  }

  private void onCancel() {
    // add your code here if necessary
    setVisible(false);
  }

  private void populate() {
    if (server == null) {
      nameField.setText("");
      addressField.setText("");
      descriptionField.setText("");
      servicesList.removeAll();
      propertiesList.removeAll();
      iconLabel.setIcon(null);
    } else {
      nameField.setText(server.getName());
      addressField.setText(server.getAddress());
      descriptionField.setText(server.getDescription());
      servicesList.removeAll();
      propertiesList.removeAll();
      iconLabel.setIcon(server.getIcon());
    }
  }

  private void setModelData() {
    server.setName(nameField.getText());
    server.setAddress(addressField.getText());
    server.setDescription(descriptionField.getText());
    // properties
    // services
    // icon
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR
   * call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    createUIComponents();
    contentPane = new JPanel();
    contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
    contentPane.setAutoscrolls(true);
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1,
      new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null,
        null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
      GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null,
      null, 0, false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
    panel1.add(panel2,
      new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
        null, 0, false));
    buttonOK = new JButton();
    this.$$$loadButtonText$$$(buttonOK, ResourceBundle.getBundle("Messages").getString("APP_OK"));
    panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
      GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    buttonCancel = new JButton();
    this.$$$loadButtonText$$$(buttonCancel,
      ResourceBundle.getBundle("Messages").getString("APP_CANCEL"));
    panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
      GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    resetButton = new JButton();
    this.$$$loadButtonText$$$(resetButton,
      ResourceBundle.getBundle("Messages").getString("APP_RESET"));
    panel1.add(resetButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
      GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(6, 5, new Insets(0, 0, 0, 0), -1, -1));
    panel3.setAutoscrolls(true);
    contentPane.add(panel3,
      new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
        null, 0, false));
    final JLabel label1 = new JLabel();
    this.$$$loadLabelText$$$(label1,
      ResourceBundle.getBundle("Messages").getString("ED_NAME_LABEL"));
    panel3.add(label1,
      new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
        false));
    nameField = new JTextField();
    panel3.add(nameField, new GridConstraints(0, 1, 1, 4, GridConstraints.ANCHOR_WEST,
      GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
      GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label2 = new JLabel();
    this.$$$loadLabelText$$$(label2,
      ResourceBundle.getBundle("Messages").getString("ED_ADDRESS_LABEL"));
    panel3.add(label2,
      new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
        false));
    addressField = new JTextField();
    panel3.add(addressField, new GridConstraints(1, 1, 1, 4, GridConstraints.ANCHOR_WEST,
      GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
      GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label3 = new JLabel();
    this.$$$loadLabelText$$$(label3,
      ResourceBundle.getBundle("Messages").getString("ED_DESCRIPTION_LABEL"));
    panel3.add(label3,
      new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
        false));
    final JLabel label4 = new JLabel();
    this.$$$loadLabelText$$$(label4,
      ResourceBundle.getBundle("Messages").getString("ED_PROPERTIES_LABEL"));
    panel3.add(label4,
      new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
        false));
    final JLabel label5 = new JLabel();
    this.$$$loadLabelText$$$(label5,
      ResourceBundle.getBundle("Messages").getString("ED_SERVICES_LABEL"));
    panel3.add(label5,
      new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
        false));
    final JLabel label6 = new JLabel();
    this.$$$loadLabelText$$$(label6,
      ResourceBundle.getBundle("Messages").getString("ED_ICON_LABEL"));
    panel3.add(label6,
      new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
        false));
    changeButton = new JButton();
    this.$$$loadButtonText$$$(changeButton,
      ResourceBundle.getBundle("Messages").getString("ED_CHANGE_ICON_BTN"));
    panel3.add(changeButton, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER,
      GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
      false));
    final Spacer spacer2 = new Spacer();
    panel3.add(spacer2, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_CENTER,
      GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null,
      null, 0, false));
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel3.add(panel4,
      new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
        null, 0, false));
    panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
    iconLabel = new JLabel();
    iconLabel.setHorizontalAlignment(0);
    iconLabel.setHorizontalTextPosition(0);
    iconLabel.setIconTextGap(0);
    iconLabel.setText("");
    panel4.add(iconLabel,
      new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
        new Dimension(24, 24), null, 0, false));
    final JScrollPane scrollPane1 = new JScrollPane();
    panel3.add(scrollPane1,
      new GridConstraints(2, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null,
        null, null, 0, false));
    descriptionField = new JTextArea();
    descriptionField.setLineWrap(true);
    descriptionField.setWrapStyleWord(true);
    scrollPane1.setViewportView(descriptionField);
    servicesList = new JList();
    panel3.add(servicesList,
      new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null,
        new Dimension(150, 50), null, 0, false));
    propertiesList = new JTable();
    panel3.add(propertiesList,
      new GridConstraints(3, 1, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null,
        new Dimension(150, 50), null, 0, false));
    final JPanel panel5 = new JPanel();
    panel5.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel3.add(panel5, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_NORTH,
      GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
      null, 0, false));
    serviceAdd.setBorderPainted(false);
    serviceAdd.setContentAreaFilled(false);
    serviceAdd.setFocusPainted(false);
    serviceAdd.setHideActionText(true);
    serviceAdd.setIconTextGap(0);
    serviceAdd.setMargin(new Insets(1, 1, 1, 1));
    serviceAdd.setText("");
    serviceAdd
      .setToolTipText(ResourceBundle.getBundle("Messages").getString("ED_ADD_SERVICE.tooltip"));
    panel5.add(serviceAdd,
      new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null,
        new Dimension(24, 24), null, 0, false));
    serviceRemove.setBorderPainted(false);
    serviceRemove.setContentAreaFilled(false);
    serviceRemove.setFocusPainted(false);
    serviceRemove.setHideActionText(true);
    serviceRemove.setIconTextGap(0);
    serviceRemove.setMargin(new Insets(1, 1, 1, 1));
    serviceRemove.setText("");
    serviceRemove.setToolTipText(
      ResourceBundle.getBundle("Messages").getString("ED_REMOVE_SERVICE.tooltip"));
    panel5.add(serviceRemove,
      new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadLabelText$$$(JLabel component, String text) {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '&') {
        i++;
        if (i == text.length()) {
          break;
        }
        if (!haveMnemonic && text.charAt(i) != '&') {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic) {
      component.setDisplayedMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadButtonText$$$(AbstractButton component, String text) {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '&') {
        i++;
        if (i == text.length()) {
          break;
        }
        if (!haveMnemonic && text.charAt(i) != '&') {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic) {
      component.setMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return contentPane;
  }

  private void createUIComponents() {
    serviceAdd = new JButton();
    serviceAdd.setIcon(IconFontSwing.buildIcon(GoogleMaterialDesignIcons.ADD_CIRCLE_OUTLINE, 24));

    serviceRemove = new JButton();
    serviceRemove.setIcon(
        IconFontSwing.buildIcon(GoogleMaterialDesignIcons.REMOVE_CIRCLE_OUTLINE, 24));
  }
}
