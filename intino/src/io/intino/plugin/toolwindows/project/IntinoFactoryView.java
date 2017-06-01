package io.intino.plugin.toolwindows.project;

import com.intellij.ide.DataManager;
import com.intellij.ide.ui.LafManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.util.ui.UIUtil;
import io.intino.plugin.actions.ExportAction;
import io.intino.plugin.actions.IntinoGenerationAction;
import io.intino.plugin.actions.PurgeAndReloadConfigurationAction;
import io.intino.plugin.actions.ReloadConfigurationAction;
import io.intino.plugin.build.ArtifactBuilder;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.lang.psi.TaraModel;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.intino.plugin.toolwindows.project.IntinoFactoryView.FactoryPanel.Label.BoxLanguage;
import static io.intino.plugin.toolwindows.project.IntinoFactoryView.FactoryPanel.Label.ModelLanguage;
import static io.intino.plugin.toolwindows.project.IntinoFactoryView.FactoryPanel.Mode.Darcula;
import static io.intino.plugin.toolwindows.project.IntinoFactoryView.FactoryPanel.Mode.Light;
import static io.intino.plugin.toolwindows.project.IntinoFactoryView.FactoryPanel.Operation.*;
import static io.intino.plugin.toolwindows.project.IntinoFactoryView.FactoryPanel.Product.*;
import static java.awt.event.ActionEvent.SHIFT_MASK;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.stream;

public class IntinoFactoryView extends JPanel {
	private JPanel contentPane;
	private JPanel factoryContainerPanel;
	private JLabel moduleLabel;
	private Project project;
	private Instant lastAction;

	IntinoFactoryView(Project project) {
		this.project = project;
		this.lastAction = Instant.now();
		this.contentPane.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				final Module module = selectedModule();
				if (module != null) moduleLabel.setText(module.getName());
			}
		});
		LafManager.getInstance().addLafManagerListener(source -> {
			mode(source.getCurrentLookAndFeel().getName().equalsIgnoreCase("darcula"));
			source.repaintUI();
		});
	}

	private void generateCode(int modifiers) {
		if (isRecurrent()) return;
		lastAction = Instant.now();
		final IntinoGenerationAction action = new IntinoGenerationAction();
		if ((modifiers & ActionEvent.SHIFT_MASK) != 0) {
			action.execute(selectedModule());
		}action.force(selectedModule());

	}

	private void build() {
		if (isRecurrent()) return;
		lastAction = Instant.now();
		final CompilerManager compilerManager = CompilerManager.getInstance(project);
		CompileScope scope = compilerManager.createModulesCompileScope(new Module[]{selectedModule()}, true);
		compilerManager.make(scope, null);
	}

	private void build(FactoryPanel.Operation operation, int modifiers) {
		if (isRecurrent()) return;
		lastAction = Instant.now();
		FactoryPhase phase = phaseOf(operation, (modifiers & ActionEvent.SHIFT_MASK) != 0);
		if (phase == null) return;
		Module module = selectedModule();
		if (module != null) new ArtifactBuilder(project, Collections.singletonList(module), phase).build();
		else Notifications.Bus.notify(new Notification("Tara Language",
				phase.gerund() + " artifact", "Impossible identify module scope", NotificationType.ERROR));

	}

	private void exportAccessors() {
		if (isRecurrent()) return;
		lastAction = Instant.now();
		new ExportAction().execute(selectedModule());
	}

	private FactoryPhase phaseOf(FactoryPanel.Operation operation, boolean shift) {
		switch (operation) {
			case PackArtifact:
				return FactoryPhase.PACKAGE;
			case DistributeArtifact:
				return shift ? FactoryPhase.INSTALL : FactoryPhase.DISTRIBUTE;
			case DeployArtifact:
				return FactoryPhase.DEPLOY;
		}
		return null;
	}

	private boolean isRecurrent() {
		return Instant.now().minus(5, SECONDS).isBefore(lastAction);
	}

	private void mode(boolean underDarcula) {
		((FactoryPanel) factoryContainerPanel).mode(underDarcula ? Darcula : Light);
	}

	private void reload(int modifiers) {
		if (isRecurrent()) return;
		lastAction = Instant.now();
		if (((modifiers & SHIFT_MASK) == SHIFT_MASK)) new PurgeAndReloadConfigurationAction().execute(selectedModule());
		else new ReloadConfigurationAction().execute(selectedModule());
	}

	private void createUIComponents() {
		factoryContainerPanel = new FactoryPanel();
		((FactoryPanel) factoryContainerPanel).addActionListener(GenerateCode, e -> generateCode(e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(ImportPackages, e -> reload(e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(BuildArtifact, e -> build());
		((FactoryPanel) factoryContainerPanel).addActionListener(PackArtifact, e -> build(PackArtifact, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(ExportAccessors, e -> exportAccessors());
		((FactoryPanel) factoryContainerPanel).addActionListener(DistributeArtifact, e -> build(DistributeArtifact, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(DeployArtifact, e -> build(DeployArtifact, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Src, e -> navigate(Src, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Gen, e -> navigate(Src, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Imports, e -> navigate(Imports, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Model, e -> navigate(Model, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Box, e -> navigate(Box, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Pack, e -> navigate(Pack, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Dist, e -> navigate(Dist, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Deploy, e -> navigate(Deploy, e.getModifiers()));
		mode(UIUtil.isUnderDarcula());
	}

	private void navigate(FactoryPanel.Product product, int modifiers) {
		final Configuration configuration = TaraUtil.configurationOf(selectedModule());
		if (configuration == null) return;
		TaraModel model = ((TaraModel) ((LegioConfiguration) configuration).legioFile());
		final Node artifact = model.components().stream().filter(n -> n.type().endsWith("Artifact")).findAny().orElse(null);
		if (artifact == null) return;
		Node node;
		switch (product) {
			case Src:
				node = find(artifact, "Code");
				if (node != null) ((Navigatable) node).navigate(true);
				break;
			case Pack:
				node = find(artifact, "Package");
				if (node != null) ((Navigatable) node).navigate(true);
				break;
			case Dist:
				node = find(artifact, "Distribution");
				if (node != null) ((Navigatable) node).navigate(true);
				break;
			default:
				node = find(artifact, product.name());
				if (node != null) ((Navigatable) node).navigate(true);
				break;
		}
	}

	private Node find(Node artifact, String type) {
		return artifact.components().stream().filter(n -> n.type().endsWith(type)).findAny().orElse(null);
	}

	private Module selectedModule() {
		final DataContext resultSync = DataManager.getInstance().getDataContextFromFocus().getResultSync();
		return resultSync != null ? resultSync.getData(LangDataKeys.MODULE) : null;
	}

	Component contentPane() {
		return contentPane;
	}

	public static class FactoryPanel extends JPanel {
		private Image image;
		private int imageSize;
		private int offsetX;
		private static final int offsetY = 100;
		private double scale;
		private Map<Operation, JImageButton> buttons = new HashMap<>();
		private Map<Product, ActionListener> listeners = new HashMap<>();
		private Map<Label, String[]> labels = new HashMap<>();
		private Mode mode;

		public FactoryPanel() {
			this.setLayout(null);
			this.operations().forEach(this::createButton);
			this.addMouseListener(listener());
		}

		public Mode mode() {
			return mode;
		}

		public FactoryPanel mode(Mode mode) {
			this.mode = mode;
			this.refresh();
			return this;
		}

		private Stream<Operation> operations() {
			return stream(Operation.values());
		}

		private Stream<Product> products() {
			return stream(Product.values());
		}

		private Stream<Label> labels() {
			return stream(Label.values());
		}

		private Component createButton(Operation operation) {
			JImageButton button = new JImageButton(operation);
			buttons.put(operation, button);
			this.add(button);
			return button;
		}

		public void addActionListener(Operation operation, ActionListener listener) {
			buttons.get(operation).addActionListener(listener);
		}

		public void addActionListener(Product product, ActionListener listener) {
			listeners.put(product, listener);
		}

		@Override
		public void paint(Graphics g) {
			setScale();
			g.setColor(backgroundColor());
			drawImage(g);
			products().forEach(p -> paint(g, p));
			labels().forEach(l -> paint(g, l));
			buttons.values().forEach(this::setLocation);
			paintComponents(g);
		}

		private void paint(Graphics g, Product product) {
			g.setFont(productFont());
			g.setColor(foregroundColor());
			FontMetrics metrics = g.getFontMetrics(productFont());
			Rectangle rect = product.rect;

			String name = product.name().toLowerCase();
			int x = offsetX + (scale(rect.x) + (scale(rect.width) - metrics.stringWidth(name)) / 2);
			int y = offsetY + (scale(rect.y) + ((scale(rect.height) - metrics.getHeight()) / 2 + metrics.getAscent()));
			g.drawString(name, x, y);
		}

		private void paint(Graphics g, Label label) {
			g.setFont(labelFont());
			g.setColor(foregroundColor());
			FontMetrics metrics = g.getFontMetrics(labelFont());
			Rectangle rect = label.product.rect;

			int index = -80;
			for (int i = 0; i < 2; i++) {
				final String[] strings = labels.get(label);
				if (strings == null) continue;
				int x = offsetX + (scale(rect.x) + (scale(rect.width) - metrics.stringWidth(strings[i])) / 2);
				int y = offsetY + (scale(rect.y + index) - metrics.getHeight());
				g.drawString(strings[i], x, y);
				index += 200;
			}
		}

		private Font labelFont() {
			return new Font("Arial", Font.BOLD, scale(180));
		}

		private Font productFont() {
			return new Font("Arial", Font.PLAIN, scale(200));
		}

		private Color backgroundColor() {
			return mode == Light ? Light.color : Darcula.color;
		}

		private Color foregroundColor() {
			return mode == Light ? Color.black : Color.white;
		}

		private void setLocation(JImageButton button) {
			Rectangle rect = button.operation.rect;
			button.setBounds(scale(rect.x) + offsetX, scale(rect.y) + offsetY, scale(rect.width), scale(rect.height));
		}

		private void drawImage(Graphics g) {
			g.drawImage(image, offsetX, offsetY, null);
		}

		private int scale(int value) {
			return (int) (value * scale);
		}

		void refresh() {
			this.image = image();
			this.repaint();
			this.repaint();
			this.invalidate();
		}

		private void setScale() {
			if (imageSize != imageSize()) image = image();
			imageSize = imageSize();
			offsetX = (this.getWidth() - imageSize) / 2;
			scale = 1.0 * this.imageSize / 5000;
		}

		private Image image() {
			try {
				InputStream is = getClass().getResourceAsStream(imageName());
				return ImageIO.read(is);

			} catch (IOException e) {
				return image;
			}
		}

		private String imageName() {
			return "/toolwindow/" + mode.name().toLowerCase() + "/intino-factory-" + imageSize() + ".png";
		}

		private int imageSize() {
			return min(500, max(200, (int) Math.floor(this.getWidth() / 100) * 100));
		}


		private MouseListener listener() {
			return new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Product product = products().filter(p -> transform(p.rect).contains(e.getX(), e.getY())).findFirst().orElse(null);
					ActionListener listener = listeners.get(product);
					if (listener == null) return;
					listener.actionPerformed(new ActionEvent(product, product.ordinal(), "Clicked"));
				}

				private Rectangle transform(Rectangle rect) {
					return new Rectangle(scale(rect.x) + offsetX, scale(rect.y) + offsetY, scale(rect.width), scale(rect.height));
				}

				@Override
				public void mousePressed(MouseEvent e) {

				}

				@Override
				public void mouseReleased(MouseEvent e) {

				}

				@Override
				public void mouseEntered(MouseEvent e) {

				}

				@Override
				public void mouseExited(MouseEvent e) {

				}
			};
		}

		public FactoryPanel model(String language, String version) {
			this.labels.put(ModelLanguage, new String[]{language, version});
			return this;
		}

		public FactoryPanel box(String language, String version) {
			this.labels.put(BoxLanguage, new String[]{language, version});
			return this;
		}

		enum Mode {
			Light("#ECECEC"), Darcula("#404243");

			private final Color color;

			Mode(String color) {
				this.color = Color.decode(color);
			}
		}

		enum Product {
			Model(1780, 20), Box(3000, 20), Imports(19, 1550), Src(1240, 1550), Gen(2399, 1550), Exports(4082, 1550),
			Out(1802, 3078), Pack(1802, 4470), Dist(1802, 5850), Deploy(1802, 7238);

			private Rectangle rect;
			private static final int size = 900;

			Product(int x, int y) {
				this.rect = new Rectangle(x, y, size, size);
			}

			Rectangle rect() {
				return rect;
			}
		}


		enum Operation {
			GenerateCode(Gen), ImportPackages(Imports), ExportAccessors(Exports),
			BuildArtifact(Out), PackArtifact(Pack), DistributeArtifact(Dist), DeployArtifact(Deploy);

			private Rectangle rect;
			private static final int size = 600;

			Operation(Product product) {
				this.rect = calculate(product.rect());
			}

			private static Rectangle calculate(Rectangle r) {
				return new Rectangle(r.x + (r.width - size) / 2, r.y - size / 2, size, size);
			}
		}

		enum Label {
			ModelLanguage(Model), BoxLanguage(Box);

			private Product product;

			Label(Product product) {
				this.product = product;
			}
		}

		class JImageButton extends JComponent implements MouseListener {

			private Operation operation;
			private ActionListener listener;
			private boolean pressed = false;
			private boolean hover = false;

			public JImageButton(Operation operation) {
				this.operation = operation;
				this.addMouseListener(this);
			}


			@Override
			public void paint(Graphics g) {
				if (!hover) return;
				Color color = foregroundColor();
				int size = pressed ? 3 : 2;

				int width = (this.getWidth() * size) / 10;
				int height = (this.getHeight() * size) / 10;

				int x = (this.getWidth() - width) / 2;
				int y = (this.getHeight() - height) / 2;
				g.setColor(color);
				g.fillOval(x, y, width, height);
				g.setColor(foregroundColor());
			}


			public void addActionListener(ActionListener listener) {
				this.listener = listener;
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (listener == null) return;
				listener.actionPerformed(new ActionEvent(operation, 0, "Clicked", e.getModifiers()));
			}

			@Override
			public void mousePressed(MouseEvent e) {
				pressed = true;
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
				repaint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				hover = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hover = false;
				repaint();
			}
		}

		private static Map<Operation, Color> colors = new HashMap<>();

		static {
			colors.put(GenerateCode, Color.decode("#FF631E"));
			colors.put(ImportPackages, Color.decode("#FE2A1A"));
			colors.put(ExportAccessors, Color.decode("#008011"));
			colors.put(BuildArtifact, Color.decode("#103FFB"));
			colors.put(PackArtifact, Color.decode("#06249F"));
			colors.put(DistributeArtifact, Color.decode("#4B1E81"));
			colors.put(DeployArtifact, Color.decode("#862FB3"));
		}
	}

}