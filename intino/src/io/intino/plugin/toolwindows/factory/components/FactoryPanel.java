package io.intino.plugin.toolwindows.factory.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.intino.plugin.toolwindows.factory.components.Label.BoxLanguage;
import static io.intino.plugin.toolwindows.factory.components.Label.ModelLanguage;
import static io.intino.plugin.toolwindows.factory.components.Mode.Darcula;
import static io.intino.plugin.toolwindows.factory.components.Mode.Light;
import static io.intino.plugin.toolwindows.factory.components.Operation.BuildArtifact;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.stream;

public class FactoryPanel extends JPanel {
	private Image image;
	private int imageSize;
	private int offsetX;
	private static final int offsetY = 10;
	private double scale;
	private final Map<Operation, ExtendedImageButton> extendedButtons = new HashMap<>();
	private final Map<Operation, CompactImageButton> compactButtons = new HashMap<>();
	private final Map<Element, ActionListener> listeners = new HashMap<>();
	private final Map<Label, String[]> labels = new HashMap<>();
	private Mode mode;

	public FactoryPanel(Mode mode) {
		this.mode = mode;
		this.setLayout(null);
		this.operations().forEach(this::createButton);
		this.addMouseListener(listener());
	}

	public FactoryPanel mode(Mode mode) {
		this.mode = mode;
		this.refresh();
		return this;
	}

	public void addActionListener(Operation operation, ActionListener listener) {
		extendedButtons.get(operation).addActionListener(listener);
		if (compactButtons.containsKey(operation)) compactButtons.get(operation).addActionListener(listener);
	}

	public void addActionListener(Element element, ActionListener listener) {
		listeners.put(element, listener);
	}

	public FactoryPanel model(String language, String version) {
		this.labels.put(ModelLanguage, new String[]{language, version});
		return this;
	}

	public FactoryPanel box(String language, String version) {
		this.labels.put(BoxLanguage, new String[]{language, version});
		return this;
	}

	void refresh() {
		this.image = image();
		this.repaint();
		this.invalidate();
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(backgroundColor());
		setScale();
		drawImage(g);
		if (scale > 0.04) {
			extendedButtons.values().forEach(b -> b.setVisible(true));
			compactButtons.values().forEach(b -> b.setVisible(false));
			elements().forEach(p -> paint(g, p));
			labels().forEach(l -> paint(g, l));
			extendedButtons.values().forEach(this::setLocation);
		} else {
			extendedButtons.values().forEach(b -> b.setVisible(false));
			compactButtons.values().forEach(b -> b.setVisible(true));
			compactButtons.values().forEach(this::setLocation);
		}
		paintComponents(g);
	}

	private void paint(Graphics g, Element element) {
		g.setFont(elementFont());
		g.setColor(foregroundColor());
		FontMetrics metrics = g.getFontMetrics(elementFont());
		Rectangle rect = element.rect();

		String name = element.name().toLowerCase();
		int x = offsetX + (scale(rect.x) + (scale(rect.width) - metrics.stringWidth(name)) / 2);
		int y = offsetY + (scale(rect.y) + ((scale(rect.height) - metrics.getHeight()) / 2 + metrics.getAscent()));
		g.drawString(name, x, y);
	}

	private void paint(Graphics g, Label label) {
		g.setFont(labelFont());
		g.setColor(foregroundColor());
		FontMetrics metrics = g.getFontMetrics(labelFont());
		Rectangle rect = label.element().rect();
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

	private void createButton(Operation operation) {
		Color color = foregroundColor();
		ExtendedImageButton button = new ExtendedImageButton(operation, color);
		extendedButtons.put(operation, button);
		if (!operation.equals(BuildArtifact)) {
			CompactImageButton value = new CompactImageButton(operation, color);
			compactButtons.put(operation, value);
			this.add(value);
		}
		this.add(button);
	}

	private Stream<Operation> operations() {
		return stream(Operation.values());
	}

	private Stream<Element> elements() {
		return stream(Element.values());
	}

	private Stream<Label> labels() {
		return stream(Label.values());
	}

	private Font labelFont() {
		return new Font("Arial", Font.BOLD, scale(180));
	}

	private Font elementFont() {
		return new Font("Arial", Font.PLAIN, scale(200));
	}

	private Color backgroundColor() {
		return mode == Light ? Light.color() : Darcula.color();
	}

	private Color foregroundColor() {
		return mode == Light ? Darcula.color() : Color.WHITE;
	}

	private void setLocation(CompactImageButton button) {
		Point point = button.getDefaultLocation();
		button.setBounds(point.x, point.y, 30, 30);
	}

	private void setLocation(ImageButton button) {
		Rectangle rect = button.operation().rect();
		button.setBounds(scale(rect.x) + offsetX, scale(rect.y) + offsetY, scale(rect.width), scale(rect.height));
	}

	private void drawImage(Graphics g) {
		g.drawImage(image, offsetX, offsetY, null);
	}

	private int scale(int value) {
		return (int) (value * scale);
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
				Element element = elements().filter(p -> transform(p.rect()).contains(e.getX(), e.getY())).findFirst().orElse(null);
				ActionListener listener = listeners.get(element);
				if (listener == null) return;
				listener.actionPerformed(new ActionEvent(element, element.ordinal(), "Clicked"));
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

}
