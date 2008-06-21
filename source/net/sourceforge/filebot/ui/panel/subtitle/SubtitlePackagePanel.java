
package net.sourceforge.filebot.ui.panel.subtitle;


import java.awt.BorderLayout;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.swing.EventListModel;


public class SubtitlePackagePanel extends JPanel {
	
	private final EventList<SubtitlePackage> model = new BasicEventList<SubtitlePackage>();
	
	private final LanguageSelectionPanel languageSelection = new LanguageSelectionPanel(model);
	
	
	public SubtitlePackagePanel() {
		super(new BorderLayout());
		add(languageSelection, BorderLayout.NORTH);
		add(new JScrollPane(createList()), BorderLayout.CENTER);
	}
	

	public EventList<SubtitlePackage> getModel() {
		return model;
	}
	

	protected JList createList() {
		FilterList<SubtitlePackage> filterList = new FilterList<SubtitlePackage>(model, new LanguageMatcherEditor(languageSelection));
		ObservableElementList<SubtitlePackage> observableList = new ObservableElementList<SubtitlePackage>(filterList, GlazedLists.beanConnector(SubtitlePackage.class));
		
		JList list = new JList(new EventListModel<SubtitlePackage>(observableList));
		
		return list;
	}
	
	/*
		private void updateLanguageFilterButtonPanel() {
			
			SortedSet<String> languages = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			
			for (int i = 0; i < unfilteredModel.getSize(); i++) {
				SubtitlePackage subtitle = (SubtitlePackage) unfilteredModel.getElementAt(i);
				languages.add(subtitle.getLanguageName());
			}
			
			languageFilterPanel.removeAll();
			
			for (String language : languages) {
				LanguageFilterButton languageFilterButton = createLanguageFilterButton(language);
				languageFilterButton.addItemListener(new LanguageFilterItemListener(language));
				
				languageFilterPanel.add(languageFilterButton);
			}
		}
		

		private void updateFilteredModel() {
			SimpleListModel model = new SimpleListModel();
			
			for (int i = 0; i < unfilteredModel.getSize(); i++) {
				SubtitlePackage subtitle = (SubtitlePackage) unfilteredModel.getElementAt(i);
				
				if (isLanguageSelected(subtitle.getLanguageName())) {
					model.add(subtitle);
				}
			}
			
			super.setModel(model);
		}
		

		public boolean isLanguageSelected(String language) {
			return !languageFilterSelection.containsKey(language) || languageFilterSelection.get(language);
		}
		

		public void setLanguageSelected(String language, boolean selected) {
			languageFilterSelection.put(language, selected);
			
			Settings.getSettings().asBooleanMap(Settings.SUBTITLE_LANGUAGE).put(language, selected);
		}
		

		private LanguageFilterButton createLanguageFilterButton(String language) {
			Locale locale = LanguageResolver.getDefault().getLocale(language);
			
			boolean selected = isLanguageSelected(language);
			
			if (locale != null)
				return new LanguageFilterButton(locale, selected);
			else
				return new LanguageFilterButton(language, selected);
		}
		
		
		private class LanguageFilterItemListener implements ItemListener {
			
			private final String language;
			
			
			public LanguageFilterItemListener(String language) {
				this.language = language;
			}
			

			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
				
				setLanguageSelected(language, selected);
				
				updateFilteredModel();
			}
			
		};
		

		private class LanguageFilterButton extends JToggleButton {
			
			public LanguageFilterButton(Locale locale, boolean selected) {
				this(locale.getDisplayLanguage(Locale.ENGLISH), ResourceManager.getFlagIcon(locale.getLanguage()), selected);
			}
			

			public LanguageFilterButton(String language, boolean selected) {
				this(language, ResourceManager.getFlagIcon(null), selected);
			}
			

			public LanguageFilterButton(String language, Icon icon, boolean selected) {
				super(icon, selected);
				
				setToolTipText(language);
				setContentAreaFilled(false);
				setFocusPainted(false);
				
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				
				setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
			}
			

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				
				// make transparent if not selected
				if (!isSelected()) {
					AlphaComposite composite = AlphaComposite.SrcOver.derive(0.2f);
					g2d.setComposite(composite);
				}
				
				super.paintComponent(g2d);
			}
			
		}
		*/
}
