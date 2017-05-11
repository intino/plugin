package io.intino.plugin.toolwindows;

import com.intellij.ProjectTopics;
import com.intellij.ide.ui.LafManager;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.util.Function;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import io.intino.plugin.actions.ExportAction;
import io.intino.plugin.actions.IntinoGenerationAction;
import io.intino.plugin.actions.PurgeAndReloadConfigurationAction;
import io.intino.plugin.actions.ReloadConfigurationAction;
import io.intino.plugin.build.ArtifactBuilder;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.console.IntinoTopics;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.intino.plugin.toolwindows.IntinoFactoryView.FactoryPanel.Mode.Darcula;
import static io.intino.plugin.toolwindows.IntinoFactoryView.FactoryPanel.Mode.Light;
import static io.intino.plugin.toolwindows.IntinoFactoryView.FactoryPanel.Operation.*;
import static io.intino.plugin.toolwindows.IntinoFactoryView.FactoryPanel.Product.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.stream;

public class IntinoFactoryView extends JPanel {
	private JPanel contentPane;
	private JComboBox modules;
	private JPanel factoryContainerPanel;
	private Project project;

	IntinoFactoryView(Project project) {
		this.project = project;
		final MessageBusConnection connect = project.getMessageBus().connect();
		connect.subscribe(ProjectTopics.MODULES, moduleListener());
		connect.subscribe(IntinoTopics.LEGIO, moduleName -> {
			loadComboBox(stream(ModuleManager.getInstance(project).getModules()).filter(m -> m.getName().equals(moduleName)).collect(Collectors.toList()));
		});
		loadComboBox(Arrays.asList(ModuleManager.getInstance(project).getModules()));
		LafManager.getInstance().addLafManagerListener(source -> {
			mode(source.getCurrentLookAndFeel().getName().equalsIgnoreCase("darcula"));
			source.repaintUI();
		});
	}

	private void generateCode() {
		new IntinoGenerationAction().execute(selectedModule());
	}

	private void build() {
		final CompilerManager compilerManager = CompilerManager.getInstance(project);
		CompileScope scope = compilerManager.createModulesCompileScope(new Module[]{selectedModule()}, true);
		compilerManager.make(scope, null);
	}

	private void build(FactoryPanel.Operation operation, int modifiers) {
		FactoryPhase phase = phaseOf(operation, (modifiers & InputEvent.SHIFT_MASK) != 0);
		if (phase == null) return;
		new ArtifactBuilder(project, Collections.singletonList(selectedModule()), phase).build();
	}

	private void exportAccessors() {
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

	private void mode(boolean underDarcula) {
		((FactoryPanel) factoryContainerPanel).mode(underDarcula ? Darcula : Light);
	}

	private void reload(int modifiers) {
		if (modifiers == ActionEvent.SHIFT_MASK) new PurgeAndReloadConfigurationAction().execute(selectedModule());
		else new ReloadConfigurationAction().execute(selectedModule());
	}

	@SuppressWarnings("unchecked")
	private void loadComboBox(List<Module> modules) {
		this.modules.setModel(new CollectionComboBoxModel<>(modules));
	}

	private void createUIComponents() {
		factoryContainerPanel = new FactoryPanel();
		((FactoryPanel) factoryContainerPanel).addActionListener(GenerateCode, e -> generateCode());
		((FactoryPanel) factoryContainerPanel).addActionListener(ImportPackages, e -> reload(e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(BuildArtifact, e -> build());
		((FactoryPanel) factoryContainerPanel).addActionListener(PackArtifact, e -> build(PackArtifact, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(ExportAccessors, e -> exportAccessors());
		((FactoryPanel) factoryContainerPanel).addActionListener(DistributeArtifact, e -> build(DistributeArtifact, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(DeployArtifact, e -> build(DeployArtifact, e.getModifiers()));
		mode(UIUtil.isUnderDarcula());
	}

	private Module selectedModule() {
		return (Module) this.modules.getSelectedItem();
	}

	Component contentPane() {
		return contentPane;
	}

	public static class FactoryPanel extends JPanel {
		private Image image;
		private int imageSize;
		private int offset;
		private double scale;
		private Font font;
		private Map<Operation, JImageButton> buttons = new HashMap<>();
		private Map<Product, ActionListener> listeners = new HashMap<>();
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
			g.setFont(font);
			drawImage(g);
			products().forEach(p -> paint(g, p));
			buttons.values().forEach(this::setLocation);
			paintComponents(g);
		}

		private void paint(Graphics g, Product product) {
			String name = product.name().toLowerCase();
			FontMetrics metrics = g.getFontMetrics(font);
			Rectangle rect = product.rect;
			int x = offset + (scale(rect.x) + (scale(rect.width) - metrics.stringWidth(name)) / 2);
			int y = (scale(rect.y) + ((scale(rect.height) - metrics.getHeight()) / 2 + metrics.getAscent()));
			g.setColor(foregroundColor());
			g.drawString(name, x, y);
		}

		private Color backgroundColor() {
			return mode == Light ? Light.color : Darcula.color;
		}

		private Color foregroundColor() {
			return mode == Light ? Color.black : Color.white;
		}

		private void setLocation(JImageButton button) {
			Rectangle rect = button.operation.rect;
			button.setBounds(scale(rect.x) + offset, scale(rect.y), scale(rect.width), scale(rect.height));
		}

		private void drawImage(Graphics g) {
			g.drawImage(image, offset, 0, null);
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
			offset = (this.getWidth() - imageSize) / 2;
			scale = 1.0 * this.imageSize / 750;
			font = new Font("Arial", Font.BOLD, scale(30));
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
			return min(500, max(300, (int) Math.floor(this.getWidth() / 100) * 100));
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
					return new Rectangle(scale(rect.x), scale(rect.y), scale(rect.width), scale(rect.height));
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

		enum Mode {
			Light("#ECECEC"), Darcula("#404243");

			private final Color color;

			Mode(String color) {
				this.color = Color.decode(color);
			}
		}

		enum Product {
			Model(315, 120), Box(500, 120), Imports(41, 361), Src(230, 360), Gen(409, 361), Out(320, 597), Exports(581, 597),
			Pack(320, 815), Repo(320, 1030), Server(320, 1245);

			private Rectangle rect;
			private static final int size = 125;

			Product(int x, int y) {
				this.rect = new Rectangle(x, y, size, size);
			}

			Rectangle rect() {
				return rect;
			}
		}


		enum Operation {
			GenerateCode(Gen), ImportPackages(Imports), ExportAccessors(Exports),
			BuildArtifact(Out), PackArtifact(Pack), DistributeArtifact(Repo), DeployArtifact(Server);

			private Rectangle rect;
			private static final int size = 80;

			Operation(Product product) {
				this.rect = calculate(product.rect());
			}

			private static Rectangle calculate(Rectangle r) {
				return new Rectangle(r.x + (r.width - size) / 2, r.y - size / 2 - 10, size, size);
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

	@NotNull
	private ModuleListener moduleListener() {
		return new ModuleListener() {
			@Override
			public void moduleAdded(@NotNull Project project, @NotNull Module module) {
				loadComboBox(Arrays.asList(ModuleManager.getInstance(project).getModules()));
			}

			@Override
			public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {

			}

			@Override
			public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
				loadComboBox(Arrays.asList(ModuleManager.getInstance(project).getModules()));
			}

			@Override
			public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
				loadComboBox(modules);
			}
		};
	}
}