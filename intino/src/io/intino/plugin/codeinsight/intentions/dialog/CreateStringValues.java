package io.intino.plugin.codeinsight.intentions.dialog;

import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import io.intino.plugin.lang.psi.impl.IntinoUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static com.intellij.openapi.util.io.FileUtilRt.getNameWithoutExtension;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.getResourcesRoot;

public class CreateStringValues extends JDialog {
	private static final String PROPERTIES = ".properties";
	private static final String MESSAGES = "messages";
	private static final String DEFAULT = "Default";
	private final String outputDsl;
	private String key;
	private JPanel contentPane;
	private JButton OKButton;
	private JButton cancelButton;
	private JButton newLanguage;
	private JPanel valuesPanel;
	private Map<JComponent, JBTextField> fields = new LinkedHashMap<>();
	private File messagesDirectory;
	private GridBagConstraints constraints = new GridBagConstraints();

	public CreateStringValues(PsiElement element, String key) {
		this.outputDsl = IntinoUtil.modelPackage(element);
		this.OKButton.addActionListener(e -> onOK());
		this.newLanguage.addActionListener(e -> onNewLanguage());
		this.cancelButton.addActionListener(e -> onCancel());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});
		this.messagesDirectory = new File(getResourcesRoot(element).getPath(), MESSAGES);
		this.key = key;
		this.contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		initLanguages();
		setContentPane(contentPane);
		setModal(true);
		setResizable(true);
		setTitle("Set i18n values");
		setLocationByPlatform(true);
		setLocationRelativeTo(this.getParent());
		getRootPane().setDefaultButton(OKButton);
	}


	private void onOK() {
		save();
		dispose();
	}

	private void save() {
		for (Map.Entry<JComponent, JBTextField> entry : fields.entrySet()) {
			final File inFile = new File(messagesDirectory, outputDsl + lang(entry) + PROPERTIES);
			if (!inFile.exists() && !createNewFile(inFile)) continue;
			put(entry.getValue().getText(), inFile);
		}
	}

	private String lang(Map.Entry<JComponent, JBTextField> entry) {
		final String name = getText(entry.getKey());
		return name.equals(DEFAULT) ? "" : "_" + name;
	}

	private String getText(JComponent key) {
		return key instanceof JBLabel ? ((JBLabel) key).getText() : ((JBTextField) key).getText();
	}

	private void onNewLanguage() {
		final JBTextField value = new JBTextField();
		final JBTextField newLanguage = new JBTextField("New Language");
		fields.put(newLanguage, value);
		valuesPanel.add(newLanguage, getLanguageConstraints(fields.size() - 1));
		valuesPanel.add(value, getValueConstraints(fields.size() - 1));
		pack();
		repaint();
		newLanguage.requestFocus();
		newLanguage.selectAll();
	}

	private void put(String value, File file) {
		try {
			Properties p = loadResource(file);
			p.put(key, value);
			p.store(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")), getNameWithoutExtension(file.getName()) + " messages");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean createNewFile(File file) {
		try {
			file.getParentFile().mkdirs();
			return file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void onCancel() {
		dispose();
	}

	private void initLanguages() {
		if (!messagesDirectory.exists() || messagesDirectory.listFiles((dir, name) -> name.endsWith(PROPERTIES)).length == 0) {
			defaultLanguage();
			return;
		}
		for (File messageFile : messagesDirectory.listFiles((dir, name) -> name.endsWith(PROPERTIES))) {
			final JBLabel jbLabel = new JBLabel(lang(messageFile));
			fields.put(jbLabel, new JBTextField(getValueFrom(messageFile)));
		}
		int i = 0;
		for (Map.Entry<JComponent, JBTextField> entry : fields.entrySet()) {
			valuesPanel.add(entry.getKey(), getLanguageConstraints(i));
			valuesPanel.add(entry.getValue(), getValueConstraints(i));
			i++;
		}
		pack();
		repaint();
	}

	private void defaultLanguage() {
		final JBTextField value = new JBTextField();
		final JBLabel defaultLang = new JBLabel(DEFAULT);
		fields.put(defaultLang, value);
		valuesPanel.add(defaultLang, getLanguageConstraints(0));
		valuesPanel.add(value, getValueConstraints(0));
		repaint();
		value.requestFocus();
	}

	private GridBagConstraints getLanguageConstraints(int y) {
		constraints.gridx = 0;
		constraints.gridy = y;
		constraints.weightx = 0.3;
		constraints.insets = new Insets(5, 0, 0, 10);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		return constraints;
	}

	private GridBagConstraints getValueConstraints(int y) {
		constraints.gridx = 1;
		constraints.gridy = y;
		constraints.weightx = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		return constraints;
	}

	private String getValueFrom(File file) {
		try {
			Properties p = loadResource(file);
			final Object o = p.get(key);
			return o != null ? o.toString() : "";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	private Properties loadResource(File file) throws IOException {
		Properties p = new Properties();
		p.load(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
		return p;
	}

	private String lang(File messageFile) {
		final String name = messageFile.getName();
		return getNameWithoutExtension(name.contains("_") ? name.split("_")[1] : DEFAULT);
	}
}
