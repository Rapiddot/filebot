package net.filebot.ui.rename;

import static java.awt.Font.*;
import static javax.swing.BorderFactory.*;
import static javax.swing.SwingUtilities.*;
import static net.filebot.ui.NotificationLogging.*;
import static net.filebot.util.ui.SwingUI.*;

import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import net.filebot.Language;
import net.filebot.RenameAction;
import net.filebot.ResourceManager;
import net.filebot.StandardRenameAction;
import net.filebot.UserFiles;
import net.filebot.WebServices;
import net.filebot.format.ExpressionFilter;
import net.filebot.format.ExpressionFormat;
import net.filebot.ui.HeaderPanel;
import net.filebot.ui.rename.FormatDialog.Mode;
import net.filebot.web.Datasource;
import net.filebot.web.EpisodeListProvider;
import net.filebot.web.MovieIdentificationService;
import net.filebot.web.MusicIdentificationService;
import net.filebot.web.SortOrder;
import net.miginfocom.swing.MigLayout;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class PresetEditor extends JDialog {

	enum Result {
		SET, DELETE, CANCEL;
	}

	private Result result = Result.CANCEL;

	private HeaderPanel presetNameHeader;
	private JTextField pathInput;
	private RSyntaxTextArea filterEditor;
	private RSyntaxTextArea formatEditor;
	private JComboBox<RenameAction> actionCombo;
	private JComboBox<Datasource> providerCombo;
	private JComboBox<SortOrder> sortOrderCombo;
	private JComboBox<String> matchModeCombo;
	private JComboBox<Language> languageCombo;

	private JRadioButton selectRadio;
	private JRadioButton inheritRadio;

	public PresetEditor(Window owner) {
		super(owner, "Preset Editor", ModalityType.APPLICATION_MODAL);
		JComponent c = (JComponent) getContentPane();

		presetNameHeader = new HeaderPanel();

		inheritRadio = new JRadioButton("<html>Use <b>Original Files</b> selection</html>");
		selectRadio = new JRadioButton("<html>Do <b>Select</b></html>");
		pathInput = new JTextField(40);

		filterEditor = createEditor();
		formatEditor = createEditor();

		actionCombo = createRenameActionCombo();
		providerCombo = createDataProviderCombo();
		sortOrderCombo = new JComboBox<SortOrder>(SortOrder.values());
		matchModeCombo = createMatchModeCombo();
		languageCombo = createLanguageCombo();

		JPanel inputPanel = new JPanel(new MigLayout("insets 0, fill"));
		inputPanel.setOpaque(false);
		inputPanel.add(new JLabel("Input Folder:"), "gap indent");
		inputPanel.add(pathInput, "growx, gap rel");
		inputPanel.add(createImageButton(selectInputFolder), "gap 0px, wrap");
		inputPanel.add(new JLabel("Includes:"), "gap indent, skip 1, split 2");
		inputPanel.add(wrapEditor(filterEditor), "growx, gap rel, gap after 40px");

		JPanel inputGroup = createGroupPanel("Files");
		inputGroup.add(selectRadio);
		inputGroup.add(inheritRadio, "wrap");
		inputGroup.add(inputPanel);

		JPanel formatGroup = createGroupPanel("Format");
		formatGroup.add(new JLabel("Expression:"));
		formatGroup.add(wrapEditor(formatEditor), "growx, gap rel");
		formatGroup.add(createImageButton(editFormatExpression), "gap 10px");

		JPanel searchGroup = createGroupPanel("Options");
		searchGroup.add(new JLabel("Datasource:"), "sg label");
		searchGroup.add(providerCombo, "sg combo");
		searchGroup.add(new JLabel("Episode Order:"), "sg label, gap indent");
		searchGroup.add(sortOrderCombo, "sg combo, wrap");
		searchGroup.add(new JLabel("Language:"), "sg label");
		searchGroup.add(languageCombo, "sg combo");
		searchGroup.add(new JLabel("Match Mode:"), "sg label, gap indent");
		searchGroup.add(matchModeCombo, "sg combo, wrap");
		searchGroup.add(new JLabel("Rename Action:"), "sg label");
		searchGroup.add(actionCombo, "sg combo, wrap");

		c.setLayout(new MigLayout("insets dialog, hidemode 3, nogrid, fill"));
		c.add(presetNameHeader, "wmin 150px, hmin 75px, growx, dock north");
		c.add(inputGroup, "growx, wrap");
		c.add(formatGroup, "growx, wrap");
		c.add(searchGroup, "growx, wrap push");
		c.add(new JButton(ok), "tag apply");
		c.add(new JButton(delete), "tag cancel");

		ButtonGroup inputButtonGroup = new ButtonGroup();
		inputButtonGroup.add(inheritRadio);
		inputButtonGroup.add(selectRadio);
		inheritRadio.setSelected(true);
		inputPanel.setVisible(false);
		selectRadio.addItemListener((evt) -> {
			inputPanel.setVisible(selectRadio.isSelected());
		});
		providerCombo.addItemListener((evt) -> {
			sortOrderCombo.setEnabled(evt.getItem() instanceof EpisodeListProvider);
		});

		setSize(650, 570);
	}

	public void setPreset(Preset p) {
		presetNameHeader.getTitleLabel().setText(p.getName());
		pathInput.setText(p.getInputFolder() == null ? "" : p.getInputFolder().getPath());
		filterEditor.setText(p.getIncludeFilter() == null ? "" : p.getIncludeFilter().getExpression());
		formatEditor.setText(p.getFormat() == null ? "" : p.getFormat().getExpression());
		providerCombo.setSelectedItem(p.getDatabase() == null ? WebServices.TheTVDB : p.getDatabase());
		sortOrderCombo.setSelectedItem(p.getSortOrder() == null ? SortOrder.Airdate : p.getSortOrder());
		matchModeCombo.setSelectedItem(p.getMatchMode() == null ? RenamePanel.MATCH_MODE_OPPORTUNISTIC : p.getMatchMode());
		actionCombo.setSelectedItem(p.getRenameAction() == null ? StandardRenameAction.MOVE : p.getRenameAction());

		selectRadio.setSelected(p.getInputFolder() != null);
	}

	public Preset getPreset() throws Exception {
		String name = presetNameHeader.getTitleLabel().getText();
		File path = inheritRadio.isSelected() ? null : new File(pathInput.getText());
		ExpressionFilter includes = inheritRadio.isSelected() ? null : new ExpressionFilter(filterEditor.getText());
		ExpressionFormat format = formatEditor.getText().trim().isEmpty() ? null : new ExpressionFormat(formatEditor.getText());
		Datasource database = ((Datasource) providerCombo.getSelectedItem());
		SortOrder sortOrder = ((SortOrder) sortOrderCombo.getSelectedItem());
		String matchMode = (String) matchModeCombo.getSelectedItem();
		Language language = ((Language) languageCombo.getSelectedItem());
		StandardRenameAction action = (StandardRenameAction) actionCombo.getSelectedItem();

		return new Preset(name, path, includes, format, database, sortOrder, matchMode, language, action);
	}

	private JPanel createGroupPanel(String title) {
		JPanel inputGroup = new JPanel(new MigLayout("insets dialog, hidemode 3, nogrid, fill"));
		inputGroup.setBorder(createTitledBorder(title));
		return inputGroup;
	}

	private RSyntaxTextArea createEditor() {
		final RSyntaxTextArea editor = new RSyntaxTextArea(new RSyntaxDocument(SyntaxConstants.SYNTAX_STYLE_GROOVY) {
			@Override
			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				super.insertString(offs, str.replaceAll("\\s", " "), a); // FORCE SINGLE LINE
			}
		}, null, 1, 80);

		editor.setAntiAliasingEnabled(true);
		editor.setAnimateBracketMatching(false);
		editor.setAutoIndentEnabled(false);
		editor.setClearWhitespaceLinesEnabled(false);
		editor.setBracketMatchingEnabled(true);
		editor.setCloseCurlyBraces(false);
		editor.setCodeFoldingEnabled(false);
		editor.setHyperlinksEnabled(false);
		editor.setUseFocusableTips(false);
		editor.setHighlightCurrentLine(false);
		editor.setLineWrap(false);
		editor.setFont(new Font(MONOSPACED, PLAIN, 14));

		return editor;
	}

	private RTextScrollPane wrapEditor(RSyntaxTextArea editor) {
		RTextScrollPane scroll = new RTextScrollPane(editor, false);
		scroll.setLineNumbersEnabled(false);
		scroll.setFoldIndicatorEnabled(false);
		scroll.setIconRowHeaderEnabled(false);
		scroll.setVerticalScrollBarPolicy(RTextScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scroll.setHorizontalScrollBarPolicy(RTextScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBackground(editor.getBackground());
		scroll.setOpaque(true);
		scroll.setBorder(pathInput.getBorder());
		return scroll;
	}

	private JComboBox<Datasource> createDataProviderCombo() {
		DefaultComboBoxModel<Datasource> providers = new DefaultComboBoxModel<>();
		for (Datasource[] seq : new Datasource[][] { WebServices.getEpisodeListProviders(), WebServices.getMovieIdentificationServices(), WebServices.getMusicIdentificationServices() }) {
			for (Datasource it : seq) {
				providers.addElement(it);
			}
		}

		JComboBox<Datasource> combo = new JComboBox<Datasource>(providers);
		combo.setRenderer(new ListCellRenderer<Object>() {

			private final ListCellRenderer<Object> parent = (ListCellRenderer<Object>) combo.getRenderer();

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) parent.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof Datasource) {
					Datasource provider = (Datasource) value;
					label.setText(provider.getName());
					label.setIcon(provider.getIcon());
				}

				return label;
			}
		});

		return combo;
	}

	private JComboBox<String> createMatchModeCombo() {
		String[] modes = new String[] { RenamePanel.MATCH_MODE_OPPORTUNISTIC, RenamePanel.MATCH_MODE_STRICT };
		JComboBox<String> combo = new JComboBox<>(modes);
		return combo;
	}

	private JComboBox<Language> createLanguageCombo() {
		DefaultComboBoxModel<Language> languages = new DefaultComboBoxModel<>();
		for (Language it : Language.preferredLanguages()) {
			languages.addElement(it);
		}
		for (Language it : Language.availableLanguages()) {
			languages.addElement(it);
		}

		JComboBox<Language> combo = new JComboBox<Language>(languages);
		combo.setRenderer(new ListCellRenderer<Language>() {

			private final ListCellRenderer<Language> parent = (ListCellRenderer<Language>) combo.getRenderer();

			@Override
			public Component getListCellRendererComponent(JList<? extends Language> list, Language value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) parent.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof Language) {
					Language it = value;
					label.setText(it.getName());
					label.setIcon(ResourceManager.getFlagIcon(it.getCode()));
				}

				return label;
			}

		});

		return combo;
	}

	private JComboBox<RenameAction> createRenameActionCombo() {
		DefaultComboBoxModel<RenameAction> actions = new DefaultComboBoxModel<>();
		for (StandardRenameAction it : EnumSet.of(StandardRenameAction.MOVE, StandardRenameAction.COPY, StandardRenameAction.KEEPLINK, StandardRenameAction.SYMLINK, StandardRenameAction.HARDLINK)) {
			actions.addElement(it);
		}

		JComboBox<RenameAction> combo = new JComboBox<RenameAction>(actions);
		combo.setRenderer(new ListCellRenderer<RenameAction>() {

			private final ListCellRenderer<RenameAction> parent = (ListCellRenderer<RenameAction>) combo.getRenderer();

			@Override
			public Component getListCellRendererComponent(JList<? extends RenameAction> list, RenameAction value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) parent.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof StandardRenameAction) {
					StandardRenameAction it = (StandardRenameAction) value;
					label.setText(it.getDisplayName());
					label.setIcon(ResourceManager.getIcon("rename.action." + it.toString().toLowerCase()));
				}

				return label;
			}

		});

		return combo;
	}

	public Result getResult() {
		return result;
	}

	private final Action selectInputFolder = new AbstractAction("Select Input Folder", ResourceManager.getIcon("action.load")) {

		@Override
		public void actionPerformed(ActionEvent evt) {
			File f = UserFiles.showOpenDialogSelectFolder(null, "Select Input Folder", evt);
			if (f != null) {
				pathInput.setText(f.getAbsolutePath());
			}
		}
	};

	private final Action editFormatExpression = new AbstractAction("Open Format Editor", ResourceManager.getIcon("action.format")) {

		@Override
		public void actionPerformed(ActionEvent evt) {
			FormatDialog.Mode mode = FormatDialog.Mode.File;
			if (providerCombo.getSelectedItem() instanceof EpisodeListProvider) {
				mode = FormatDialog.Mode.Episode;
			} else if (providerCombo.getSelectedItem() instanceof MovieIdentificationService) {
				mode = FormatDialog.Mode.Movie;
			} else if (providerCombo.getSelectedItem() instanceof MusicIdentificationService) {
				mode = FormatDialog.Mode.Music;
			}

			FormatDialog dialog = new FormatDialog(getWindow(evt.getSource()), mode, null);
			dialog.setFormatCode(formatEditor.getText());
			dialog.setLocation(getOffsetLocation(dialog.getOwner()));
			dialog.setVisible(true);

			if (dialog.submit()) {
				formatEditor.setText(dialog.getFormat().getExpression());
			}
		}
	};

	private final Action ok = new AbstractAction("Preset", ResourceManager.getIcon("dialog.continue")) {

		@Override
		public void actionPerformed(ActionEvent evt) {
			try {
				Preset preset = getPreset();
				if (preset != null) {
					result = Result.SET;
					setVisible(false);
				}
			} catch (Exception e) {
				UILogger.severe("Invalid preset settings: " + e.getMessage());
			}
		}
	};

	private final Action delete = new AbstractAction("Delete", ResourceManager.getIcon("dialog.cancel")) {

		@Override
		public void actionPerformed(ActionEvent evt) {
			result = Result.DELETE;
			setVisible(false);
		}
	};

}
