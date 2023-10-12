import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import parser.MathExpression;

public class Calculator extends JFrame implements ActionListener {
    private JTextField txt;
    private JButton[][] ops;
    private static final int w = 4, h = 5, size = 50;
    public Calculator() {

        txt = new JTextField();
        txt.setPreferredSize(new Dimension(70, 70));
        txt.setEnabled(true);
        txt.setCaretColor(Color.WHITE);
        txt.setFont(new Font("SansSerif", Font.BOLD, 60));
        txt.setHorizontalAlignment(JTextField.RIGHT);
        txt.setAlignmentY(-2.0F);
        txt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                // perhaps a bit too short to be a switch statement, but I fear
                // it might get bigger eventually as new problems arise
                String temp = txt.getText();
                switch (keyCode) {
                    case KeyEvent.VK_ENTER: 
                        if (temp.matches("[a-zA-Z ]+")) {
                            txt.setText("");
                            return;
                        }
                        if (temp.length() == 1 && temp.matches("[-+/*]+")) {
                            txt.setText("SYNTAX ERROR");
                            return;
                        }
                        txt.setText(new MathExpression(temp).solve());
                        break;

                    case KeyEvent.VK_BACK_SPACE:
                        if (temp.matches("[a-zA-Z ]+")) {
                            txt.setText("");
                            return;
                        }
                        break;                
                    default:
                        e.consume();
                        break;
                }
                txt.setCaretPosition(txt.getText().length());
            }
        });
        txt.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException{
                if (str == null || (str.matches("[^0-9/*+.=]") && !str.equals("-"))) {
                    //System.out.println(str);
                    return;
                }

                int thislen = getLength();
                
                boolean err = false;
                if (super.getText(0, thislen).matches("[a-zA-Z ]+")) {
                    super.remove(0, thislen);
                    offs = 0;
                    err = true;
                }

                if (str.equals("=")) {
                    if (err) {
                        return;
                    }
                    String temp = super.getText(0, thislen);
                    if (temp.length() == 1 && temp.matches("[-+/*]+")) {
                        txt.setText("SYNTAX ERROR");
                        return;
                    }
                    super.remove(0, thislen);
                    str = (new MathExpression(temp).solve());
                    offs = 0;
                }

                int len = (getLength() + str.length());
                if (len <= 20) {
                    if (len < 10) {
                        txt.setFont(new Font("SansSerif", Font.BOLD, 60));
                    } else if (len < 16) {
                        txt.setFont(new Font("SansSerif", Font.BOLD, 35));
                    } else {
                        txt.setFont(new Font("SansSerif", Font.BOLD, 25));
                    }
                    super.insertString(offs, str, a);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        JPanel operations = new JPanel(new GridLayout(h, w));
        ops = new JButton[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                ops[i][j] = new JButton();
                ops[i][j].addActionListener(this);
                ops[i][j].setPreferredSize(new Dimension(size, size));
                operations.add(ops[i][j]);
            }
        }
        int k = 1;
        for (int i = 3; i > 0; i--) {
            for (int j = 0; j < 3; j++) {
                this.ops[i][j].setText(String.format("%d", k++));
            }
        }
        this.ops[4][1].setText(String.format("%d", 0));
        String[] op = new String[]{"%", "CE", "C", "/"};
        // for (int i = 0; i < 2; i++) {
        //     for (int j = 0; j < 4; j++) {
        //         this.ops[i][j].setText(op[i][j]);
        //     }
        // }
        for (int i = 0; i < 4; i++) {
            ops[0][i].setText(op[i]);
        }
        ops[1][3].setText("X");
        ops[2][3].setText("-");
        ops[3][3].setText("+");
        ops[4][3].setText("=");
        ops[4][0].setText("+/-");
        ops[4][2].setText(","); 

        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.PAGE_AXIS));
        pan.add(Box.createRigidArea(new Dimension(7, 7)));
        pan.add(txt);
        pan.add(Box.createVerticalGlue());
        pan.add(operations);
        pan.add(Box.createVerticalGlue());
        
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }
        this.setTitle("Really Simple Calculator");
        this.getContentPane().add(pan);
        //this.pack();
        this.setSize(350, 450);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private int lastIndexOfOp(String str) {
        int plus = str.lastIndexOf('+');
        int minus = str.lastIndexOf('-');
        int div = str.lastIndexOf('/');
        int times = str.lastIndexOf('*');

        return Math.max(Math.max(Math.max(div,times), minus), plus);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String buttext = ((JButton) e.getSource()).getText();
        String txttext = txt.getText();
        switch (buttext) {
            case "X":
                txt.setText(txttext + "*");
                break;
            case ",":
                txt.setText(txttext + ".");
                break;
            case "%":
                if (txttext.matches("[0-9]+")) {
                    txt.setText(String.valueOf(Double.parseDouble(txttext) / 100));
                }
                break;
            case "CE":
                int idx = lastIndexOfOp(txttext);
                if (idx >= 0) {
                    txt.setText(txttext.substring(0, idx));
                    break;
                }
            case "C":
                txt.setText("");
                break;
            case "+/-":
                if (txttext.isEmpty()) {
                    break;
                }
                txt.setText("-(" + txttext + ")");
                break;
            case "=":
                txt.setText((new MathExpression(txttext)).solve());
                break;
            default:
                if (txttext.matches("[a-zA-Z ]+")) {
                    txttext = "";
                }
                txt.setText(txttext + buttext);
                break;
        }
        txt.grabFocus();
    }
}
