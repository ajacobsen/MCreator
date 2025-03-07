/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2020 Pylo and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.mcreator.ui.workspace.resources;

import net.mcreator.ui.component.JSelectableList;
import net.mcreator.ui.component.TransparentToolBar;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.dialogs.SoundElementDialog;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.laf.MCreatorTheme;
import net.mcreator.ui.laf.SlickDarkScrollBarUI;
import net.mcreator.ui.workspace.IReloadableFilterable;
import net.mcreator.ui.workspace.WorkspacePanel;
import net.mcreator.util.ListUtils;
import net.mcreator.util.SoundUtils;
import net.mcreator.workspace.elements.SoundElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Locale;

public class WorkspacePanelSounds extends JPanel implements IReloadableFilterable {

	private final WorkspacePanel workspacePanel;

	private final ResourceFilterModel<SoundElement> filterModel;

	WorkspacePanelSounds(WorkspacePanel workspacePanel) {
		super(new BorderLayout());
		setOpaque(false);

		this.workspacePanel = workspacePanel;
		this.filterModel = new ResourceFilterModel<>(workspacePanel, SoundElement::getName);

		JSelectableList<SoundElement> soundElementList = new JSelectableList<>(filterModel);
		soundElementList.setOpaque(false);
		soundElementList.setCellRenderer(new Render());
		soundElementList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		soundElementList.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2)
					editSelectedSound(soundElementList.getSelectedValue());
			}
		});

		JScrollPane sp = new JScrollPane(soundElementList);
		sp.setOpaque(false);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.getViewport().setOpaque(false);
		sp.getVerticalScrollBar().setUnitIncrement(11);
		sp.getVerticalScrollBar().setUI(new SlickDarkScrollBarUI((Color) UIManager.get("MCreatorLAF.DARK_ACCENT"),
				(Color) UIManager.get("MCreatorLAF.LIGHT_ACCENT"), sp.getVerticalScrollBar()));
		sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		sp.setBorder(null);

		add("Center", sp);

		TransparentToolBar bar = new TransparentToolBar();
		bar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));

		JButton importsound = L10N.button("action.workspace.resources.import_sound");
		importsound.setIcon(UIRES.get("16px.open.gif"));
		importsound.setContentAreaFilled(false);
		importsound.setOpaque(false);
		ComponentUtils.deriveFont(importsound, 12);
		importsound.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		bar.add(importsound);

		JButton edit = L10N.button("workspace.sounds.edit_selected");
		edit.setIcon(UIRES.get("16px.edit.gif"));
		edit.setContentAreaFilled(false);
		edit.setOpaque(false);
		ComponentUtils.deriveFont(edit, 12);
		edit.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		bar.add(edit);

		JButton del = L10N.button("workspace.sounds.delete_selected");
		del.setIcon(UIRES.get("16px.delete.gif"));
		del.setOpaque(false);
		del.setContentAreaFilled(false);
		del.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		bar.add(del);

		del.addActionListener(a -> deleteSelectedSound(workspacePanel, soundElementList));

		JButton play = L10N.button("workspace.sounds.play_selected");
		play.setIcon(UIRES.get("16px.play"));
		play.setOpaque(false);
		play.setContentAreaFilled(false);
		play.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		bar.add(play);
		play.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				SoundElement soundElement = soundElementList.getSelectedValue();
				if (soundElement != null) {
					if (!soundElement.getFiles().isEmpty()) {
						SoundUtils.playSound(
								new File(workspacePanel.getMCreator().getWorkspace().getFolderManager().getSoundsDir(),
										ListUtils.getRandomItem(soundElement.getFiles()) + ".ogg"));
						play.setEnabled(false);
					}
				}
			}

			@Override public void mouseReleased(MouseEvent e) {
				SoundUtils.stopAllSounds();
				play.setEnabled(true);
			}

		});

		soundElementList.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_DELETE -> deleteSelectedSound(workspacePanel, soundElementList);
				case KeyEvent.VK_ENTER -> editSelectedSound(soundElementList.getSelectedValue());
				}
			}
		});

		edit.addActionListener(e -> editSelectedSound(soundElementList.getSelectedValue()));
		importsound.addActionListener(e -> workspacePanel.getMCreator().actionRegistry.importSound.doAction());
		add("North", bar);

	}

	private void deleteSelectedSound(WorkspacePanel workspacePanel, JSelectableList<SoundElement> soundElementList) {
		List<SoundElement> soundElements = soundElementList.getSelectedValuesList();
		if (!soundElements.isEmpty()) {
			int n = JOptionPane.showConfirmDialog(workspacePanel.getMCreator(),
					L10N.t("workspace.sounds.confirm_deletion_message"), L10N.t("common.confirmation"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (n == 0) {
				soundElements.forEach(workspacePanel.getMCreator().getWorkspace()::removeSoundElement);
				reloadElements();
			}
		}
	}

	private void editSelectedSound(SoundElement selectedValue) {
		if (selectedValue != null) {
			SoundElementDialog.soundDialog(workspacePanel.getMCreator(), selectedValue, null);
			workspacePanel.getMCreator().getWorkspace().markDirty();
			reloadElements();
		}
	}

	@Override public void reloadElements() {
		filterModel.removeAllElements();
		workspacePanel.getMCreator().getWorkspace().getSoundElements().forEach(filterModel::addElement);
		refilterElements();
	}

	@Override public void refilterElements() {
		filterModel.refilter();
	}

	static class Render extends JPanel implements ListCellRenderer<SoundElement> {

		Render() {
			setLayout(new GridLayout());
			setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
		}

		@Override
		public JPanel getListCellRendererComponent(JList<? extends SoundElement> list, SoundElement ma, int index,
				boolean isSelected, boolean cellHasFocus) {

			removeAll();

			JPanel cont = new JPanel(new BorderLayout());
			cont.setBackground(isSelected ?
					((Color) UIManager.get("MCreatorLAF.LIGHT_ACCENT")).brighter() :
					(Color) UIManager.get("MCreatorLAF.LIGHT_ACCENT"));
			cont.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

			JPanel namepan = new JPanel(new BorderLayout());
			namepan.setBorder(BorderFactory.createEmptyBorder(0, 8, 5, 0));
			namepan.setOpaque(false);

			JLabel name = new JLabel(ma.getName());
			name.setFont(MCreatorTheme.secondary_font.deriveFont(20.0f));
			namepan.add("North", name);

			JLabel name2 = L10N.label("workspace.sounds.files", String.join(", ", ma.getFiles()));
			ComponentUtils.deriveFont(name2, 11);
			namepan.add("South", name2);

			JPanel iconpn = new JPanel(new BorderLayout());
			iconpn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			iconpn.setOpaque(false);
			iconpn.add("West", new JLabel(UIRES.get("note")));
			iconpn.add("Center", namepan);

			String rightText;

			if (ma.getSubtitle() != null && !ma.getSubtitle().isEmpty()) {
				rightText = L10N.t("workspace.sounds.subtitle_and_category", ma.getSubtitle(), ma.getCategory());
			} else {
				rightText = L10N.t("workspace.sounds.category", ma.getCategory());
			}

			JLabel rightTextLabel = new JLabel(rightText);
			ComponentUtils.deriveFont(rightTextLabel, 17);
			cont.add("East", rightTextLabel);

			cont.add("West", iconpn);

			add(cont);
			setOpaque(false);

			return this;
		}

	}

}
