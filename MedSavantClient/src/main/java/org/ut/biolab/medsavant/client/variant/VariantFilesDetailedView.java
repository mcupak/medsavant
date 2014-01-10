/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.variant;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import java.awt.MouseInfo;
import java.awt.Point;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.SimpleVariantFile;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.BlockingPanel;
import org.ut.biolab.medsavant.client.view.genetics.QueryUtils;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author abrook
 */
class VariantFilesDetailedView extends DetailedView implements BasicVariantColumns {

    private final JPanel details;
    private final JPanel content;
    private SimpleVariantFile[] files;
    private DetailsWorker detailsWorker;
    private CollapsiblePane infoPanel;
    private final BlockingPanel blockPanel;

    public VariantFilesDetailedView(String page) {
        super(page);

        JPanel viewContainer = (JPanel) ViewUtil.clear(this.getContentPanel());
        viewContainer.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        JPanel infoContainer = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(infoContainer);

        contentPanel.add(ViewUtil.getClearBorderlessScrollPane(infoContainer), BorderLayout.CENTER);

        CollapsiblePanes panes = new CollapsiblePanes();
        panes.setOpaque(false);

        infoPanel = new CollapsiblePane();
        infoPanel.setStyle(CollapsiblePane.TREE_STYLE);
        infoPanel.setCollapsible(false);
        panes.add(infoPanel);
        panes.addExpansion();

        infoContainer.add(panes);

        content = new JPanel();
        content.setLayout(new BorderLayout());
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(content, BorderLayout.CENTER);

        details = ViewUtil.getClearPanel();

        content.add(details);

        blockPanel = new BlockingPanel("No file selected", contentPanel);
        viewContainer.add(blockPanel, BorderLayout.CENTER);
    }

    @Override
    public void setSelectedItem(Object[] item) {

        if (item.length == 0) {
            blockPanel.block();
        } else {
            files = new SimpleVariantFile[]{(SimpleVariantFile) item[0]};
            infoPanel.setTitle(files[0].getName());

            details.removeAll();
            details.updateUI();

            if (detailsWorker != null) {
                detailsWorker.cancel(true);
            }
            detailsWorker = new DetailsWorker(files[0]);
            detailsWorker.execute();
        }
    }

    public synchronized void setFileInfoList(List<String[]> info) {

        details.removeAll();

        ViewUtil.setBoxYLayout(details);

        String[][] values = new String[info.size()][2];
        for (int i = 0; i < info.size(); i++) {
            values[i][0] = info.get(i)[0];
            values[i][1] = info.get(i)[1];
        }

        details.add(ViewUtil.getKeyValuePairList(values));

        details.updateUI();

    }

    private class DetailsWorker extends MedSavantWorker<List<String[]>> {

        private final SimpleVariantFile file;

        public DetailsWorker(SimpleVariantFile f) {
            super(getPageName());
            this.file = f;
        }

        @Override
        protected List<String[]> doInBackground() throws Exception {
            return MedSavantClient.VariantManager.getTagsForUpload(LoginController.getInstance().getSessionID(), file.getUploadId());
        }

        protected void showProgress(double ignored) {
        }

        @Override
        protected void showSuccess(List<String[]> result) {
            result.add(0, new String[]{"File Name", file.getName()});
            result.add(1, new String[]{"Upload ID", Integer.toString(file.getUploadId())});
            result.add(2, new String[]{"File ID", Integer.toString(file.getFileId())});
			//result.add(3, new String[]{"DNA ID", ""}); // need to fill this field.
            setFileInfoList(result);
            blockPanel.unblock();
        }
    }

    @Override
    public void setMultipleSelections(List<Object[]> items) {
        if (items.isEmpty()) {
            blockPanel.block();
        } else {
            files = new SimpleVariantFile[items.size()];
            for (int i = 0; i < items.size(); i++) {
                files[i] = (SimpleVariantFile) (items.get(i)[0]);
            }

            if (items.isEmpty()) {
                infoPanel.setTitle("");
            } else {
                infoPanel.setTitle("Multiple uploads (" + items.size() + ")");
            }
            details.removeAll();
            details.updateUI();
        }
    }

    @Override
    public JPopupMenu createPopup() {
        JPopupMenu popupMenu = new JPopupMenu();

        if (ProjectController.getInstance().getCurrentVariantTableSchema() == null) {
            popupMenu.add(new JLabel("(You must choose a variant table before filtering)"));
        } else {

            //Filter by vcf file
            final JMenuItem filter1Item = new JMenuItem("Filter by Variant File");
            filter1Item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {                    
                    QueryUtils.addQueryOnVariantFiles(files);                                                                                           
                    MedSavantFrame.getInstance().searchAnimationFromMousePos("Selected File has been added to query.  Click 'Variants' to review and execute search.");
                }
            });
            
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }
}
