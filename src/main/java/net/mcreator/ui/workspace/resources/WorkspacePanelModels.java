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

import net.mcreator.element.GeneratableElement;
import net.mcreator.element.ModElementType;
import net.mcreator.generator.GeneratorStats;
import net.mcreator.io.FileIO;
import net.mcreator.ui.component.TransparentToolBar;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.dialogs.JavaModelAnimationEditorDialog;
import net.mcreator.ui.dialogs.ProgressDialog;
import net.mcreator.ui.dialogs.TextureMappingDialog;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.laf.SlickDarkScrollBarUI;
import net.mcreator.ui.workspace.IReloadableFilterable;
import net.mcreator.ui.workspace.WorkspacePanel;
import net.mcreator.util.StringUtils;
import net.mcreator.workspace.resources.Model;
import net.mcreator.workspace.resources.TexturedModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkspacePanelModels extends JPanel implements IReloadableFilterable {

	private final WorkspacePanel workspacePanel;

	private final ResourceFilterModel<Model> filterModel;
	private final JList<Model> modelList;

	WorkspacePanelModels(WorkspacePanel workspacePanel) {
		super(new BorderLayout());
		setOpaque(false);

		this.workspacePanel = workspacePanel;
		this.filterModel = new ResourceFilterModel<>(workspacePanel,
				(item, query) -> item.getReadableName().toLowerCase(Locale.ENGLISH).contains(query) || item.getType()
						.name().toLowerCase(Locale.ENGLISH).contains(query), Model::getReadableName);
		modelList = new JList<>(filterModel);

		modelList.setOpaque(false);
		modelList.setCellRenderer(new Render());
		modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modelList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		modelList.setVisibleRowCount(-1);

		modelList.addMouseMotionListener(new MouseAdapter() {
			@Override public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				int idx = modelList.locationToIndex(e.getPoint());
				Model model = modelList.getModel().getElementAt(idx);
				if (model != null) {
					workspacePanel.getMCreator().getStatusBar().setMessage(model.getReadableName());
				}
			}
		});

		JScrollPane sp = new JScrollPane(modelList);
		sp.setOpaque(false);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.getViewport().setOpaque(false);
		sp.getVerticalScrollBar().setUnitIncrement(11);
		sp.getVerticalScrollBar().setUI(new SlickDarkScrollBarUI((Color) UIManager.get("MCreatorLAF.DARK_ACCENT"),
				(Color) UIManager.get("MCreatorLAF.LIGHT_ACCENT"), sp.getVerticalScrollBar()));
		sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

		add("Center", sp);

		TransparentToolBar bar = new TransparentToolBar();
		bar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));

		JButton imp1 = L10N.button("action.workspace.resources.import_java_model");
		imp1.setIcon(UIRES.get("16px.importjavamodel"));
		imp1.setContentAreaFilled(false);
		imp1.setOpaque(false);
		ComponentUtils.deriveFont(imp1, 12);
		imp1.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

		if (workspacePanel.getMCreator().getGeneratorStats().getBaseCoverageInfo().get("model_java")
				!= GeneratorStats.CoverageStatus.NONE)
			bar.add(imp1);

		imp1.addActionListener(e -> workspacePanel.getMCreator().actionRegistry.importJavaModel.doAction());

		JButton imp2 = L10N.button("action.workspace.resources.import_json_model");
		imp2.setIcon(UIRES.get("16px.importjsonmodel"));
		imp2.setContentAreaFilled(false);
		imp2.setOpaque(false);
		ComponentUtils.deriveFont(imp2, 12);
		imp2.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

		if (workspacePanel.getMCreator().getGeneratorStats().getBaseCoverageInfo().get("model_json")
				!= GeneratorStats.CoverageStatus.NONE)
			bar.add(imp2);

		imp2.addActionListener(e -> workspacePanel.getMCreator().actionRegistry.importJSONModel.doAction());

		JButton imp3 = L10N.button("action.workspace.resources.import_obj_mtl_model");
		imp3.setIcon(UIRES.get("16px.importobjmodel"));
		imp3.setContentAreaFilled(false);
		imp3.setOpaque(false);
		ComponentUtils.deriveFont(imp3, 12);
		imp3.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

		if (workspacePanel.getMCreator().getGeneratorStats().getBaseCoverageInfo().get("model_obj")
				!= GeneratorStats.CoverageStatus.NONE)
			bar.add(imp3);

		imp3.addActionListener(e -> workspacePanel.getMCreator().actionRegistry.importOBJModel.doAction());

		JButton editTextureMappings = L10N.button("workspace.3dmodels.edit_texture_mappings");
		editTextureMappings.setIcon(UIRES.get("16px.edit.gif"));
		editTextureMappings.setOpaque(false);
		editTextureMappings.setContentAreaFilled(false);
		editTextureMappings.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		bar.add(editTextureMappings);
		editTextureMappings.addActionListener(e -> editSelectedModelTextureMappings());

		JButton editModelAnimations = L10N.button("workspace.3dmodels.redefine_animations");
		editModelAnimations.setIcon(UIRES.get("16px.edit.gif"));
		editModelAnimations.setOpaque(false);
		editModelAnimations.setContentAreaFilled(false);
		editModelAnimations.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		bar.add(editModelAnimations);
		editModelAnimations.addActionListener(e -> editSelectedModelAnimations());

		JButton del = L10N.button("workspace.3dmodels.delete_selected");
		del.setIcon(UIRES.get("16px.delete.gif"));
		del.setOpaque(false);
		del.setContentAreaFilled(false);
		del.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		bar.add(del);

		del.addActionListener(e -> deleteCurrentlySelected());

		modelList.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					deleteCurrentlySelected();
				}
			}
		});

		modelList.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					Model model = modelList.getSelectedValue();
					if (model.getType() == Model.Type.JAVA) {
						editSelectedModelAnimations();
					} else {
						editSelectedModelTextureMappings();
					}
				}
			}
		});

		add("North", bar);
	}

	private void deleteCurrentlySelected() {
		Model model = modelList.getSelectedValue();
		if (model != null) {
			int n = JOptionPane.showConfirmDialog(workspacePanel.getMCreator(),
					L10N.t("workspace.3dmodels.delete_confirm_message"), L10N.t("common.confirmation"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null);

			if (n == 0) {
				Arrays.stream(model.getFiles()).forEach(File::delete);
				reloadElements();
			}
		}
	}

	private void editSelectedModelAnimations() {
		Model model = modelList.getSelectedValue();
		if (model.getType() == Model.Type.JAVA) {
			File file = model.getFile();
			String code = FileIO.readFileToString(file);
			code = JavaModelAnimationEditorDialog.openAnimationEditorDialog(workspacePanel.getMCreator(), code);
			if (code != null) {
				FileIO.writeStringToFile(code, file);

				ProgressDialog dial = new ProgressDialog(workspacePanel.getMCreator(),
						L10N.t("workspace.3dmodels.regenerating_code"));
				Thread t = new Thread(() -> {
					ProgressDialog.ProgressUnit p0 = new ProgressDialog.ProgressUnit(
							L10N.t("workspace.3dmodels.regenerating_entity_code"));
					dial.addProgress(p0);

					AtomicInteger i = new AtomicInteger();
					// this model might be in use, we need to regenerate code of mobs
					workspacePanel.getMCreator().getWorkspace().getModElements().forEach(e -> {
						if (e.getType() == ModElementType.LIVINGENTITY && !e.isCodeLocked()) {
							GeneratableElement generatableElement = e.getGeneratableElement();
							if (generatableElement != null) {
								// generate mod element
								workspacePanel.getMCreator().getGenerator().generateElement(generatableElement);
							}
						}

						i.getAndIncrement();
						p0.setPercent((int) (((float) i.get() / (float) workspacePanel.getMCreator().getWorkspace()
								.getModElements().size()) * 100.0f));
						dial.refreshDisplay();
					});

					p0.ok();
					dial.refreshDisplay();

					ProgressDialog.ProgressUnit p2 = new ProgressDialog.ProgressUnit(
							L10N.t("workspace.3dmodels.rebuilding_workspace"));
					dial.addProgress(p2);
					workspacePanel.getMCreator().actionRegistry.buildWorkspace.doAction();
					p2.ok();
					dial.refreshDisplay();

					dial.hideAll();
				}, "WorkspaceModelsReload");
				t.start();
				dial.setVisible(true);
			}
		} else {
			JOptionPane.showMessageDialog(workspacePanel.getMCreator(),
					L10N.t("workspace.3dmodels.animation_unsupported_message"),
					L10N.t("workspace.3dmodels.animation_unsupported_title"), JOptionPane.WARNING_MESSAGE);
		}
	}

	private void editSelectedModelTextureMappings() {
		Model model = modelList.getSelectedValue();
		Map<String, TexturedModel.TextureMapping> textureMappingMap = TexturedModel.getTextureMappingsForModel(model);
		if (textureMappingMap != null) {
			textureMappingMap = new TextureMappingDialog(textureMappingMap).openMappingDialog(
					workspacePanel.getMCreator(), null, model.getType() == Model.Type.JSON);
			if (textureMappingMap != null) {
				String data = TexturedModel.getJSONForTextureMapping(textureMappingMap);
				FileIO.writeStringToFile(data, new File(workspacePanel.getMCreator().getFolderManager().getModelsDir(),
						model.getFile().getName() + ".textures"));
			}
		} else {
			JOptionPane.showMessageDialog(workspacePanel.getMCreator(),
					L10N.t("workspace.3dmodels.mappings_unsupported_message"),
					L10N.t("workspace.3dmodels.mappings_unsupported_title"), JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override public void reloadElements() {
		filterModel.removeAllElements();
		Model.getModels(workspacePanel.getMCreator().getWorkspace()).forEach(filterModel::addElement);
		refilterElements();
	}

	@Override public void refilterElements() {
		filterModel.refilter();
	}

	static class Render extends JLabel implements ListCellRenderer<Model> {

		@Override
		public JLabel getListCellRendererComponent(JList<? extends Model> list, Model ma, int index, boolean isSelected,
				boolean cellHasFocus) {
			setOpaque(isSelected);
			setBackground(isSelected ?
					(Color) UIManager.get("MCreatorLAF.LIGHT_ACCENT") :
					(Color) UIManager.get("MCreatorLAF.DARK_ACCENT"));
			setText(StringUtils.abbreviateString(ma.getReadableName(), 13));
			setToolTipText(ma.getReadableName());
			ComponentUtils.deriveFont(this, 11);
			setVerticalTextPosition(BOTTOM);
			setHorizontalTextPosition(CENTER);
			setHorizontalAlignment(CENTER);
			setIcon(UIRES.get("model." + ma.getType().name().toLowerCase(Locale.ENGLISH)));
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			return this;
		}

	}

}
