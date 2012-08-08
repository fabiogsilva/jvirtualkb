package com.github.fabiogsilva.jvirtualkb;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * @author Fábio Gomes
 * 
 */
public class VirtualKeyboard extends JDialog {

	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public static final int MODE_ALPHANUMERIC = 0;
	/**
	 *
	 */
	public static final int MODE_ALPHABETIC = 1;
	/**
	 *
	 */
	public static final int MODE_NUMERIC = 2;

	private static final char SIM_TAB = '-';
	private static final char SIM_CAPS_LOCK = '>';
	private static final char SIM_SHIFT = '<';
	private static final String KEYPAD = "-QWERTYUIOP>ASDFGHJKL\u2190<ZXCVBNM";
	private static final String NUMPAD = "6789234501,\u2190";

	private final Component source;
	private final Method eventProcessor;
	private final Color clrBtnBackgrChk = new Color(53, 147, 194);
	private Color clrBtnBackgrDef;
	private Button btnTab;
	private Button btnShift;
	private Button btnCapsLock;

	/**
	 * 
	 */
	private final ActionListener keyAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Button bt = (Button) e.getSource();
			String txt = bt.getText();
			if (bt == btnTab) {
				sendKey('\t');
			} else if (bt == btnCapsLock) {
				btnShift.release();
				bt.toggle();
			} else if (bt == btnShift) {
				btnCapsLock.release();
				bt.toggle();
			} else if (txt.equals("\u2190")) {
				sendKey('\b');
			} else {
				char ch = bt.getText().charAt(0);
				if (btnShift != null && btnCapsLock != null) {
					if (btnShift.isSelected()) {
						btnShift.release();
					} else if (!btnCapsLock.isSelected()) {
						ch = Character.toLowerCase(ch);
					}
				}
				sendKey(ch);
			}
		}
	};

	/**
	 * @param source
	 * @param mode
	 * @throws AWTException
	 */
	public VirtualKeyboard(Component source, int mode) throws AWTException {
		this.source = source;
		this.eventProcessor = getEventProcessor(source.getClass());

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setResizable(false);
		getContentPane().setBackground(new Color(65, 92, 131));
		setLayout(new FlowLayout());

		JPanel p1, p2 = null;

		if (mode != MODE_NUMERIC) {
			p1 = new JPanel(new GridLayout(3, 1));
			p1.setOpaque(false);
			for (int i = 0; i < KEYPAD.length(); i++) {
				if (i % 11 == 0) {
					if (i > 0) {
						p1.add(p2);
					}
					if (i == 22) {
						p2 = new JPanel(new GridLayout(1, 11));
					} else {
						p2 = new JPanel(new GridLayout(1, 11));
					}
					p2.setOpaque(false);
				}
				char ch = KEYPAD.charAt(i);
				Button btn = new Button(ch);
				if (ch == SIM_TAB) {
					btnTab = btn;
				} else if (ch == SIM_CAPS_LOCK) {
					btnCapsLock = btn;
				} else if (ch == SIM_SHIFT) {
					btnShift = btn;
				}
				p2.add(btn);
			}
			p2.add(new Button(' '));
			p1.add(p2);
			getContentPane().add(p1);
		}

		if (mode != MODE_ALPHABETIC) {
			p1 = new JPanel(new GridLayout(0, 4));
			p1.setOpaque(false);
			for (int i = 0; i < NUMPAD.length(); i++) {
				p1.add(new Button(NUMPAD.charAt(i)));
			}
			getContentPane().add(p1);
		}

		pack();
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
	}

	/**
	 *
	 */
	@Override
	public void setVisible(boolean b) {
		setLocation((int) source.getLocationOnScreen().getX(), (int) (source.getLocationOnScreen()
				.getY() + source.getSize().getHeight()));
		super.setVisible(b);
	}

	/**
	 * @author Fábio Gomes
	 */
	private class Button extends JButton {

		private static final long serialVersionUID = 1L;

		private Button(char ch) {
			String val;
			switch (ch) {
			case SIM_TAB:
				val = "TAB";
				break;
			case SIM_CAPS_LOCK:
				val = "CAPS";
				break;
			case SIM_SHIFT:
				val = "SHIFT";
				break;
			default:
				val = String.valueOf(ch);
				break;
			}
			setText(val);
			setPreferredSize(new Dimension(55, 50));
			setFont(new Font("Sans-Serif", Font.BOLD, 14));
			setMargin(new Insets(0, 0, 0, 0));
			addActionListener(keyAction);
			if (clrBtnBackgrDef == null) {
				clrBtnBackgrDef = getBackground();
			}
			setOpaque(true);
		}

		private boolean isHeld() {
			return getBackground().equals(clrBtnBackgrChk);
		}

		private void hold() {
			setBackground(clrBtnBackgrChk);
		}

		private void release() {
			setBackground(clrBtnBackgrDef);
		}

		private void toggle() {
			if (isHeld()) {
				release();
			} else {
				hold();
			}
		}

	}

	/**
	 * @param cl
	 */
	private Method getEventProcessor(Class<?> cl) {
		try {
			Method m = cl.getDeclaredMethod("processKeyEvent", KeyEvent.class);
			m.setAccessible(true);
			return m;
		} catch (NoSuchMethodException e) {
			cl = cl.getSuperclass();
			if (cl != null && !cl.equals(Object.class)) {
				return getEventProcessor(cl);
			} else
				return null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param ch
	 */
	private void sendKey(char ch) {
		KeyEvent evt;
		if (ch == '\b') {
			evt = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
					KeyEvent.VK_BACK_SPACE, '\b');
		} else if (ch == '\t') {
			evt = new KeyEvent(source, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
					KeyEvent.VK_TAB, '\t');
		} else {
			evt = new KeyEvent(source, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0,
					KeyEvent.VK_UNDEFINED, ch);
		}
		try {
			eventProcessor.invoke(source, evt);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
