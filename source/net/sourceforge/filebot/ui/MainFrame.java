
package net.sourceforge.filebot.ui;


import static javax.swing.ScrollPaneConstants.*;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import net.sourceforge.filebot.ResourceManager;
import net.sourceforge.filebot.Settings;
import net.sourceforge.filebot.ui.panel.analyze.AnalyzePanelBuilder;
import net.sourceforge.filebot.ui.panel.episodelist.EpisodeListPanelBuilder;
import net.sourceforge.filebot.ui.panel.list.ListPanelBuilder;
import net.sourceforge.filebot.ui.panel.rename.RenamePanelBuilder;
import net.sourceforge.filebot.ui.panel.sfv.SfvPanelBuilder;
import net.sourceforge.tuned.PreferencesMap.PreferencesEntry;
import net.sourceforge.tuned.ui.ArrayListModel;
import net.sourceforge.tuned.ui.DefaultFancyListCellRenderer;
import net.sourceforge.tuned.ui.ShadowBorder;
import net.sourceforge.tuned.ui.TunedUtilities;


public class MainFrame extends JFrame {
	
	private JList selectionList = new PanelSelectionList();
	
	private HeaderPanel headerPanel = new HeaderPanel();
	
	private final PreferencesEntry<String> persistentSelectedPanel = Settings.userRoot().entry("selectedPanel");
	
	
	public MainFrame() {
		super(Settings.getApplicationName());
		
		// set taskbar / taskswitch icons
		setIconImages(Arrays.asList(ResourceManager.getImage("window.icon.small"), ResourceManager.getImage("window.icon.big")));
		
		selectionList.setModel(new ArrayListModel(createPanelBuilders()));
		
		JScrollPane selectionListScrollPane = new JScrollPane(selectionList, VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_NEVER);
		selectionListScrollPane.setBorder(new CompoundBorder(new ShadowBorder(), selectionListScrollPane.getBorder()));
		selectionListScrollPane.setOpaque(false);
		
		headerPanel.getTitleLabel().setBorder(new EmptyBorder(8, 90, 10, 0));
		
		JComponent c = (JComponent) getContentPane();
		c.setLayout(new MigLayout("insets 0, fill", "95px[fill]", "fill"));
		
		c.add(selectionListScrollPane, "pos visual.x+6 visual.y+10 n visual.y2-12");
		c.add(headerPanel, "growx, dock north");
		
		setSize(760, 615);
		
		selectionList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				showPanel((PanelBuilder) selectionList.getSelectedValue());
				persistentSelectedPanel.setValue(Integer.toString(selectionList.getSelectedIndex()));
			}
		});
		
		try {
			// restore selected panel
			selectionList.setSelectedIndex(Integer.parseInt(persistentSelectedPanel.getValue()));
		} catch (Exception e) {
			// default panel
			selectionList.setSelectedIndex(1);
		}
	}
	

	protected List<PanelBuilder> createPanelBuilders() {
		List<PanelBuilder> builders = new ArrayList<PanelBuilder>();
		
		builders.add(new ListPanelBuilder());
		builders.add(new RenamePanelBuilder());
		builders.add(new AnalyzePanelBuilder());
		builders.add(new EpisodeListPanelBuilder());
		builders.add(new SfvPanelBuilder());
		
		return builders;
	}
	

	protected void showPanel(final PanelBuilder selectedBuilder) {
		headerPanel.setTitle(selectedBuilder.getName());
		
		JComponent panel = null;
		
		for (int i = 0; i < getContentPane().getComponentCount(); i++) {
			JComponent c = (JComponent) getContentPane().getComponent(i);
			PanelBuilder builder = (PanelBuilder) c.getClientProperty("panelBuilder");
			
			if (builder != null) {
				c.setVisible(false);
				
				if (builder.equals(selectedBuilder)) {
					panel = c;
				}
			}
		}
		
		JComponent contentPane = (JComponent) getContentPane();
		
		if (panel == null) {
			panel = selectedBuilder.create();
			panel.putClientProperty("panelBuilder", selectedBuilder);
			
			contentPane.add(panel, "hidemode 3");
		}
		
		panel.setVisible(true);
		
		// update layout now
		contentPane.validate();
	}
	
	
	private static class PanelSelectionList extends JList {
		
		private static final int SELECTDELAY_ON_DRAG_OVER = 300;
		
		
		public PanelSelectionList() {
			setCellRenderer(new PanelCellRenderer());
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			setBorder(new EmptyBorder(4, 5, 4, 5));
			
			// initialize "drag over" panel selection
			new DropTarget(this, new DragDropListener());
		}
		
		
		private class DragDropListener extends DropTargetAdapter {
			
			private boolean selectEnabled = false;
			
			private Timer dragEnterTimer;
			
			
			@Override
			public void dragOver(DropTargetDragEvent dtde) {
				if (selectEnabled) {
					int index = locationToIndex(dtde.getLocation());
					setSelectedIndex(index);
				}
			}
			

			@Override
			public void dragEnter(final DropTargetDragEvent dtde) {
				dragEnterTimer = TunedUtilities.invokeLater(SELECTDELAY_ON_DRAG_OVER, new Runnable() {
					
					@Override
					public void run() {
						selectEnabled = true;
						
						// bring window to front when on dnd
						SwingUtilities.getWindowAncestor((JComponent) dtde.getSource()).toFront();
					}
				});
			}
			

			@Override
			public void dragExit(DropTargetEvent dte) {
				selectEnabled = false;
				
				if (dragEnterTimer != null) {
					dragEnterTimer.stop();
				}
			}
			

			@Override
			public void drop(DropTargetDropEvent dtde) {
				
			}
			
		}
		
	}
	

	private static class PanelCellRenderer extends DefaultFancyListCellRenderer {
		
		public PanelCellRenderer() {
			super(10, 0, new Color(0x163264));
			
			// center labels in list
			setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
			setHighlightingEnabled(false);
			
			setVerticalTextPosition(SwingConstants.BOTTOM);
			setHorizontalTextPosition(SwingConstants.CENTER);
		}
		

		@Override
		public void configureListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.configureListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			PanelBuilder panel = (PanelBuilder) value;
			setText(panel.getName());
			setIcon(panel.getIcon());
		}
		
	}
	
}
